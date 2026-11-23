package com.fintech.customer.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.fintech.customer.model.Cliente;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// Test del repositorio contra una base H2 real en memoria.
// @Transactional deshace los cambios al terminar cada test.
@SpringBootTest
@Transactional
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente guardar(String documento) {
        Cliente c = new Cliente();
        c.setNombre("Ana");
        c.setApellido("García");
        c.setDocumento(documento);
        c.setEmail("ana@mail.com");
        c.setSaldo(new BigDecimal("100"));
        return clienteRepository.save(c);
    }

    @Test
    void guardar_asignaIdFechaYVersion() {
        Cliente guardado = guardar("111");

        assertNotNull(guardado.getId());
        assertNotNull(guardado.getFechaAlta());
        assertNotNull(guardado.getVersion());
    }

    @Test
    void existsByDocumento() {
        guardar("222");

        assertTrue(clienteRepository.existsByDocumento("222"));
        assertFalse(clienteRepository.existsByDocumento("999"));
    }

    @Test
    void existsByDocumentoAndIdNot_excluyeAlPropioCliente() {
        Cliente ana = guardar("333");
        Cliente otro = guardar("444");

        assertFalse(clienteRepository.existsByDocumentoAndIdNot("333", ana.getId()));
        assertTrue(clienteRepository.existsByDocumentoAndIdNot("333", otro.getId()));
    }
}
