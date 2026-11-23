#!/bin/bash
# Apaga los 4 servicios
for p in 8081 8082 8761 8888; do
  pid=$(lsof -ti:$p); [ -n "$pid" ] && kill $pid && echo "Apagado el puerto $p"
done
echo "Listo"
