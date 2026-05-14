package com.example.traitortracing.controller;

import com.example.traitortracing.service.WatermarkClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    @Autowired
    private WatermarkClientService watermarkClientService;

    // API để User tải ảnh (Backend nhận ảnh, gọi Python nhúng fingerprint rồi trả về)
    @PostMapping(value = "/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> downloadWithWatermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fingerprint") String fingerprint) {
        try {
            byte[] watermarkedImage = watermarkClientService.embedWatermark(file, fingerprint);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("attachment", "secure_download.jpg");
            return new ResponseEntity<>(watermarkedImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // API để Admin truy vết ảnh rò rỉ
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
