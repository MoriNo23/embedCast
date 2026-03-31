#!/bin/bash

# Este script envía archivos locales directamente a tu Smart TV para hot-reloading.
# Uso: ./deploy.sh [IP_DE_TU_TV] [ARCHIVO_A_SUBIR]
# Ejemplo: ./deploy.sh 10.42.0.224 app/src/main/assets/remote_guide.html

if [ "$#" -ne 2 ]; then
    echo "Uso: $0 <IP_TV> <archivo>"
    echo "Ejemplo: $0 10.42.0.224 app/src/main/assets/remote_guide.html"
    exit 1
fi

TV_IP=$1
FILE_PATH=$2
FILENAME=$(basename "$FILE_PATH")
PORT=8081

if [ ! -f "$FILE_PATH" ]; then
    echo "Error: El archivo $FILE_PATH no existe."
    exit 1
fi

echo "Desplegando $FILENAME en $TV_IP:$PORT..."

# Subir usando curl
curl -X POST "http://$TV_IP:$PORT/api/upload" \
     -F "file=@$FILE_PATH" \
     -F "filename=$FILENAME"

echo -e "\nDespliegue completado! El TV debería recargar automáticamente en 1-2 segundos."
