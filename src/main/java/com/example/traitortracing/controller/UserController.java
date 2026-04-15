package com.example.traitortracing.controller;

import com.example.traitortracing.dto.request.AuthenticationRequest;
import com.example.traitortracing.dto.request.IntrospectRequest;
import com.example.traitortracing.dto.request.UserRequest;
import com.example.traitortracing.dto.response.ApiResponse;
import com.example.traitortracing.dto.response.AuthenticationResponse;
import com.example.traitortracing.dto.response.IntrospectResponse;
import com.example.traitortracing.dto.response.UserResponse;
import com.example.traitortracing.entity.Users;
import com.example.traitortracing.service.UserService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ApiResponse<Users> createUser(@RequestBody UserRequest user) {
        ApiResponse<Users> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(user));
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getAllUsers());
        return apiResponse;
    }

    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> getMyInfo() {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getMyInfo());
        return apiResponse;
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<UserResponse> deleteUser(@PathVariable UUID userId) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        userService.deleteUser(userId);
        apiResponse.setMessage("User deleted successfully");
        return apiResponse;
    }

    @PostMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable UUID userId, @RequestBody UserRequest userUpdateRequest) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateUser(userId, userUpdateRequest));
        return apiResponse;
    }

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var result = userService.authenticate(request);

        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);
        apiResponse.setMessage("login successful!");
        return apiResponse;
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = userService.introspect(request);
        ApiResponse<IntrospectResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);
        return apiResponse;
    }

}
