package com.example.traitortracing.controller;

import com.example.traitortracing.service.WatermarkClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

import com.example.traitortracing.entity.Downloads;
import com.example.traitortracing.entity.Images;
import com.example.traitortracing.entity.Users;
import com.example.traitortracing.repository.DownloadsRepository;
import com.example.traitortracing.repository.ImagesRepository;
import com.example.traitortracing.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.UUID;

@RestController
@RequestMapping("/api/integration")
@CrossOrigin("*")
public class IntegrationController {

    @Autowired
    private WatermarkClientService watermarkClientService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ImagesRepository imagesRepository;
    @Autowired
    private DownloadsRepository downloadsRepository;

    @GetMapping(value = "/download/{imageId}")
    public ResponseEntity<byte[]> downloadSecureImage(@PathVariable UUID imageId) {
        try {
            // 1. Lấy thông tin user đang đăng nhập
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepository.getUserByUsername(username);
            if (user == null || user.getFingerprint_bits() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2. Lấy thông tin ảnh từ DB
            Images image = imagesRepository.findById(imageId).orElse(null);
            if (image == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 3. Đọc ảnh từ ổ cứng
            Path path = Paths.get(image.getFilePath());
            byte[] fileBytes = Files.readAllBytes(path);

            // 4. Gọi Python API để nhúng watermark
            byte[] watermarkedImage = watermarkClientService.embedWatermark(fileBytes, image.getFileName(), user.getFingerprint_bits());

            // 5. Lưu lịch sử tải xuống
            Downloads downloadRecord = new Downloads();
            downloadRecord.setUser(user);
            downloadRecord.setImage(image);
            downloadsRepository.save(downloadRecord);

            // 6. Trả về cho client
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("attachment", "secure_" + image.getFileName());
            return new ResponseEntity<>(watermarkedImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/trace-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> traceLeakedImage(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Lấy tất cả users có fingerprint hợp lệ
            List<com.example.traitortracing.entity.Users> usersWithFingerprint = userRepository.findAll().stream()
                    .filter(u -> u.getFingerprint_bits() != null && u.getFingerprint_bits().matches("^[01]+$"))
                    .collect(java.util.stream.Collectors.toList());

            if (usersWithFingerprint.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "Không tìm thấy dữ liệu vân tay hợp lệ trong hệ thống."));
            }

            // 2. Trích xuất fingerprint (Sử dụng độ dài chuẩn từ DB)
            int fingerprintLength = usersWithFingerprint.get(0).getFingerprint_bits().length();
            String extractedFingerprint = watermarkClientService.extractWatermark(file, fingerprintLength);

            if (extractedFingerprint == null || extractedFingerprint.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "Không thể trích xuất vân tay từ ảnh này."));
            }

            // 3. Tìm kiếm nghi phạm thực sự (Phải vượt ngưỡng Threshold)
            com.example.traitortracing.entity.Users bestMatch = null;
            double bestScore = -1.0;
            Map<String, Object> bestAccuseResult = null;

            for (com.example.traitortracing.entity.Users user : usersWithFingerprint) {
                Map<String, Object> accuseResult = watermarkClientService.accuse(user.getFingerprint_bits(), extractedFingerprint);

                double score = ((Number) accuseResult.getOrDefault("score", 0)).doubleValue();
                double threshold = ((Number) accuseResult.getOrDefault("threshold", 0)).doubleValue();
                boolean isAttacker = (boolean) accuseResult.getOrDefault("is_attacker", false);

                // Cập nhật người khớp nhất NHƯNG phải thỏa mãn điều kiện là kẻ tấn công (vượt ngưỡng)
                if (isAttacker && score > bestScore) {
                    bestScore = score;
                    bestMatch = user;
                    bestAccuseResult = accuseResult;
                }
            }

            // 4. Trả về kết quả
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("extracted_fingerprint", extractedFingerprint);

            if (bestMatch != null) {
                result.put("status", "FOUND");
                result.put("suspect_username", bestMatch.getUsername());
                result.put("confidence", bestScore);
                result.put("suspect_id", bestMatch.getId().toString());

                // Lấy lịch sử download
                List<Downloads> downloads = downloadsRepository.findByUser(bestMatch);
                result.put("download_count", downloads.size());
                if (!downloads.isEmpty()) {
                    result.put("last_download_at", downloads.get(downloads.size() - 1).getDownloaded_at());
                }

                if (bestAccuseResult != null) result.putAll(bestAccuseResult);
            } else {
                result.put("status", "NOT_FOUND");
                result.put("message", "Ảnh sạch hoặc không tìm thấy người dùng trùng khớp vượt ngưỡng an toàn.");
                result.put("max_score_found", bestScore > -1 ? bestScore : 0);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/trace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> traceImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("length") int length,
            @RequestParam("original_fingerprint") String originalFingerprint) {
        try {
            // 1. Trích xuất fingerprint từ ảnh rò rỉ
            String extracted = watermarkClientService.extractWatermark(file, length);
            
            // 2. Đối chiếu (Accuse) để lấy điểm số
            Map<String, Object> result = watermarkClientService.accuse(originalFingerprint, extracted);
            result.put("extracted_fingerprint", extracted);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
