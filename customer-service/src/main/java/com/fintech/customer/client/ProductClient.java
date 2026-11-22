package com.fintech.customer.client;

import com.fintech.customer.dto.ProductoRequest;
import com.fintech.customer.dto.ProductoResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Cliente declarativo (Clase 16): solo se escribe la interfaz y Spring genera la implementación.
// No hay "url": Feign le pide a Eureka las instancias de "product-service" (Clase 17)
// y Spring Cloud LoadBalancer elige una.
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/productos/cliente/{clienteId}")
    List<ProductoResponse> obtenerProductosPorCliente(@PathVariable("clienteId") Long clienteId);

    @PostMapping("/productos")
    ProductoResponse crearProducto(@RequestBody ProductoRequest request);
}
