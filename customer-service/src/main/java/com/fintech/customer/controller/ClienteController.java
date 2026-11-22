package com.fintech.customer.controller;

import com.fintech.customer.dto.ClienteConProductosResponse;
import com.fintech.customer.dto.ClienteRequest;
import com.fintech.customer.dto.ClienteResponse;
import com.fintech.customer.dto.ClienteUpdateRequest;
import com.fintech.customer.dto.MontoRequest;
import com.fintech.customer.dto.NuevoProductoRequest;
import com.fintech.customer.dto.ProductoResponse;
import com.fintech.customer.exception.ErrorRespuesta;
import com.fintech.customer.service.ClienteService;
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
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Gestión de clientes y sus productos financieros")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los clientes")
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar un cliente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "El cliente no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un cliente con ese documento",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar los datos personales de un cliente (el saldo no se modifica acá)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "El cliente no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Documento usado por otro cliente",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteUpdateRequest request) {
        return clienteService.actualizar(id, request);
    }

    @PostMapping("/{id}/depositar")
    @Operation(summary = "Depositar dinero en el saldo del cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito realizado"),
            @ApiResponse(responseCode = "404", description = "El cliente no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ClienteResponse depositar(@PathVariable Long id, @Valid @RequestBody MontoRequest request) {
        return clienteService.depositar(id, request.monto());
    }

    @PostMapping("/{id}/extraer")
    @Operation(summary = "Extraer dinero del saldo del cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Extracción realizada"),
            @ApiResponse(responseCode = "404", description = "El cliente no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ClienteResponse extraer(@PathVariable Long id, @Valid @RequestBody MontoRequest request) {
        return clienteService.extraer(id, request.monto());
    }

    @GetMapping("/{id}/productos")
    @Operation(summary = "Cliente con sus productos (consulta a product-service vía Feign)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente y productos"),
            @ApiResponse(responseCode = "404", description = "El cliente no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "503", description = "product-service no disponible",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ClienteConProductosResponse obtenerConProductos(@PathVariable Long id) {
        return clienteService.obtenerConProductos(id);
    }

    @PostMapping("/{id}/productos")
    @Operation(summary = "Asociar un nuevo producto al cliente (lo crea product-service vía Feign)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "El cliente no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Número de producto duplicado",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "503", description = "product-service no disponible",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<ProductoResponse> agregarProducto(@PathVariable Long id,
                                                            @Valid @RequestBody NuevoProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.agregarProducto(id, request));
    }
}
