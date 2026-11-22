package com.fintech.product.mapper;

import com.fintech.product.dto.ProductoRequest;
import com.fintech.product.dto.ProductoResponse;
import com.fintech.product.dto.ProductoUpdateRequest;
import com.fintech.product.model.Producto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// MapStruct genera la implementación en tiempo de compilación (Clase 22)
@Mapper(componentModel = "spring")
public interface ProductoMapper {

    // DTO -> entidad: se ignoran los campos que genera el sistema
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaAlta", ignore = true)
    Producto toEntity(ProductoRequest request);

    // entidad -> DTO
    ProductoResponse toResponse(Producto producto);

    List<ProductoResponse> toResponseList(List<Producto> productos);

    // Actualiza una entidad existente con los datos permitidos
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "fechaAlta", ignore = true)
    void actualizarEntidad(ProductoUpdateRequest request, @MappingTarget Producto producto);
}
