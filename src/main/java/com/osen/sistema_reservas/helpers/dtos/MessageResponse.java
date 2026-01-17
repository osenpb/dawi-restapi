package com.osen.sistema_reservas.helpers.dtos;


public record MessageResponse(
        String message
) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
