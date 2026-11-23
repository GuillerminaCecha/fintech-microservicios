package com.fintech.customer.service;

import com.fintech.customer.client.ProductClient;
import com.fintech.customer.dto.ClienteConProductosResponse;
import com.fintech.customer.dto.ClienteRequest;
import com.fintech.customer.dto.ClienteResponse;
import com.fintech.customer.dto.ClienteUpdateRequest;
import com.fintech.customer.dto.NuevoProductoRequest;
import com.fintech.customer.dto.ProductoResponse;
import com.fintech.customer.exception.ClienteNoEncontradoException;
import com.fintech.customer.exception.RecursoDuplicadoException;
import com.fintech.customer.mapper.ClienteMapper;
import com.fintech.customer.mapper.ProductoMapper;
import com.fintech.customer.model.Cliente;
import com.fintech.customer.repository.ClienteRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final ProductoMapper productoMapper;
    private final ProductClient productClient;

    // Todas las dependencias son abstracciones inyectadas por constructor (SOLID - D)
    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper,
                          ProductoMapper productoMapper, ProductClient productClient) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.productoMapper = productoMapper;
        this.productClient = productClient;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteMapper.toResponseList(clienteRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return clienteMapper.toResponse(obtenerEntidad(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByDocumento(request.documento())) {
            throw new RecursoDuplicadoException("Ya existe un cliente con documento " + request.documento());
        }
        Cliente cliente = clienteMapper.toEntity(request);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteUpdateRequest request) {
        Cliente cliente = obtenerEntidad(id);
        if (clienteRepository.existsByDocumentoAndIdNot(request.documento(), id)) {
            throw new RecursoDuplicadoException("Ya existe otro cliente con documento " + request.documento());
        }
        clienteMapper.actualizarEntidad(request, cliente);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse depositar(Long id, BigDecimal monto) {
        Cliente cliente = obtenerEntidad(id);
        cliente.depositar(monto);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse extraer(Long id, BigDecimal monto) {
        Cliente cliente = obtenerEntidad(id);
        cliente.extraer(monto);   // lanza SaldoInsuficienteException si no alcanza
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    // Comunicación con product-service mediante Feign.
    // Sin @Transactional: no conviene tener una conexión a la base abierta durante una llamada HTTP
    public ClienteConProductosResponse obtenerConProductos(Long id) {
        ClienteResponse cliente = clienteMapper.toResponse(obtenerEntidad(id));
        List<ProductoResponse> productos = productClient.obtenerProductosPorCliente(id);
        return new ClienteConProductosResponse(cliente, productos);
    }

    // Valida que el cliente exista ACÁ y recién después le pide a product-service que cree el producto
    public ProductoResponse agregarProducto(Long id, NuevoProductoRequest request) {
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNoEncontradoException(id);
        }
        return productClient.crearProducto(productoMapper.toProductoRequest(request, id));
    }

    private Cliente obtenerEntidad(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }
}
