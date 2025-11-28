package com.dawi.dawi_restapi.core.departamento.dtos;

import com.dawi.dawi_restapi.core.habitacion.models.Habitacion;

import java.util.List;

public record DepartamentoResponse(
        Long id,
        Long departamentoId, // ?? luego veo xq hice esto xD
        String nombre,
        String direcicon,
        List<Habitacion> habitaciones
) {
}
