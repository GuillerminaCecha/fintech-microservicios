package com.fintech.product.repository;

import com.fintech.product.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // SELECT * FROM productos WHERE cliente_id = ?
    List<Producto> findByClienteId(Long clienteId);

    boolean existsByNumero(String numero);
}
