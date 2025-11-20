package com.dawi.dawi_restapi.auth.infraestructure.dtos;

public record AuthResponseDTO(
        UserResponseDTO userResponseDTO,
        String token
) {
}
