package com.dawi.dawi_restapi.core.hotel.dtos;

import com.dawi.dawi_restapi.core.dtos.HabitacionDTO;
import com.dawi.dawi_restapi.core.habitacion.models.TipoHabitacion;
import com.dawi.dawi_restapi.core.hotel.models.Departamento;

import java.util.List;

public record HotelDTO(
        Long id,
        String nombre,
        String direccion,
        Departamento departamento,
        List<HabitacionDTO> habitaciones
) {
}
