package com.fintech.product.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.fintech.product.model.EstadoProducto;
import com.fintech.product.model.Producto;
import com.fintech.product.model.TipoProducto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    private Producto guardar(String numero, Long clienteId) {
        Producto p = new Producto();
        p.setNumero(numero);
        p.setTipo(TipoProducto.CUENTA);
        p.setMonto(BigDecimal.TEN);
        p.setClienteId(clienteId);
        return productoRepository.save(p);
    }

    @Test
    void guardar_asignaEstadoActivoYFecha() {
        Producto guardado = guardar("CA-1", 1L);

        assertNotNull(guardado.getId());
        assertEquals(EstadoProducto.ACTIVO, guardado.getEstado());
        assertNotNull(guardado.getFechaAlta());
    }

    @Test
    void findByClienteId_devuelveSoloLosDelCliente() {
        guardar("CA-1", 1L);
        guardar("CA-2", 1L);
        guardar("CA-3", 2L);

        assertEquals(2, productoRepository.findByClienteId(1L).size());
        assertEquals(1, productoRepository.findByClienteId(2L).size());
        assertTrue(productoRepository.findByClienteId(3L).isEmpty());
    }

    @Test
    void existsByNumero() {
        guardar("CA-9", 1L);

        assertTrue(productoRepository.existsByNumero("CA-9"));
        assertFalse(productoRepository.existsByNumero("NO-EXISTE"));
    }
}
