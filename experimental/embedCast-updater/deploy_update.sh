#!/bin/bash

# Script de Despliegue para MovieON TV
# Este script automatiza la compilación y subida del APK al mini-servidor.

PROJECT_DIR="/home/fullmetal/Descargas/MovieON/tv-app"
UPDATER_DIR="/home/fullmetal/Descargas/MovieON/dev-updater"

echo "🔄 Compilando la app..."
cd $PROJECT_DIR
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Error al compilar la app. Abortando despliegue."
    exit 1
fi

echo "✅ Compilación exitosa!"

# Obtener versionCode y versionName de build.gradle.kts
VERSION_CODE=$(grep -A 5 "defaultConfig" $PROJECT_DIR/app/build.gradle.kts | grep "versionCode" | awk -F '=' '{print $2}' | tr -d ' ' | tr -d '\n')
VERSION_NAME=$(grep -A 5 "defaultConfig" $PROJECT_DIR/app/build.gradle.kts | grep "versionName" | awk -F '=' '{print $2}' | tr -d '"' | tr -d ' ' | tr -d '\n')

echo "📦 Versión compilada: $VERSION_CODE ($VERSION_NAME)"

# Crear el archivo version.json
cat <<EOF > $UPDATER_DIR/version.json
{
  "versionCode": $VERSION_CODE,
  "versionName": "$VERSION_NAME"
}
EOF

echo "📄 version.json actualizado en dev-updater."

# Copiar el APK al servidor de actualizaciones
APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "$UPDATER_DIR/app-debug.apk"
    echo "🚀 APK copiado exitosamente a dev-updater."
    echo "🌐 La próxima vez que inicie la TV, detectará la actualización $VERSION_CODE."
else
    echo "❌ No se encontró el APK en $APK_PATH. El proceso falló."
    exit 1
fi
