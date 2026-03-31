# Tools

Herramientas auxiliares para el proyecto EmbedCast.

## EmbedCast Link Extractor

Userscript para Tampermonkey/Greasemonkey que extrae URLs de embeds de videos.

### ¿Qué hace?

- Detecta automáticamente iframes de reproductores de video en cualquier página
- Muestra un botón flotante para copiar la URL del iframe
- Útil para obtener links de embeds y usarlos en EmbedCast

### Instalación

1. Instala [Tampermonkey](https://www.tampermonkey.net/) en tu navegador
2. Abre el archivo `embedcast-link-extractor.user.js`
3. Tampermonkey detectará el script y te pedirá instalarlo
4. Haz clic en "Instalar"

### Uso

1. Navega a cualquier página que contenga un reproductor de video embebido
2. Aparecerá un botón rojo en la esquina inferior izquierda
3. Haz clic en "COPIAR SRC" para copiar la URL del embed
4. Pega la URL donde la necesites

### Archivos

| Archivo                              | Descripción                    |
| ------------------------------------ | ------------------------------ |
| `embedcast-link-extractor.user.js`   | Userscript principal           |
