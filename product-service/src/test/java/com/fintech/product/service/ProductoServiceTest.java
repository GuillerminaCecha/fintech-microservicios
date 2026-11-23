package com.fintech.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fintech.product.dto.ProductoRequest;
import com.fintech.product.dto.ProductoResponse;
import com.fintech.product.dto.ProductoUpdateRequest;
import com.fintech.product.exception.ProductoNoEncontradoException;
import com.fintech.product.exception.RecursoDuplicadoException;
import com.fintech.product.mapper.ProductoMapper;
import com.fintech.product.model.EstadoProducto;
import com.fintech.product.model.Producto;
import com.fintech.product.model.TipoProducto;
import com.fintech.product.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    private ProductoService productoService;
    private Producto tarjeta;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository, Mappers.getMapper(ProductoMapper.class));
        tarjeta = new Producto();
        tarjeta.setId(1L);
        tarjeta.setNumero("TJ-1");
        tarjeta.setTipo(TipoProducto.TARJETA);
        tarjeta.setDescripcion("Visa");
        tarjeta.setMonto(new BigDecimal("5000"));
        tarjeta.setEstado(EstadoProducto.ACTIVO);
        tarjeta.setClienteId(7L);
    }

    @Test
    @DisplayName("crear guarda el producto si el número no existe")
    void crear_ok() {
        ProductoRequest request = new ProductoRequest(TipoProducto.CUENTA, "CA-1", "Caja", BigDecimal.TEN, 7L);
        when(productoRepository.existsByNumero("CA-1")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse respuesta = productoService.crear(request);

        assertEquals(TipoProducto.CUENTA, respuesta.tipo());
        assertEquals(7L, respuesta.clienteId());
    }

    @Test
    @DisplayName("crear con número duplicado lanza RecursoDuplicadoException y no guarda")
    void crear_numeroDuplicado() {
        ProductoRequest request = new ProductoRequest(TipoProducto.CUENTA, "CA-1", "Caja", BigDecimal.TEN, 7L);
        when(productoRepository.existsByNumero("CA-1")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorId inexistente lanza ProductoNoEncontradoException")
    void buscarPorId_inexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class, () -> productoService.buscarPorId(99L));
    }

    @Test
    @DisplayName("listarPorCliente devuelve los productos mapeados a DTO")
    void listarPorCliente() {
        when(productoRepository.findByClienteId(7L)).thenReturn(List.of(tarjeta));

        List<ProductoResponse> productos = productoService.listarPorCliente(7L);

        assertEquals(1, productos.size());
        assertEquals("TJ-1", productos.get(0).numero());
    }

    @Test
    @DisplayName("actualizar cambia descripción, monto y estado, pero no el número, tipo ni cliente")
    void actualizar() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(productoRepository.save(tarjeta)).thenReturn(tarjeta);

        ProductoResponse respuesta = productoService.actualizar(1L,
                new ProductoUpdateRequest("Visa Platinum", new BigDecimal("9000"), EstadoProducto.INACTIVO));

        assertEquals("Visa Platinum", respuesta.descripcion());
        assertEquals(EstadoProducto.INACTIVO, respuesta.estado());
        assertEquals("TJ-1", respuesta.numero());
        assertEquals(TipoProducto.TARJETA, respuesta.tipo());
        assertEquals(7L, respuesta.clienteId());
    }

    @Test
    @DisplayName("eliminar un producto inexistente lanza excepción y no borra nada")
    void eliminar_inexistente() {
        when(productoRepository.existsById(99L)).thenReturn(false);

        assertThrows(ProductoNoEncontradoException.class, () -> productoService.eliminar(99L));
        verify(productoRepository, never()).deleteById(any());
    }
}
