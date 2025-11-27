package com.dawi.dawi_restapi.core.habitacion.dtos;

public record HabitacionRequest(
                      // puede ser null para nuevas
        String numero,
        String estado,
        double precio,
        Long tipoHabitacionId
) {
}
