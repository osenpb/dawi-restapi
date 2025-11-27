package com.dawi.dawi_restapi.core.departamento.repository;

import com.dawi.dawi_restapi.core.departamento.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    Optional<Departamento> findByNombre(String nombre);
}