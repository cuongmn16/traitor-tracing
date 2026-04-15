package com.example.traitortracing.service;

import com.example.traitortracing.dto.request.ImageRequest;
import com.example.traitortracing.dto.response.ImageResponse;
import com.example.traitortracing.entity.Images;
import com.example.traitortracing.exception.AppException;
import com.example.traitortracing.exception.ErrorCode;
import com.example.traitortracing.mapper.ImageMapper;
import com.example.traitortracing.repository.ImagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImagesService {
    @Autowired
    private ImagesRepository imagesRepository;
    @Autowired
    private ImageMapper imageMapper;
    // CREATE
    public ImageResponse create(ImageRequest request) {
        Images image = new Images();
        image.setFileName(request.getFileName());
        image.setFilePath(request.getFilePath());
        image.setPhash(request.getPhash());
        image = imagesRepository.save(image);
        return  imageMapper.toImageResponse(image);
    }

    // READ ALL
    public List<ImageResponse> getAll() {

        return imagesRepository.findAll().stream().map(imageMapper::toImageResponse).collect(Collectors.toList());
    }

    // READ ONE
    public ImageResponse getById(UUID id) {
        return imagesRepository.findById(id).stream().map(imageMapper::toImageResponse).findFirst().orElse(null);

    }

    // UPDATE
    public ImageResponse update(UUID id, ImageRequest request) {
        Images image = imagesRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.IMAGE_NOT_EXISTED));
        imageMapper.toUpdateImages(image, request);
        var updatedImage = imagesRepository.save(image);

        return imageMapper.toImageResponse(updatedImage);
    }

    // DELETE
    public void delete(UUID id) {
        imagesRepository.deleteById(id);
    }
}
