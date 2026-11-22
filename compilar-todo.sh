#!/bin/bash
# Compila y testea los 4 proyectos. Uso: ./compilar-todo.sh
cd "$(dirname "$0")"
for s in config-server eureka-server product-service customer-service; do
  echo ""
  echo "========== $s =========="
  (cd $s && ./mvnw -q test) && echo "✅ $s OK" || { echo "❌ $s FALLÓ"; exit 1; }
done
echo ""
echo "🎉 Los 4 proyectos compilan y sus tests pasan"
