package com.example.traitortracing.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApiResponse<T> {
    private int code = 1000;
    private String message;
    private T result;
}
