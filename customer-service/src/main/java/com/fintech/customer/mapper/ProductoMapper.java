package com.fintech.customer.mapper;

import com.fintech.customer.dto.NuevoProductoRequest;
import com.fintech.customer.dto.ProductoRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Mapeo con MÚLTIPLES FUENTES (Clase 22): el body del usuario + el id de la URL
@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "tipo", source = "request.tipo")
    @Mapping(target = "numero", source = "request.numero")
    @Mapping(target = "descripcion", source = "request.descripcion")
    @Mapping(target = "monto", source = "request.monto")
    @Mapping(target = "clienteId", source = "clienteId")
    ProductoRequest toProductoRequest(NuevoProductoRequest request, Long clienteId);
}
