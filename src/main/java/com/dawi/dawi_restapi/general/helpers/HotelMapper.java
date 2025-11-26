package com.dawi.dawi_restapi.general.helpers;

import com.dawi.dawi_restapi.core.hotel.dtos.HotelDTO;
import com.dawi.dawi_restapi.core.hotel.models.Hotel;

public class HotelMapper {

    public static HotelDTO toDTO(Hotel hotel) {
        return new HotelDTO(
                hotel.getId(),
                hotel.getNombre(),
                hotel.getDireccion(),
                hotel.getDepartamento(),
                hotel.getHabitaciones().stream().map(HabitacionMapper::toDTO).toList()
        );
    }

}
