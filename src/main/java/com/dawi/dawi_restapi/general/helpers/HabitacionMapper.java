package com.dawi.dawi_restapi.general.helpers;

import com.dawi.dawi_restapi.core.dtos.HabitacionDTO;
import com.dawi.dawi_restapi.core.habitacion.models.Habitacion;

public class HabitacionMapper {

    private HabitacionMapper() {
        throw new UnsupportedOperationException("Esta clase no debe ser instanciada");
    }

    public static HabitacionDTO toDTO(Habitacion habitacion) {
        if (habitacion == null) {
            return null;
        }

        return new HabitacionDTO(
                habitacion.getId(),
                habitacion.getNumero(),
                habitacion.getEstado(),
                habitacion.getPrecio(),
                habitacion.getTipoHabitacion(),
                habitacion.getHotel() != null ? habitacion.getHotel().getId() : null
        );
    }

    public static Habitacion toEntity(HabitacionDTO dto) {
        if (dto == null) {
            return null;
        }

        return Habitacion.builder()
                .id(dto.id())
                .numero(dto.numero())
                .estado(dto.estado())
                .precio(dto.precio())
                .tipoHabitacion(dto.tipoHabitacion())
                .build();
    }
}
