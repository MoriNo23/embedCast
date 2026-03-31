#!/usr/bin/env python3
"""
Mini-Servidor de Actualizaciones para MovieON TV
Este servidor aloja el APK debug y el archivo version.json para que la TV
pueda verificar y descargar actualizaciones automáticamente.
"""

import http.server
import socketserver
import json
import os
import subprocess
from datetime import datetime

PORT = 8000
APK_DIR = os.path.dirname(os.path.abspath(__file__))
VERSION_FILE = os.path.join(APK_DIR, "version.json")


class UpdateHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/version.json":
            # Servir el archivo version.json
            if os.path.exists(VERSION_FILE):
                self.send_response(200)
                self.send_header("Content-type", "application/json")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                with open(VERSION_FILE, "r") as f:
                    self.wfile.write(f.read().encode())
            else:
                self.send_response(404)
                self.end_headers()
        elif self.path.endswith(".apk"):
            # Servir el APK
            apk_path = os.path.join(APK_DIR, "app-debug.apk")
            if os.path.exists(apk_path):
                self.send_response(200)
                self.send_header(
                    "Content-type", "application/vnd.android.package-archive"
                )
                self.send_header(
                    "Content-Disposition", f'attachment; filename="app-debug.apk"'
                )
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                with open(apk_path, "rb") as f:
                    self.wfile.write(f.read())
            else:
                self.send_response(404)
                self.end_headers()
        else:
            # Servir archivos estáticos (para index.html si lo agregamos después)
            super().do_GET()

    def log_message(self, format, *args):
        print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {args[0]}")


def main():
    print(f"🚀 Iniciando servidor de actualizaciones en el puerto {PORT}")
    print(f"📁 Directorio: {APK_DIR}")
    print(f"📡 IP del servidor: 10.42.0.1")
    print(
        f"📱 TV puede verificar actualizaciones en: http://10.42.0.1:8000/version.json"
    )
    print(f"📥 APK disponible en: http://10.42.0.1:8000/app-debug.apk")
    print(f"Ctrl+C para detener el servidor\n")

    with socketserver.TCPServer(("", PORT), UpdateHandler) as httpd:
        httpd.serve_forever()


if __name__ == "__main__":
    main()
