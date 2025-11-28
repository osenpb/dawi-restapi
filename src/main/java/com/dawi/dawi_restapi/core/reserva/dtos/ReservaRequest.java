package com.dawi.dawi_restapi.core.reserva.dtos;

import com.dawi.dawi_restapi.core.cliente.dtos.ClienteDTO;

import java.time.LocalDate;
import java.util.List;

public record ReservaRequest(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<Long> habitacionesIds,
        ClienteDTO cliente
) {
}
