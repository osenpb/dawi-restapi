package com.dawi.dawi_restapi.api.publico;

import com.dawi.dawi_restapi.core.cliente.model.Cliente;
import com.dawi.dawi_restapi.core.cliente.service.ClienteService;
import com.dawi.dawi_restapi.core.habitacion.dtos.HabitacionResponse;
import com.dawi.dawi_restapi.core.reserva.dtos.ReservaRequestDTO;
import com.dawi.dawi_restapi.core.reserva.dtos.ReservaResponseDTO;
import com.dawi.dawi_restapi.core.habitacion.models.Habitacion;
import com.dawi.dawi_restapi.core.habitacion.repository.HabitacionRepository;
import com.dawi.dawi_restapi.core.habitacion.service.HabitacionService;
import com.dawi.dawi_restapi.core.departamento.model.Departamento;
import com.dawi.dawi_restapi.core.hotel.model.Hotel;
import com.dawi.dawi_restapi.core.hotel.repositories.HotelRepository;
import com.dawi.dawi_restapi.core.departamento.service.DepartamentoService;
import com.dawi.dawi_restapi.core.hotel.services.HotelService;
import com.dawi.dawi_restapi.core.reserva.models.DetalleReserva;
import com.dawi.dawi_restapi.core.reserva.models.Reserva;
import com.dawi.dawi_restapi.core.reserva.services.ReservaService;
import com.dawi.dawi_restapi.helpers.mappers.HabitacionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/reserva")
@RequiredArgsConstructor
public class ReservaController {

    private final DepartamentoService departamentoService;
    private final HotelService hotelService;
    private final HotelRepository hotelRepository; // Agregado para obtener Hotel entity
    private final ReservaService reservaService;
    private final HabitacionService habitacionService;
    private final HabitacionRepository habitacionRepository;
    private final ClienteService clienteService;

