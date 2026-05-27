package com.example.traitortracing.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class WatermarkClientService {

    private final RestTemplate restTemplate;
    private final String pythonApiUrl;
    private final String internalServiceKey;

    public WatermarkClientService(
            RestTemplate restTemplate,
            @Value("${tracing.python-base-url:http://localhost:8000}") String pythonApiUrl,
            @Value("${tracing.internal-service-key:tracing-internal}") String internalServiceKey) {
        this.restTemplate = restTemplate;
        this.pythonApiUrl = pythonApiUrl.replaceAll("/$", "");
        this.internalServiceKey = internalServiceKey;
    }

    private HttpHeaders tracingHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        if (contentType != null) {
            headers.setContentType(contentType);
        }
        headers.set("X-Internal-Service-Key", internalServiceKey);
        return headers;
    }

    public byte[] embedWatermark(MultipartFile file, String fingerprint) throws Exception {
        return embedWatermark(
                file.getBytes(),
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg",
                fingerprint);
    }

    public byte[] embedWatermark(byte[] fileBytes, String fileName, String fingerprint) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        body.add("fingerprint", fingerprint);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, tracingHeaders(MediaType.MULTIPART_FORM_DATA));
        ResponseEntity<byte[]> response =
                restTemplate.postForEntity(pythonApiUrl + "/embed", requestEntity, byte[].class);
        return response.getBody();
    }

    public String extractWatermark(MultipartFile file, int length) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            }
        });
        body.add("length", String.valueOf(length));

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, tracingHeaders(MediaType.MULTIPART_FORM_DATA));
        ResponseEntity<Map> response =
                restTemplate.postForEntity(pythonApiUrl + "/extract", requestEntity, Map.class);
        return (String) response.getBody().get("extracted_fingerprint");
    }

    public String extractWatermarkWithOriginal(
            MultipartFile file, byte[] originalBytes, int length, boolean isScreenshotMode) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "leaked.jpg";
            }
        });
        body.add("length", String.valueOf(length));
        body.add("original_file", new ByteArrayResource(originalBytes) {
            @Override
            public String getFilename() {
                return "original.jpg";
            }
        });
        body.add("is_screenshot_mode", String.valueOf(isScreenshotMode));

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, tracingHeaders(MediaType.MULTIPART_FORM_DATA));
        ResponseEntity<Map> response =
                restTemplate.postForEntity(pythonApiUrl + "/extract", requestEntity, Map.class);
        return (String) response.getBody().get("extracted_fingerprint");
    }

    public String generateFingerprint() {
        try {
            HttpEntity<Void> requestEntity = new HttpEntity<>(tracingHeaders(null));
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonApiUrl + "/generate_fingerprint", requestEntity, Map.class);
            Object fp = response.getBody().get("fingerprint");
            if (fp == null || fp.toString().isBlank()) {
                throw new IllegalStateException("Tracing service returned empty fingerprint");
            }
            return fp.toString();
        } catch (RestClientException e) {
            throw new IllegalStateException("Tracing service unavailable: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> accuse(String original, String extracted) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("fingerprint_original", original);
        body.add("fingerprint_extracted", extracted);

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(body, tracingHeaders(MediaType.APPLICATION_FORM_URLENCODED));
        ResponseEntity<Map> response =
                restTemplate.postForEntity(pythonApiUrl + "/accuse", requestEntity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> traceScreenshot(MultipartFile leakedFile, byte[] originalBytes, String originalFileName)
            throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(leakedFile.getBytes()) {
            @Override
            public String getFilename() {
                return leakedFile.getOriginalFilename() != null ? leakedFile.getOriginalFilename() : "leaked.jpg";
            }
        });
        body.add("original_file", new ByteArrayResource(originalBytes) {
            @Override
            public String getFilename() {
                return originalFileName;
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, tracingHeaders(MediaType.MULTIPART_FORM_DATA));
        ResponseEntity<Map> response =
                restTemplate.postForEntity(pythonApiUrl + "/trace_screenshot", requestEntity, Map.class);
        return response.getBody();
    }

    public void saveSiftVector(byte[] fileBytes, String imageId) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return "image.jpg";
            }
        });
        body.add("image_id", imageId);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, tracingHeaders(MediaType.MULTIPART_FORM_DATA));
        restTemplate.postForEntity(pythonApiUrl + "/save_sift_vector", requestEntity, Map.class);
    }

    public void deleteSiftVector(String imageId) {
        try {
            HttpEntity<Void> requestEntity = new HttpEntity<>(tracingHeaders(null));
            restTemplate.exchange(
                    pythonApiUrl + "/sift_vector/" + imageId,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class);
        } catch (RestClientException e) {
            System.out.println("Cảnh báo: không xóa được SIFT vector " + imageId + ": " + e.getMessage());
        }
    }

    public Map<String, Object> autoTrace(MultipartFile file, int length) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "leaked.jpg";
            }
        });
        body.add("length", String.valueOf(length));

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, tracingHeaders(MediaType.MULTIPART_FORM_DATA));
        ResponseEntity<Map> response =
                restTemplate.postForEntity(pythonApiUrl + "/auto_trace", requestEntity, Map.class);
        return response.getBody();
    }
}
