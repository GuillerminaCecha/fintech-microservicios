package com.fintech.product.controller;

import com.fintech.product.dto.ProductoRequest;
import com.fintech.product.dto.ProductoResponse;
import com.fintech.product.dto.ProductoUpdateRequest;
import com.fintech.product.exception.ErrorRespuesta;
import com.fintech.product.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Productos financieros: cuentas, tarjetas, préstamos e inversiones")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos")
    public List<ProductoResponse> listar() {
        return productoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar un producto por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "El producto no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ProductoResponse buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar los productos de un cliente (lo consume customer-service vía Feign)")
    public List<ProductoResponse> listarPorCliente(@PathVariable Long clienteId) {
        return productoService.listarPorCliente(clienteId);
    }

    @PostMapping
    @Operation(summary = "Crear un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese número",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar descripción, monto y estado de un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "El producto no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoUpdateRequest request) {
        return productoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "El producto no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
