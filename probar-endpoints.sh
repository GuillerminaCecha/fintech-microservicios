#!/bin/bash
# Prueba rápida de punta a punta (requiere los 4 servicios levantados)
C=http://localhost:8081
P=http://localhost:8082
probar() {  # metodo url body descripcion
  echo ""
  echo "▶ $4"
  if [ -n "$3" ]; then
    curl -s -w "\n   → HTTP %{http_code}\n" -X $1 "$2" -H "Content-Type: application/json" -d "$3"
  else
    curl -s -w "\n   → HTTP %{http_code}\n" -X $1 "$2"
  fi
}
SUF=$RANDOM   # para no repetir documentos/números entre corridas
probar POST $C/clientes "{\"nombre\":\"Ana\",\"apellido\":\"García\",\"documento\":\"DNI$SUF\",\"email\":\"ana@mail.com\",\"saldo\":150000}" "Registrar cliente (esperado 201)"
ID=$(curl -s $C/clientes | grep -o '"id":[0-9]*' | tail -1 | cut -d: -f2)
echo "   (id del cliente creado: $ID)"
probar POST $C/clientes/$ID/productos "{\"tipo\":\"TARJETA\",\"numero\":\"TJ-$SUF\",\"descripcion\":\"Visa Gold\",\"monto\":500000}" "Feign: asociar tarjeta (esperado 201)"
probar POST $C/clientes/$ID/productos "{\"tipo\":\"CUENTA\",\"numero\":\"CA-$SUF\",\"descripcion\":\"Caja de ahorro\",\"monto\":80000}" "Feign: asociar cuenta (esperado 201)"
probar POST $C/clientes/$ID/productos "{\"tipo\":\"CUENTA\",\"numero\":\"CA-$SUF\",\"descripcion\":\"Repetida\",\"monto\":0}" "Feign: número duplicado (esperado 409)"
probar GET  $C/clientes/$ID/productos "" "Feign: cliente con sus productos (esperado 200)"
probar POST $C/clientes/$ID/extraer "{\"monto\":99999999}" "Extraer sin saldo (esperado 422)"
probar GET  $C/clientes/999999 "" "Cliente inexistente (esperado 404)"
probar GET  $P/productos/cliente/$ID "" "product-service directo (esperado 200)"
echo ""
