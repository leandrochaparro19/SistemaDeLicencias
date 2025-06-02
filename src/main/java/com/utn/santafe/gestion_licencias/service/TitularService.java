
package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TitularService {

    @Autowired
    private TitularRepository titularRepository;

    public Titular guardarTitular(Titular titular) {
        return titularRepository.save(titular);
    }

    public List<Titular> listarTitulares() {
        return titularRepository.findAll();
    }

    public Titular buscarPorId(Long id){
        return titularRepository.findById(id).orElse(null);
    }

    public void eliminarPorId(Long id){
        titularRepository.deleteById(id);
    }
}