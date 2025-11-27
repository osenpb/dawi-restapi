package com.dawi.dawi_restapi.core.habitacion.dtos;

import com.dawi.dawi_restapi.core.tipoHabitacion.model.TipoHabitacion;

public record HabitacionResponse(
    Long id,
    String numero,
    String estado,
    Double precio,
    TipoHabitacion tipoHabitacion,
    Long hotelId

) {
}
