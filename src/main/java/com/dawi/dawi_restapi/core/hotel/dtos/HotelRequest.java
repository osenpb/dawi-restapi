package com.dawi.dawi_restapi.core.hotel.dtos;

public record HotelRequest(

        String nombre,
        String direccion,
        Long departamentoId

        ) {

}
