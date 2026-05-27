package com.example.traitortracing.controller;

import com.example.traitortracing.configuration.StoragePathResolver;
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
import com.example.traitortracing.entity.TraceResults;
import com.example.traitortracing.repository.DownloadsRepository;
import com.example.traitortracing.repository.ImagesRepository;
import com.example.traitortracing.repository.UserRepository;
import com.example.traitortracing.repository.TraceResultsRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import java.nio.file.Files;
import java.nio.file.Path;
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
    @Autowired
    private TraceResultsRepository traceResultsRepository;
    @Autowired
    private StoragePathResolver storagePathResolver;

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
            Path path = storagePathResolver.resolveStoredFile(image.getFilePath());
            if (!Files.exists(path)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            byte[] fileBytes = Files.readAllBytes(path);

            // 4. Gọi Python API để nhúng watermark
            byte[] watermarkedImage = watermarkClientService.embedWatermark(fileBytes, image.getFileName(),
                    user.getFingerprint_bits());

            // 5. Lưu lịch sử tải xuống
            Downloads downloadRecord = new Downloads();
            downloadRecord.setUser(user);
            downloadRecord.setImage(image);
            downloadsRepository.save(downloadRecord);

            // 6. Trả về cho client
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("attachment", "secure_" + username + "_" + image.getFileName());
            return new ResponseEntity<>(watermarkedImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/trace-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> traceLeakedImage(
            @RequestParam("file") MultipartFile file) {
        try {

            // 1. Lấy users hợp lệ (Q-ary dùng số từ 0 đến 7)
            List<com.example.traitortracing.entity.Users> usersWithFingerprint = userRepository.findAll().stream()
                    .filter(u -> u.getFingerprint_bits() != null
                            && u.getFingerprint_bits().matches("^[0-7]+$"))
                    .collect(java.util.stream.Collectors.toList());

            if (usersWithFingerprint.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "error", "Không tìm thấy dữ liệu vân tay hệ Q-ary hợp lệ trong hệ thống."));
            }

            int fingerprintLength = usersWithFingerprint.get(0).getFingerprint_bits().length();

            // 2. Tự động Trace thông qua SIFT Search và DWT-DCT
            Map<String, Object> autoTraceResult = watermarkClientService.autoTrace(file, fingerprintLength);
            String extractedFingerprint = (String) autoTraceResult.get("extracted_fingerprint");
            String originalImageIdStr = (String) autoTraceResult.get("original_image_id");

            if (extractedFingerprint == null || extractedFingerprint.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "Không thể trích xuất vân tay từ ảnh này."));
            }

            // 3. Tìm best match theo độ chính xác
            com.example.traitortracing.entity.Users bestMatch = null;
            double bestScore = -1.0;
            Map<String, Object> bestAccuseResult = null;

            for (com.example.traitortracing.entity.Users user : usersWithFingerprint) {
                Map<String, Object> accuseResult = watermarkClientService.accuse(
                        user.getFingerprint_bits(),
                        extractedFingerprint);

                double score = ((Number) accuseResult.getOrDefault("score", 0)).doubleValue();
                double accuracy = ((Number) accuseResult.getOrDefault("accuracy", 0)).doubleValue();
                boolean isAttacker = (boolean) accuseResult.getOrDefault("is_attacker", false);

                // Ưu tiên điểm cao nhất VÀ phải vượt ngưỡng an toàn
                if (isAttacker && score > bestScore) {
                    bestScore = score;
                    bestMatch = user;
                    bestAccuseResult = accuseResult;
                    bestAccuseResult.put("best_accuracy", accuracy);
                }
            }

            // 4. Build response
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("extracted_fingerprint", extractedFingerprint);
            
            if (originalImageIdStr != null) {
                result.put("matched_original_image_id", originalImageIdStr);
                try {
                    Images origImage = imagesRepository.findById(UUID.fromString(originalImageIdStr)).orElse(null);
                    if (origImage != null) {
                        result.put("matched_original_filename", origImage.getFileName());
                    }
                } catch (Exception e) {}
            }

            if (bestMatch != null) {

                result.put("status", "FOUND");
                result.put("suspect_username", bestMatch.getUsername());
                result.put("suspect_id", bestMatch.getId().toString());
                
                // Trả về accuracy để UI hiển thị phần trăm chuẩn xác (VD: 0.99 -> 99%)
                double finalAccuracy = ((Number) bestAccuseResult.getOrDefault("best_accuracy", 0)).doubleValue();
                result.put("confidence", finalAccuracy);

                // download info
                List<Downloads> downloads = downloadsRepository.findByUser(bestMatch);
                result.put("download_count", downloads.size());

                if (!downloads.isEmpty()) {
                    result.put("last_download_at",
                            downloads.get(downloads.size() - 1).getDownloaded_at());
                }

                if (bestAccuseResult != null) {
                    result.putAll(bestAccuseResult);
                }

                // Tự động lưu kết quả điều tra vào database
                try {
                    TraceResults traceRecord = TraceResults.builder()
                            .extractedFingerprint(extractedFingerprint)
                            .matchedUserId(bestMatch.getId())
                            .confidence(finalAccuracy)
                            .sourceUrl(file.getOriginalFilename() != null ? file.getOriginalFilename() : "leaked_image.jpg")
                            .build();
                    traceResultsRepository.save(traceRecord);
                } catch (Exception dbEx) {
                    dbEx.printStackTrace();
                }

            } else {
                result.put("status", "NOT_FOUND");
                result.put("message",
                        "Ảnh sạch hoặc không tìm thấy người dùng trùng khớp vượt ngưỡng an toàn.");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
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

    @PostMapping(value = "/trace-screenshot", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> traceScreenshot(
            @RequestParam("file") MultipartFile file,
            @RequestParam("originalImageId") UUID originalImageId) {
        try {
            // 1. Lấy thông tin ảnh gốc từ DB
            Images image = imagesRepository.findById(originalImageId).orElse(null);
            if (image == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy ảnh gốc tương ứng trong hệ thống."));
            }

            // 2. Đọc ảnh gốc từ ổ cứng
            Path path = storagePathResolver.resolveStoredFile(image.getFilePath());
            byte[] originalBytes = Files.readAllBytes(path);

            // 3. Gọi Python API `/trace_screenshot` để nắn thẳng và bóc tách dấu vết vi phân
            Map<String, Object> pythonResult = watermarkClientService.traceScreenshot(file, originalBytes, image.getFileName());
            if (pythonResult == null || pythonResult.containsKey("error")) {
                String errMsg = pythonResult != null && pythonResult.containsKey("error") ? (String) pythonResult.get("error") : "Không thể bóc tách dấu vết vi phân.";
                return ResponseEntity.ok(Map.of("error", errMsg));
            }

            String extractedFingerprint = (String) pythonResult.get("extracted_fingerprint");
            if (extractedFingerprint == null || extractedFingerprint.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "Không thể giải mã dấu vết vi phân từ ảnh chụp màn hình này."));
            }

            // 4. Lấy tất cả users có fingerprint hợp lệ
            List<com.example.traitortracing.entity.Users> usersWithFingerprint = userRepository.findAll().stream()
                    .filter(u -> u.getFingerprint_bits() != null && u.getFingerprint_bits().matches("^[01]+$"))
                    .collect(java.util.stream.Collectors.toList());

            if (usersWithFingerprint.isEmpty()) {
                return ResponseEntity.ok(Map.of("error", "Không tìm thấy danh sách vân tay người dùng trong hệ thống."));
            }

            // 5. Đối chiếu (Accuse) để tìm thủ phạm khớp nhất dựa trên tỉ lệ trùng khớp (accuracy)
            com.example.traitortracing.entity.Users bestMatch = null;
            double bestScore = -1.0;
            double bestAccuracy = -1.0;
            Map<String, Object> bestAccuseResult = null;

            for (com.example.traitortracing.entity.Users user : usersWithFingerprint) {
                Map<String, Object> accuseResult = watermarkClientService.accuse(
                        user.getFingerprint_bits(),
                        extractedFingerprint);

                double score = ((Number) accuseResult.getOrDefault("score", 0)).doubleValue();
                double accuracy = ((Number) accuseResult.getOrDefault("accuracy", 0)).doubleValue();

                if (accuracy > bestAccuracy) {
                    bestAccuracy = accuracy;
                    bestScore = score;
                    bestMatch = user;
                    bestAccuseResult = accuseResult;
                }
            }

            // 6. Xây dựng kết quả trả về
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("extracted_fingerprint", extractedFingerprint);
            result.put("best_accuracy", bestAccuracy);
            result.put("best_score", bestScore);

            double THRESHOLD_ACCURACY = 0.60; // Đặt ngưỡng 65% cho ảnh chụp màn hình vi phân do nén/nhiễu camera

            if (bestMatch != null && bestAccuracy >= THRESHOLD_ACCURACY) {
                result.put("status", "FOUND");
                result.put("suspect_username", bestMatch.getUsername());
                result.put("suspect_id", bestMatch.getId().toString());
                result.put("confidence", bestAccuracy);

                // Lịch sử tải xuống
                List<Downloads> downloads = downloadsRepository.findByUser(bestMatch);
                result.put("download_count", downloads.size());
                if (!downloads.isEmpty()) {
                    result.put("last_download_at", downloads.get(downloads.size() - 1).getDownloaded_at());
                }

                if (bestAccuseResult != null) {
                    result.putAll(bestAccuseResult);
                }

                // Tự động lưu log điều tra
                try {
                    TraceResults traceRecord = TraceResults.builder()
                            .extractedFingerprint(extractedFingerprint)
                            .matchedUserId(bestMatch.getId())
                            .confidence(bestAccuracy)
                            .sourceUrl("[SCREENSHOT] " + file.getOriginalFilename())
                            .build();
                    traceResultsRepository.save(traceRecord);
                } catch (Exception dbEx) {
                    dbEx.printStackTrace();
                }
            } else {
                result.put("status", "NOT_FOUND");
                result.put("message", "Không tìm thấy người dùng trùng khớp đủ độ tin cậy từ ảnh chụp màn hình.");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống khi phân tích ảnh chụp màn hình: " + e.getMessage()));
        }
    }
}
