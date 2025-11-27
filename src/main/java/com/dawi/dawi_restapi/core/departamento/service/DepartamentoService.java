package com.dawi.dawi_restapi.core.departamento.service;

import com.dawi.dawi_restapi.core.departamento.model.Departamento;
import com.dawi.dawi_restapi.core.departamento.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public Optional<Departamento> buscarPorId(Long id) {
        return departamentoRepository.findById(id);
    }

    public List<Departamento> listar(){
        return departamentoRepository.findAll();
    }

    public Departamento guardar(Departamento departamento) {

        return departamentoRepository.save(departamento);
    }

    public void eliminar(Long depId) {
        departamentoRepository.deleteById(depId);
    }

    public Optional<Departamento> buscarPorNombre(String nombre) {
        return departamentoRepository.findByNombre(nombre);
    }

}
