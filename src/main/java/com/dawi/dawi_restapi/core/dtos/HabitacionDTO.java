package com.dawi.dawi_restapi.core.dtos;

import com.dawi.dawi_restapi.core.habitacion.models.TipoHabitacion;

public record HabitacionDTO(
    Long id,
    String numero,
    String estado,
    Double precio,
    TipoHabitacion tipoHabitacion,
    Long hotelId

) {
}
