package com.dawi.dawi_restapi.core.reserva.services;

import com.dawi.dawi_restapi.core.cliente.model.Cliente;
import com.dawi.dawi_restapi.core.cliente.service.ClienteService;
import com.dawi.dawi_restapi.core.habitacion.models.Habitacion;
import com.dawi.dawi_restapi.core.hotel.dtos.HotelResponse;
import com.dawi.dawi_restapi.core.hotel.model.Hotel;
import com.dawi.dawi_restapi.core.hotel.services.HotelService;
import com.dawi.dawi_restapi.core.reserva.dtos.ReservaRequest;
import com.dawi.dawi_restapi.core.reserva.dtos.ReservaResponse;
import com.dawi.dawi_restapi.core.reserva.models.Reserva;
import com.dawi.dawi_restapi.core.reserva.repositories.ReservaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;

    private final ClienteService clienteService;

    private final HotelService hotelService;

    public Reserva guardar(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public void eliminar(Long id) {
        reservaRepository.deleteById(id);
    }

    public List<Reserva> buscarReservasPorDniCliente(String dni){
        return reservaRepository.findByClienteDni(dni);
    }

//    @Transactional
//    public ReservaResponse reservarHabitaciones(Long hotelId, ReservaRequest dto) {
//
//        //validarFechas(dto.fechaInicio(), dto.fechaFin());
//        HotelResponse hotel = hotelService.buscarPorId(hotelId);
//
//        List<Habitacion> habitaciones = validarYObtenerHabitaciones(hotel, dto.habitacionesIds(), dto.fechaInicio(), dto.fechaFin());
//
//        Cliente cliente = clienteService.crearOCrearCliente(dto.cliente());
//
//        long noches = ChronoUnit.DAYS.between(dto.fechaInicio(), dto.fechaFin());
//        double total = calcularTotal(habitaciones, noches);
//
//        Reserva reserva = this.guardar(hotel, cliente, dto.fechaInicio(), dto.fechaFin(), habitaciones, total);
//        this.guardar(reserva);
//
//        return mapToResponse(reserva, habitaciones);
//    }

    // Métodos privados: validarFechas, validarYObtenerHabitaciones, calcularTotal, crearReserva, mapToResponse...
}


