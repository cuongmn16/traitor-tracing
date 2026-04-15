package com.example.traitortracing.controller;

import com.example.traitortracing.dto.request.ImageRequest;
import com.example.traitortracing.dto.response.ApiResponse;
import com.example.traitortracing.dto.response.ImageResponse;
import com.example.traitortracing.entity.Images;

import com.example.traitortracing.service.ImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/imgages")
public class ImagesController {
    @Autowired
    private ImagesService imagesService;

    @PostMapping
    public ApiResponse<ImageResponse> create(@RequestBody ImageRequest imageRequest) {
        ApiResponse<ImageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(imagesService.create(imageRequest));
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<List<ImageResponse>> getAll() {
        ApiResponse<List<ImageResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(imagesService.getAll());
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<ImageResponse> getById(@PathVariable UUID id) {
        ApiResponse<ImageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(imagesService.getById(id));
        return apiResponse;
    }

    @PutMapping("/{id}")
    public ApiResponse<ImageResponse> update(@PathVariable UUID id, @RequestBody ImageRequest image) {
        ApiResponse<ImageResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(imagesService.update(id, image));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        imagesService.delete(id);
        apiResponse.setResult("Image deleted successfully");
        return apiResponse;
    }

}
