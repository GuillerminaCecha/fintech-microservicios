package com.fintech.customer.mapper;

import com.fintech.customer.dto.ClienteRequest;
import com.fintech.customer.dto.ClienteResponse;
import com.fintech.customer.dto.ClienteUpdateRequest;
import com.fintech.customer.model.Cliente;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaAlta", ignore = true)
    Cliente toEntity(ClienteRequest request);

    ClienteResponse toResponse(Cliente cliente);

    List<ClienteResponse> toResponseList(List<Cliente> clientes);

    // El saldo se ignora explícitamente: no se modifica en una actualización
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "saldo", ignore = true)
    @Mapping(target = "fechaAlta", ignore = true)
    void actualizarEntidad(ClienteUpdateRequest request, @MappingTarget Cliente cliente);
}
