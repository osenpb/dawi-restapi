package com.dawi.dawi_restapi.controllers.publico;

import com.dawi.dawi_restapi.core.dtos.HabitacionDisponibilidadDTO;
import com.dawi.dawi_restapi.core.habitacion.models.TipoHabitacion;
import com.dawi.dawi_restapi.core.habitacion.services.HabitacionService;

import com.dawi.dawi_restapi.core.habitacion.services.TipoHabitacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public/habitaciones")
@RequiredArgsConstructor
public class HabitacionConsultaController {

    private final HabitacionService habitacionService;
    private final TipoHabitacionService tipoHabitacionService;

    @GetMapping("/disponibles")
    public HabitacionDisponibilidadDTO verificarDisponibilidad(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        int cantidadDisponible = habitacionService.obtenerCantidadDisponible(hotelId, fechaInicio, fechaFin);

        return new HabitacionDisponibilidadDTO(cantidadDisponible > 0, cantidadDisponible);
    }

    @GetMapping("/tipos")
    public List<TipoHabitacion> listadoTipoHabitaciones() {
        return tipoHabitacionService.listar();
    }


}