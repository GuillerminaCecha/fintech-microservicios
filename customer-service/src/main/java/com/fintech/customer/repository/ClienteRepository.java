package com.fintech.customer.repository;

import com.fintech.customer.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDocumento(String documento);

    // Para validar duplicados al actualizar, excluyendo al propio cliente
    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
