
package com.utn.santafe.gestion_licencias.repository;

import com.utn.santafe.gestion_licencias.model.titular.Titular;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TitularRepository extends JpaRepository<Titular, Long> {

}