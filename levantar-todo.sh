#!/bin/bash
# Levanta los 4 servicios en segundo plano, en orden. Los logs quedan en la carpeta logs/
# Uso:  ./levantar-todo.sh          (Config Server lee de GitHub)
#       ./levantar-todo.sh native   (Config Server lee la carpeta local config-repo)
cd "$(dirname "$0")"
mkdir -p logs

if [ -z "$DB_PASSWORD" ]; then
  read -s -p "Contraseña de MySQL: " DB_PASSWORD; echo
  export DB_PASSWORD
fi

PERFIL_CONFIG=""
[ "$1" == "native" ] && PERFIL_CONFIG="-Dspring-boot.run.profiles=native"

esperar() {  # espera a que un puerto responda
  echo -n "   esperando $1 en el puerto $2"
  for i in $(seq 1 90); do
    if curl -sf "http://localhost:$2/actuator/health" | grep -q "\"status\":\"UP\""; then echo " ✅"; return 0; fi
    echo -n "."; sleep 2
  done
  echo " ❌ no arrancó: revisá logs/$1.log"; exit 1
}

echo "1) config-server";   (cd config-server   && nohup ./mvnw -q spring-boot:run $PERFIL_CONFIG > ../logs/config-server.log 2>&1 &);   esperar config-server 8888
echo "2) eureka-server";   (cd eureka-server   && nohup ./mvnw -q spring-boot:run > ../logs/eureka-server.log 2>&1 &);                  esperar eureka-server 8761
echo "3) product-service"; (cd product-service && nohup ./mvnw -q spring-boot:run > ../logs/product-service.log 2>&1 &);                esperar product-service 8082
echo "4) customer-service";(cd customer-service && nohup ./mvnw -q spring-boot:run > ../logs/customer-service.log 2>&1 &);              esperar customer-service 8081

echo ""
echo "🎉 Todo levantado. Esperá ~30 segundos a que se registren en Eureka y abrí:"
echo "   Eureka:           http://localhost:8761"
echo "   Swagger clientes: http://localhost:8081/swagger-ui.html"
echo "   Swagger productos:http://localhost:8082/swagger-ui.html"
echo "Para apagar todo: ./detener-todo.sh"
