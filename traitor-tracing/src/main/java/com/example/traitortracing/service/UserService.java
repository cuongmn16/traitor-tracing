package com.example.traitortracing.service;

import com.example.traitortracing.dto.request.AuthenticationRequest;
import com.example.traitortracing.dto.request.IntrospectRequest;
import com.example.traitortracing.dto.request.UserRequest;
import com.example.traitortracing.dto.response.AuthenticationResponse;
import com.example.traitortracing.dto.response.IntrospectResponse;
import com.example.traitortracing.dto.response.UserResponse;
import com.example.traitortracing.entity.Users;
import com.example.traitortracing.exception.AppException;
import com.example.traitortracing.exception.ErrorCode;
import com.example.traitortracing.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;
    @Autowired
    private WatermarkClientService watermarkClientService;

    public Users createUser(UserRequest userRegisterRequest) {
        if (userRepository.existsByUsername(userRegisterRequest.getUsername())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        Users user = new Users();
        user.setUsername(userRegisterRequest.getUsername());
        user.setPassword_hash(passwordEncoder.encode(userRegisterRequest.getPassword_hash()));
        user.setName(userRegisterRequest.getName());
        user.setRole(userRegisterRequest.getRole());

        // Tự động sinh mã fingerprint từ hệ thống Python (Tardos Code)
        String generatedFingerprint = watermarkClientService.generateFingerprint();
        user.setFingerprint_bits(generatedFingerprint);

        return userRepository.save(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.getUserByUsername(authenticationRequest.getUsername());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword_hash());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(user);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken(token);
        authenticationResponse.setAuthenticated(true);

        return authenticationResponse;
    }

    private String generateToken(Users user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("traitor_tracing")
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .issueTime(new Date())
                .claim("role", user.getRole())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        IntrospectResponse introspectResponse = new IntrospectResponse();
        introspectResponse.setValid(verified && expirationTime.after(new Date()));

        return introspectResponse;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .fingerprint_bits(user.getFingerprint_bits())
                .role(user.getRole())
                .created_at(user.getCreated_at()).build()).toList();
    }

    public UserResponse updateUser(UUID userId, UserRequest request) {
        Users user = userRepository.getUserById((userId));
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        user.setUsername(request.getUsername());
        user.setPassword_hash(request.getPassword_hash());
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setFingerprint_bits(request.getFingerprint_bits());

        return mapToUserResponse(userRepository.save(user));
    }

    private UserResponse mapToUserResponse(Users user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .fingerprint_bits(user.getFingerprint_bits())
                .role(user.getRole())
                .build();
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        Users user = userRepository.getUserByUsername(username);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .fingerprint_bits(user.getFingerprint_bits())
                .role(user.getRole())
                .build();
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

}
