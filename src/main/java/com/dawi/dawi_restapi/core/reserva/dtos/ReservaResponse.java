package com.dawi.dawi_restapi.core.reserva.dtos;

import java.time.LocalDate;
import java.util.List;

public record ReservaResponse(
     Long id,
     LocalDate fechaInicio,
     LocalDate fechaFin,
     double total,
     String estado,
     List<String> habitaciones
) {
}
