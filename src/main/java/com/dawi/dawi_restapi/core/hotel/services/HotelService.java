package com.dawi.dawi_restapi.core.hotel.services;


import com.dawi.dawi_restapi.core.hotel.dtos.HotelDTO;
import com.dawi.dawi_restapi.core.hotel.dtos.HotelRequest;
import com.dawi.dawi_restapi.core.hotel.models.Departamento;
import com.dawi.dawi_restapi.core.hotel.models.Hotel;
import com.dawi.dawi_restapi.core.hotel.repositories.DepartamentoRepository;
import com.dawi.dawi_restapi.core.hotel.repositories.HotelRepository;
import com.dawi.dawi_restapi.general.helpers.HotelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final DepartamentoService departamentoService;

    public List<Hotel> listarPorDepartamentoId(Long departamentoId) {
        return hotelRepository.findByDepartamentoId(departamentoId);
    }

    public Hotel guardar(HotelRequest hotelRequest) {

        Departamento departamento = departamentoService
                .buscarPorId(hotelRequest.departamentoId())
                .orElseThrow(() -> new RuntimeException("Departamento no existe"));

        Hotel hotel = new Hotel();
        hotel.setId(null);
        hotel.setNombre(hotelRequest.nombre());
        hotel.setDireccion(hotelRequest.direccion());
        hotel.setDepartamento(departamento);

        return hotelRepository.save(hotel);
    }
    // ARREGLAR, PROBLEMA DE LOGICA CREO
    public Hotel actualizar(HotelRequest hotelRequest) {

        Departamento departamento = departamentoService
                .buscarPorId(hotelRequest.departamentoId())
                .orElseThrow(() -> new RuntimeException("Departamento no existe"));

        Hotel hotel = new Hotel();

        hotel.setNombre(hotelRequest.nombre());
        hotel.setDireccion(hotelRequest.direccion());
        hotel.setDepartamento(departamento);

        return hotelRepository.save(hotel);
    }

    public HotelDTO buscarPorId(Long id) {

        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Hotel no encontrado"));
        HotelDTO hotelDTO = HotelMapper.toDTO(hotel);

        return hotelDTO;

    }

    public void eliminar(Long id) {
        hotelRepository.deleteById(id);
    }

    public List<HotelDTO> listarHoteles() {

        List<Hotel> hoteles = hotelRepository.findAll();

        List<HotelDTO> hotelesDTO = hoteles.stream().map(HotelMapper::toDTO).toList();

        return hotelesDTO;
    }
}
