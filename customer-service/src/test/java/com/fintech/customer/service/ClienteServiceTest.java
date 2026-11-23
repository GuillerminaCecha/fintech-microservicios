package com.fintech.customer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fintech.customer.client.ProductClient;
import com.fintech.customer.dto.*;
import com.fintech.customer.exception.ClienteNoEncontradoException;
import com.fintech.customer.exception.RecursoDuplicadoException;
import com.fintech.customer.exception.SaldoInsuficienteException;
import com.fintech.customer.mapper.ClienteMapper;
import com.fintech.customer.mapper.ProductoMapper;
import com.fintech.customer.model.Cliente;
import com.fintech.customer.repository.ClienteRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Tests unitarios del servicio: el repositorio y el cliente Feign son mocks,
// los mappers son los reales generados por MapStruct
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductClient productClient;

    private ClienteService clienteService;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository,
                Mappers.getMapper(ClienteMapper.class),
                Mappers.getMapper(ProductoMapper.class),
                productClient);
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellido("García");
        cliente.setDocumento("30111222");
        cliente.setEmail("ana@mail.com");
        cliente.setSaldo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("crear guarda el cliente si el documento no existe")
    void crear_ok() {
        ClienteRequest request = new ClienteRequest("Ana", "García", "30111222", "ana@mail.com", new BigDecimal("500"));
        when(clienteRepository.existsByDocumento("30111222")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse respuesta = clienteService.crear(request);

        assertEquals("Ana", respuesta.nombre());
        assertEquals(0, new BigDecimal("500").compareTo(respuesta.saldo()));
    }

    @Test
    @DisplayName("crear con documento duplicado lanza RecursoDuplicadoException y no guarda")
    void crear_documentoDuplicado() {
        ClienteRequest request = new ClienteRequest("Ana", "García", "30111222", "ana@mail.com", BigDecimal.ZERO);
        when(clienteRepository.existsByDocumento("30111222")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> clienteService.crear(request));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorId inexistente lanza ClienteNoEncontradoException")
    void buscarPorId_inexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        ClienteNoEncontradoException ex = assertThrows(ClienteNoEncontradoException.class,
                () -> clienteService.buscarPorId(99L));
        assertEquals("No se encontró el cliente con id 99", ex.getMessage());
    }

    @Test
    @DisplayName("actualizar modifica los datos personales pero nunca el saldo")
    void actualizar_noModificaSaldo() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByDocumentoAndIdNot("30111222", 1L)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse respuesta = clienteService.actualizar(1L,
                new ClienteUpdateRequest("Ana María", "García", "30111222", "nuevo@mail.com"));

        assertEquals("Ana María", respuesta.nombre());
        assertEquals("nuevo@mail.com", respuesta.email());
        assertEquals(0, new BigDecimal("1000").compareTo(respuesta.saldo()));
    }

    @Test
    @DisplayName("actualizar con un documento de otro cliente lanza RecursoDuplicadoException")
    void actualizar_documentoDeOtroCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByDocumentoAndIdNot("40555666", 1L)).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> clienteService.actualizar(1L,
                new ClienteUpdateRequest("Ana", "García", "40555666", "ana@mail.com")));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("depositar suma el monto al saldo")
    void depositar() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        ClienteResponse respuesta = clienteService.depositar(1L, new BigDecimal("250.50"));

        assertEquals(0, new BigDecimal("1250.50").compareTo(respuesta.saldo()));
    }

    @Test
    @DisplayName("extraer resta el monto si el saldo alcanza")
    void extraer_ok() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        ClienteResponse respuesta = clienteService.extraer(1L, new BigDecimal("1000"));

        assertEquals(0, BigDecimal.ZERO.compareTo(respuesta.saldo()));
    }

    @Test
    @DisplayName("extraer sin saldo suficiente lanza SaldoInsuficienteException y no guarda")
    void extraer_saldoInsuficiente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(SaldoInsuficienteException.class,
                () -> clienteService.extraer(1L, new BigDecimal("1000.01")));
        assertEquals(0, new BigDecimal("1000").compareTo(cliente.getSaldo()));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerConProductos combina el cliente con los productos que devuelve Feign")
    void obtenerConProductos() {
        ProductoResponse tarjeta = new ProductoResponse(10L, "TJ-1", "TARJETA", "Visa", new BigDecimal("5000"),
                "ACTIVO", 1L, null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productClient.obtenerProductosPorCliente(1L)).thenReturn(List.of(tarjeta));

        ClienteConProductosResponse respuesta = clienteService.obtenerConProductos(1L);

        assertEquals("Ana", respuesta.cliente().nombre());
        assertEquals(1, respuesta.productos().size());
        assertEquals("TJ-1", respuesta.productos().get(0).numero());
    }

    @Test
    @DisplayName("obtenerConProductos de un cliente inexistente no llama a product-service")
    void obtenerConProductos_clienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNoEncontradoException.class, () -> clienteService.obtenerConProductos(99L));
        verifyNoInteractions(productClient);
    }

    @Test
    @DisplayName("agregarProducto envía a product-service el body con el clienteId de la URL")
    void agregarProducto_ok() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(productClient.crearProducto(any(ProductoRequest.class)))
                .thenReturn(new ProductoResponse(5L, "CA-1", "CUENTA", "Caja", BigDecimal.TEN, "ACTIVO", 1L, null));

        clienteService.agregarProducto(1L,
                new NuevoProductoRequest("CUENTA", "CA-1", "Caja", BigDecimal.TEN));

        ArgumentCaptor<ProductoRequest> captor = ArgumentCaptor.forClass(ProductoRequest.class);
        verify(productClient).crearProducto(captor.capture());
        assertEquals(1L, captor.getValue().clienteId());
        assertEquals("CUENTA", captor.getValue().tipo());
        assertEquals("CA-1", captor.getValue().numero());
    }

    @Test
    @DisplayName("agregarProducto a un cliente inexistente no llama a product-service")
    void agregarProducto_clienteInexistente() {
        when(clienteRepository.existsById(99L)).thenReturn(false);

        assertThrows(ClienteNoEncontradoException.class, () -> clienteService.agregarProducto(99L,
                new NuevoProductoRequest("CUENTA", "CA-1", "Caja", BigDecimal.TEN)));
        verifyNoInteractions(productClient);
    }
}
