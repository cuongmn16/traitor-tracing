package com.example.traitortracing.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service
public class WatermarkClientService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String PYTHON_API_URL = "http://localhost:8000";

    public byte[] embedWatermark(MultipartFile file, String fingerprint) throws Exception {
        return embedWatermark(file.getBytes(), file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg", fingerprint);
    }

    public byte[] embedWatermark(byte[] fileBytes, String fileName, String fingerprint) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() { return fileName; }
        });
        body.add("fingerprint", fingerprint);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.postForEntity(PYTHON_API_URL + "/embed", requestEntity, byte[].class);
        return response.getBody();
    }

    public String extractWatermark(MultipartFile file, int length) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() { return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg"; }
        });
        body.add("length", length);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_API_URL + "/extract", requestEntity, Map.class);
        return (String) response.getBody().get("extracted_fingerprint");
    }

    public String generateFingerprint() {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_API_URL + "/generate_fingerprint", null, Map.class);
            return (String) response.getBody().get("fingerprint");
        } catch (Exception e) {
            // Fallback ngẫu nhiên nếu Python chưa bật
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100; i++) sb.append(Math.random() > 0.5 ? "1" : "0");
            return sb.toString();
        }
    }

    public Map<String, Object> accuse(String original, String extracted) {
        HttpHeaders headers = new HttpHeaders();
        // BẮT BUỘC dùng FORM_URLENCODED vì Python dùng Form(...)
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // BẮT BUỘC dùng MultiValueMap
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("fingerprint_original", original);
        body.add("fingerprint_extracted", extracted);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Gửi request
        ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_API_URL + "/accuse", requestEntity, Map.class);
        return response.getBody();
    }
}
