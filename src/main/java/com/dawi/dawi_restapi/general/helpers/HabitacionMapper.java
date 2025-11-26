package com.dawi.dawi_restapi.general.helpers;

import com.dawi.dawi_restapi.core.dtos.HabitacionDTO;
import com.dawi.dawi_restapi.core.habitacion.models.Habitacion;

public class HabitacionMapper {

    public static HabitacionDTO toDTO(Habitacion habitacion) {
        return new HabitacionDTO(
                habitacion.getId(),
                habitacion.getNumero(),
                habitacion.getEstado(),
                habitacion.getPrecio(),
                habitacion.getTipoHabitacion(),
                habitacion.getHotel().getId()
        );
    }

}
