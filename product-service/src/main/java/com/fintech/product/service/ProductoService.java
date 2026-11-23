package com.fintech.product.service;

import com.fintech.product.dto.ProductoRequest;
import com.fintech.product.dto.ProductoResponse;
import com.fintech.product.dto.ProductoUpdateRequest;
import com.fintech.product.exception.ProductoNoEncontradoException;
import com.fintech.product.exception.RecursoDuplicadoException;
import com.fintech.product.mapper.ProductoMapper;
import com.fintech.product.model.Producto;
import com.fintech.product.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    // Inyección de dependencias por constructor (inversión de dependencias, SOLID)
    public ProductoService(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoMapper.toResponseList(productoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) {
        return productoMapper.toResponse(obtenerEntidad(id));
    }

    // Endpoint que consume customer-service mediante Feign
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCliente(Long clienteId) {
        return productoMapper.toResponseList(productoRepository.findByClienteId(clienteId));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (productoRepository.existsByNumero(request.numero())) {
            throw new RecursoDuplicadoException("Ya existe un producto con número " + request.numero());
        }
        Producto producto = productoMapper.toEntity(request);
        return productoMapper.toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoUpdateRequest request) {
        Producto producto = obtenerEntidad(id);
        productoMapper.actualizarEntidad(request, producto);
        return productoMapper.toResponse(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ProductoNoEncontradoException(id);
        }
        productoRepository.deleteById(id);
    }

    private Producto obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }
}
