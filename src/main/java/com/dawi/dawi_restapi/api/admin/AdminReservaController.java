package com.dawi.dawi_restapi.api.admin;


import com.dawi.dawi_restapi.core.cliente.model.Cliente;
import com.dawi.dawi_restapi.core.cliente.service.ClienteService;
import com.dawi.dawi_restapi.core.reserva.dtos.ReservaAdminUpdateDTO;
import com.dawi.dawi_restapi.core.habitacion.models.Habitacion;
import com.dawi.dawi_restapi.core.habitacion.service.HabitacionService;
import com.dawi.dawi_restapi.core.detalle_reserva.model.DetalleReserva;
import com.dawi.dawi_restapi.core.reserva.models.Reserva;
import com.dawi.dawi_restapi.core.reserva.services.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reservas")
@RequiredArgsConstructor
public class AdminReservaController {

    private final ReservaService reservaService;
    private final ClienteService clienteService;
    private final HabitacionService habitacionService;

    /**
     * Convertir Reserva a Map para evitar referencias circulares
     */
    private Map<String, Object> reservaToMap(Reserva reserva) {
        Map<String, Object> res = new HashMap<>();
        res.put("id", reserva.getId());
        res.put("fechaReserva", reserva.getFechaReserva() != null ? reserva.getFechaReserva().toString() : "");
        res.put("fechaInicio", reserva.getFechaInicio() != null ? reserva.getFechaInicio().toString() : "");
        res.put("fechaFin", reserva.getFechaFin() != null ? reserva.getFechaFin().toString() : "");
        res.put("total", reserva.getTotal());
        res.put("estado", reserva.getEstado());

        // Hotel simplificado
        if (reserva.getHotel() != null) {
            Map<String, Object> hotel = new HashMap<>();
            hotel.put("id", reserva.getHotel().getId());
            hotel.put("nombre", reserva.getHotel().getNombre());
            hotel.put("direccion", reserva.getHotel().getDireccion() != null ? reserva.getHotel().getDireccion() : "");
            if (reserva.getHotel().getDepartamento() != null) {
                hotel.put("departamento", Map.of(
                        "id", reserva.getHotel().getDepartamento().getId(),
                        "nombre", reserva.getHotel().getDepartamento().getNombre()
                ));
            }
            res.put("hotel", hotel);
        }

        // Cliente simplificado
        if (reserva.getCliente() != null) {
            res.put("cliente", Map.of(
                    "id", reserva.getCliente().getId(),
                    "nombre", reserva.getCliente().getNombre() != null ? reserva.getCliente().getNombre() : "",
                    "apellido", reserva.getCliente().getApellido() != null ? reserva.getCliente().getApellido() : "",
                    "email", reserva.getCliente().getEmail() != null ? reserva.getCliente().getEmail() : "",
                    "telefono", reserva.getCliente().getTelefono() != null ? reserva.getCliente().getTelefono() : "",
                    "documento", reserva.getCliente().getDni() != null ? reserva.getCliente().getDni() : ""
            ));
        }

        // Detalles simplificados
        if (reserva.getDetalles() != null) {
            List<Map<String, Object>> detalles = reserva.getDetalles().stream().map(det -> {
                Map<String, Object> detMap = new HashMap<>();
                detMap.put("id", det.getId());
                detMap.put("habitacionId", det.getHabitacion() != null ? det.getHabitacion().getId() : null);
                detMap.put("precioNoche", det.getPrecioNoche());
                return detMap;
            }).toList();
            res.put("detalles", detalles);
        }

        return res;
    }


    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorDni(@RequestParam String dni) {
        List<Reserva> reservas = reservaService.buscarReservasPorDniCliente(dni);
        List<Map<String, Object>> response = reservas.stream()
                .map(this::reservaToMap)
                .toList();
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<?> listar() {
        List<Reserva> reservas = reservaService.listar();
        List<Map<String, Object>> response = reservas.stream()
                .map(this::reservaToMap)
                .toList();
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.ok(Map.of("message", "Reserva eliminada correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return reservaService.buscarPorId(id)
                .map(reserva -> ResponseEntity.ok(reservaToMap(reserva)))
                .orElse(ResponseEntity.notFound().build());
    }




    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody ReservaAdminUpdateDTO dto) {

        Reserva reserva = reservaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Actualizar fechas y estado
        reserva.setFechaInicio(dto.fechaInicio());
        reserva.setFechaFin(dto.fechaFin());
        reserva.setEstado(dto.estado());

        // Actualizar hotel si cambió
        if (dto.hotelId() != null && !dto.hotelId().equals(reserva.getHotel().getId())) {
            Habitacion primeraHab = habitacionService.buscarPorId(dto.habitaciones().get(0))
                    .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));
            reserva.setHotel(primeraHab.getHotel());
        }

        // Actualizar cliente
        Cliente cliente = reserva.getCliente();
        cliente.setNombre(dto.cliente().nombre());
        cliente.setApellido(dto.cliente().apellido());
        cliente.setEmail(dto.cliente().email());
        cliente.setDni(dto.cliente().dni());
        if (dto.cliente().telefono() != null) {
            cliente.setTelefono(dto.cliente().telefono());
        }
        clienteService.guardar(cliente);

        // Limpiar detalles anteriores
        reserva.getDetalles().clear();

        // Calcular noches
        long noches = ChronoUnit.DAYS.between(dto.fechaInicio(), dto.fechaFin());
        if (noches <= 0) noches = 1;

        double total = 0;

        for (Long idHab : dto.habitaciones()) {
            Habitacion h = habitacionService.buscarPorId(idHab)
                    .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

            DetalleReserva det = new DetalleReserva();
            det.setReserva(reserva);
            det.setHabitacion(h);
            det.setPrecioNoche(h.getPrecio());
            reserva.getDetalles().add(det);
            total += h.getPrecio() * noches;
        }

        reserva.setTotal(total);
        Reserva saved = reservaService.guardar(reserva);
        return ResponseEntity.ok(reservaToMap(saved));
    }




}