    /**
     * Listar todos los departamentos o buscar por nombre
     */
    @GetMapping("/departamentos")
    public ResponseEntity<?> listarDepartamentos(@RequestParam(required = false) String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            return departamentoService.buscarPorNombre(nombre)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.ok(List.of()));
        }
        return ResponseEntity.ok(departamentoService.listar());
    }

    /**
     * Obtener hoteles por departamento
     */
    @GetMapping("/hoteles")
    public ResponseEntity<?> verHoteles(@RequestParam Long depId) {
        Optional<Departamento> departamento = departamentoService.buscarPorId(depId);
        List<Hotel> hoteles = hotelService.listarPorDepartamentoId(depId);

        return ResponseEntity.ok(Map.of(
                "departamento", departamento.orElse(null),
                "hoteles", hoteles
        ));
    }

    /**
     * Obtener información de un hotel específico
     */
    @GetMapping("/hoteles/{id}")
    public ResponseEntity<?> infoHotel(@PathVariable Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel no encontrado"));

        return ResponseEntity.ok(Map.of(
                "hotel", Map.of(
                        "id", hotel.getId(),
                        "nombre", hotel.getNombre(),
                        "direccion", hotel.getDireccion(),
                        "precioMinimo", hotel.getPrecioMinimo(),
                        "cantidadHabitaciones", hotel.cantidadHabitaciones()
                ),
                "departamento", hotel.getDepartamento(),
                "habitaciones", hotel.getHabitaciones().stream()
                        .map(HabitacionMapper::toDTO)
                        .toList()
        ));
    }

    /**
     * Obtener habitaciones disponibles de un hotel para un rango de fechas
     */
    @GetMapping("/hoteles/{id}/habitaciones-disponibles")
    public ResponseEntity<?> habitacionesDisponibles(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel no encontrado"));

        List<HabitacionResponse> disponibles = hotel.getHabitaciones().stream()
                .filter(h -> "DISPONIBLE".equals(h.getEstado()))
                .filter(h -> habitacionService.estaDisponible(h, fechaInicio, fechaFin))
                .map(HabitacionMapper::toDTO)
                .toList();

        return ResponseEntity.ok(Map.of(
                "hotelId", id,
                "hotelNombre", hotel.getNombre(),
                "fechaInicio", fechaInicio,
                "fechaFin", fechaFin,
                "habitacionesDisponibles", disponibles,
                "cantidad", disponibles.size()
        ));
    }

    /**
     * Crear una reserva
     */
    @PostMapping("/hoteles/{id}/reservar")
    public ResponseEntity<?> reservar(
            @PathVariable Long id,
            @RequestBody @Valid ReservaRequestDTO dto) {

        // Obtener el hotel como entidad (no DTO)
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel no encontrado"));

        // Validar fechas
        LocalDate hoy = LocalDate.now();
        if (dto.fechaInicio().isBefore(hoy)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La fecha de inicio no puede ser anterior a hoy"
            ));
        }
        if (dto.fechaFin().isBefore(dto.fechaInicio())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La fecha de fin no puede ser anterior a la fecha de inicio"
            ));
        }
        if (dto.fechaInicio().isEqual(dto.fechaFin())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La reserva debe ser de al menos una noche"
            ));
        }

        // Validar que se seleccionó al menos una habitación
        if (dto.habitacionesIds() == null || dto.habitacionesIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Debe seleccionar al menos una habitación"
            ));
        }

        // Obtener habitaciones y validar disponibilidad
        List<String> noDisponibles = new ArrayList<>();
        List<Habitacion> habitaciones = new ArrayList<>();

        for (Long idHab : dto.habitacionesIds()) {
            Habitacion h = habitacionRepository.findById(idHab)
                    .orElseThrow(() -> new RuntimeException("Habitación no encontrada: " + idHab));

            // Verificar que la habitación pertenece al hotel
            if (!h.getHotel().getId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "La habitación " + h.getNumero() + " no pertenece a este hotel"
                ));
            }

            if (!habitacionService.estaDisponible(h, dto.fechaInicio(), dto.fechaFin())) {
                noDisponibles.add(h.getNumero() + " (" + h.getTipoHabitacion().getNombre() + ")");
            } else {
                habitaciones.add(h);
            }
        }

        if (!noDisponibles.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Las siguientes habitaciones no están disponibles para las fechas seleccionadas",
                    "habitaciones", noDisponibles
            ));
        }

        // Crear o buscar cliente
        Cliente cli = new Cliente();
        cli.setNombre(dto.cliente().nombre());
        cli.setApellido(dto.cliente().apellido());
        cli.setEmail(dto.cliente().correo());
        cli.setDni(dto.cliente().dni());
        cli = clienteService.guardar(cli);

        // Calcular número de noches y total
        long noches = java.time.temporal.ChronoUnit.DAYS.between(dto.fechaInicio(), dto.fechaFin());

        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cli);
        reserva.setHotel(hotel);
        reserva.setFechaInicio(dto.fechaInicio());
        reserva.setFechaFin(dto.fechaFin());
        reserva.setFechaReserva(LocalDate.now());
        reserva.setEstado("CONFIRMADA");

        // Crear detalles y calcular total
        List<DetalleReserva> detalles = new ArrayList<>();
        double total = 0;

        for (Habitacion h : habitaciones) {
            DetalleReserva det = new DetalleReserva();
            det.setHabitacion(h);
            det.setReserva(reserva);
            det.setPrecioNoche(h.getPrecio());
            detalles.add(det);
            total += h.getPrecio() * noches; // Precio por noche * número de noches
        }

        reserva.setTotal(total);
        reserva.setDetalles(detalles);
        reservaService.guardar(reserva);

        // Construir respuesta
        ReservaResponseDTO response = new ReservaResponseDTO(
                reserva.getId(),
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                reserva.getTotal(),
                reserva.getEstado(),
                habitaciones.stream()
                        .map(h -> h.getNumero() + " - " + h.getTipoHabitacion().getNombre())
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener detalles de una reserva (para página de confirmación)
     */
    @GetMapping("/reserva/{id}")
    public ResponseEntity<?> obtenerReserva(@PathVariable Long id) {
        Reserva reserva = reservaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        return ResponseEntity.ok(Map.of(
                "id", reserva.getId(),
                "fechaReserva", reserva.getFechaReserva(),
                "fechaInicio", reserva.getFechaInicio(),
                "fechaFin", reserva.getFechaFin(),
                "estado", reserva.getEstado(),
                "total", reserva.getTotal(),
                "cliente", Map.of(
                        "nombre", reserva.getCliente().getNombre(),
                        "apellido", reserva.getCliente().getApellido(),
                        "dni", reserva.getCliente().getDni(),
                        "email", reserva.getCliente().getEmail()
                ),
                "hotel", Map.of(
                        "id", reserva.getHotel().getId(),
                        "nombre", reserva.getHotel().getNombre(),
                        "direccion", reserva.getHotel().getDireccion()
                ),
                "habitaciones", reserva.getDetalles().stream()
                        .map(d -> Map.of(
                                "numero", d.getHabitacion().getNumero(),
                                "tipo", d.getHabitacion().getTipoHabitacion().getNombre(),
                                "precioNoche", d.getPrecioNoche()
                        ))
                        .toList()
        ));
    }

    /**
     * Buscar reservas por DNI del cliente
     */
    @GetMapping("/mis-reservas")
    public ResponseEntity<?> buscarMisReservas(@RequestParam String dni) {
        List<Reserva> reservas = reservaService.buscarReservasPorDniCliente(dni);

        if (reservas.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "No se encontraron reservas para el DNI: " + dni,
                    "reservas", List.of()
            ));
        }

        List<Map<String, Object>> reservasDTO = reservas.stream()
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "fechaReserva", r.getFechaReserva(),
                        "fechaInicio", r.getFechaInicio(),
                        "fechaFin", r.getFechaFin(),
                        "estado", r.getEstado(),
                        "total", r.getTotal(),
                        "hotel", r.getHotel().getNombre(),
                        "habitaciones", r.getDetalles().size()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "dni", dni,
                "reservas", reservasDTO
        ));
    }
}