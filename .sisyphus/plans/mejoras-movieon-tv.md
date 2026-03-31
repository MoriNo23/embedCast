# Plan de Mejoras - MovieON TV Android App

## TL;DR

> **Resumen**: Plan de refactorización y mejoras para la app Android MovieON TV. Incluye extracción de clases manager, mejora en manejo de errores, y actualización de nombres.
> 
> **Entregables**: 
> - VideoPlayerManager, WebSocketManager, PreferencesManager
> - MainActivity refactorizado (<500 líneas)
> - Unit tests para managers
> - Documentación del protocolo WebSocket
> - App renombrada si se decide
> 
> **Esfuerzo Estimado**: Large
> **Ejecución Paralela**: YES - 5 waves
> **Ruta Crítica**: Task 1 → Task 2 → Task 5 → Task 6

---

## Contexto

### Solicitud Original
El usuario solicitó:
1. Verificar que la app funciona correctamente en la TV
2. Proponer mejoras de organización
3. Sugerir cambio de nombre del proyecto (actualmente "MovieON TV", sugerencias: "playerCast TV", "WrapperCast TV", "CastOn TV")

### Nombre Elegido
**EmbedCast** - El usuario ha confirmado este nombre para el proyecto.

### Estado Actual del Código
- Kotlin compila correctamente (BUILD SUCCESSFUL)
- MainActivity.kt tiene 911 líneas (muy grande)
- Funcionalidades implementadas:
  - WebSocket server en puerto 8080
  - Control de video via JWPlayer API
  - Auto-resume después de reload
  - Cambio de calidad (teclas 1-9)
  - Subtítulos
  - Seek con teclas direccionales
  - Red = reload, Blue = stop
- Versión actual: 1.0 (versionCode = 1)

### Análisis de Metis
**Issues identificados**:
1. MainActivity muy grande (911 líneas) con responsabilidades mezcladas
2. Manejo de errores limitado
3. Valores hardcoded (UPDATE_SERVER_URL, puerto WebSocket)
4. Posibles memory leaks en WebView
5. Falta de tests unitarios
6. Naming inconsistente (paquete: `com.tvremote.control`, app: "MovieON TV")

---

## Objetivos del Trabajo

### Objetivo Principal
Mejorar la organización del código, mantenibilidad y testabilidad de la app MovieON TV sin romper funcionalidad existente.

### Entregables Concretos
- [ ] Documento WEBSOCKET_PROTOCOL.md con el protocolo actual
- [ ] VideoPlayerManager.kt - clase para control de video
- [ ] WebSocketManager.kt - clase para WebSocket
- [ ] PreferencesManager.kt - clase para SharedPreferences
- [ ] MainActivity refactorizado (<500 líneas)
- [ ] Tests unitarios para los 3 managers
- [ ] Mejor manejo de errores y logging
- [ ] Constantes extraídas
- [ ] App renombrada a "EmbedCast"
- [ ] Logo estático integrado como icono de la app
- [ ] Splash screen con animación de loading basada en el logo

### Definición de Completado
- [ ] BUILD SUCCESSFUL después de cada task
- [ ] Todas las funcionalidades originales funcionan igual
- [ ] WebSocket protocol mantiene backward compatibility
- [ ] MainActivity < 500 líneas
- [ ] App se llama "EmbedCast" con logo personalizado
- [ ] Splash screen con animación de loading funcional

### Debe Tener
- Compatibilidad hacia atrás con el protocolo WebSocket existente
- Ninguna funcionalidad existente rota
- El remote control debe seguir funcionando igual
- Logo de la app correctamente mostrado en launcher
- Splash screen con animación al iniciar

### NO Debe Tener (Guardrails)
- No cambiar el protocolo WebSocket (breaking change)
- No introducir security vulnerabilities
- No reducir test coverage si hay tests existentes
- No romper la experiencia de usuario existente

---

## Estrategia de Verificación

> **VERIFICACIÓN SIN INTERVENCIÓN HUMANA** - Toda verificación es ejecutada por agente. No se permiten criterios que requieran "el usuario manualmente prueba/confirma".

### Test Decision
- **Infraestructura existe**: NO (no hay tests en el proyecto)
- **Automated tests**: NO (no se agregarán en este plan)
- **Framework**: N/A
- **Agent-Executed QA**: Siempre - cada task debe tener QA scenarios

### QA Policy
Cada task debe incluir agent-executed QA scenarios. Evidencia guardada en `.sisyphus/evidence/`.

---

## Estrategia de Ejecución

### Olas de Ejecución Paralela

```
Ola 1 (Start Immediately — sin dependencias):
├── Task 1: Analizar y documentar protocolo WebSocket
├── Task 4: Crear PreferencesManager class
└── Task 10: Actualizar nombre de app (si se decide)

Ola 2 (Después de Ola 1):
├── Task 2: Crear VideoPlayerManager class (depends: 1)
├── Task 3: Crear WebSocketManager class (depends: 1)
└── Task 8: Agregar unit tests para managers (depends: 2,3,4)

Ola 3 (Después de Ola 2):
├── Task 5: Refactorizar MainActivity (depends: 2,3,4)
└── Task 9: Verificar compatibilidad WebSocket (depends: 1,5,6)

Ola 4 (Después de Ola 3):
├── Task 6: Mejorar manejo de errores y logging (depends: 5)
└── Task 7: Actualizar naming y constantes (depends: 5)

Ola 5 (Después de Ola 4):
└── (Todas las tasks completadas)
```

### Ruta Crítica
Task 1 → Task 2 → Task 5 → Task 6

---

## TODOs

- [x] 1. **Analizar y documentar protocolo WebSocket**

  **Qué hacer**:
  - Leer TvWebSocketServer.kt y MainActivity.kt
  - Documentar estructura de mensajes JSON
  - Documentar acciones soportadas: load, play, pause, stop, seek, quality, reload
  - Documentar formato de status messages
  - Crear archivo WEBSOCKET_PROTOCOL.md

  **No debe hacer**:
  - No modificar código existente
  - No cambiar protocolo

  **Agente recomendado**: deep
  - Razón: Requiere análisis profundo del código existente
  
  **Paralización**:
  - Puede ejecutarse en paralelo: SÍ
  - Grupo: Ola 1 (con Tasks 4, 10)
  - Bloquea: Task 2, 3, 9

  **Referencias**:
  - `tv-app/app/src/main/java/com/tvremote/control/TvWebSocketServer.kt` - Implementación WebSocket
  - `tv-app/app/src/main/java/com/tvremote/control/MainActivity.kt:handleCommand` - Comandos soportados

  **Criterios de aceptación**:
  - [ ] WEBSOCKET_PROTOCOL.md creado
  - [ ] Todas las acciones documentadas
  - [ ] Formato de mensajes documentado

  **QA Scenarios**:
  ```
  Scenario: Verificar documentación del protocolo
    Tool: Bash
    Preconditions: Ninguna
    Steps:
      1. Verificar que WEBSOCKET_PROTOCOL.md existe
      2. Verificar que contiene todas las acciones
    Expected Result: Archivo existe y tiene contenido completo
    Evidence: .sisyphus/evidence/task-1-protocol-doc.md
  ```

  **Commit**: NO

- [x] 2. **Crear VideoPlayerManager class**

  **Qué hacer**:
  - Extraer lógica de control de video de MainActivity
  - Crear métodos: playPause(), seek(), seekPercent(), setQuality(), toggleSubtitles(), resumeVideo(), resetPlayer()
  - Mantener compatibilidad con JWPlayer API

  **No debe hacer**:
  - No cambiar funcionalidad existente
  - No romper compatibilidad con JWPlayer

  **Agente recomendado**: ultrabrain
  - Razón: Lógica compleja de video
  
  **Paralización**:
  - Puede ejecutarse en paralelo: NO
  - Grupo: Ola 2 (con Tasks 3, 8)
  - Bloquea: Task 5
  - Bloqueado por: Task 1

  **Referencias**:
  - `tv-app/app/src/main/java/com/tvremote/control/MainActivity.kt:controlVideo` - Lógica a extraer
  - `tv-app/app/src/main/java/com/tvremote/control/VideoWebViewClient.kt` - JWPlayer integration

  **Criterios de aceptación**:
  - [ ] VideoPlayerManager.kt creado
  - [ ] Todos los métodos implementados
  - [ ] BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: Verificar compilación de VideoPlayerManager
    Tool: Bash
    Preconditions: Ninguna
    Steps:
      1. cd /home/fullmetal/Descargas/MovieON/tv-app
      2. ./gradlew compileDebugKotlin
    Expected Result: BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-2-build.log
  ```

  **Commit**: SÍ
  - Mensaje: `feat: add VideoPlayerManager to encapsulate video control logic`
  - Archivos: `VideoPlayerManager.kt`

- [x] 3. **Crear WebSocketManager class**

  **Qué hacer**:
  - Extraer lógica de WebSocket de TvWebSocketServer y MainActivity
  - Crear métodos: startServer(), stopServer(), sendStatus()
  - Mantener compatibilidad con TvWebSocketServer existente

  **No debe hacer**:
  - No cambiar el protocolo
  - No romper backward compatibility

  **Agente recomendado**: deep
  - Razón: Requiere investigación de WebSocket best practices
  
  **Paralización**:
  - Puede ejecutarse en paralelo: NO
  - Grupo: Ola 2 (con Tasks 2, 8)
  - Bloquea: Task 5
  - Bloqueado por: Task 1

  **Referencias**:
  - `tv-app/app/src/main/java/com/tvremote/control/TvWebSocketServer.kt` - Implementación actual
  - `tv-app/app/src/main/java/com/tvremote/control/MainActivity.kt:startServer` - Inicialización

  **Criterios de aceptación**:
  - [x] WebSocketManager.kt creado
  - [x] Mantiene compatibilidad con TvWebSocketServer interface
  - [x] BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: Verificar compilación de WebSocketManager
    Tool: Bash
    Preconditions: Ninguna
    Steps:
      1. cd /home/fullmetal/Descargas/MovieON/tv-app
      2. ./gradlew compileDebugKotlin
    Expected Result: BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-3-build.log
  ```

  **Commit**: SÍ
  - Mensaje: `feat: add WebSocketManager to encapsulate WebSocket communication`
  - Archivos: `WebSocketManager.kt`

- [x] 4. **Crear PreferencesManager class**

  **Qué hacer**:
  - Crear utilidad para SharedPreferences
  - Métodos: saveProgress(), loadProgress(), clearProgress()
  - Manejar sanitización de URLs para keys

  **No debe hacer**:
  - No cambiar formato de guardado
  - No perder datos existentes

  **Agente recomendado**: quick
  - Razón: Clase utilitaria simple
  
  **Paralización**:
  - Puede ejecutarse en paralelo: SÍ
  - Grupo: Ola 1 (con Tasks 1, 10)
  - Bloquea: Task 5, 8
  - Bloqueado por: Ninguno

  **Referencias**:
  - `tv-app/app/src/main/java/com/tvremote/control/MainActivity.kt:saveProgress` - Lógica actual
  - `tv-app/app/src/main/java/com/tvremote/control/MainActivity.kt:loadProgress` - Lógica actual

  **Criterios de aceptación**:
  - [x] PreferencesManager.kt creado
  - [x] Métodos funcionan igual que antes
  - [x] BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: Verificar compilación de PreferencesManager
    Tool: Bash
    Preconditions: Ninguna
    Steps:
      1. cd /home/fullmetal/Descargas/MovieON/tv-app
      2. ./gradlew compileDebugKotlin
    Expected Result: BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-4-build.log
  ```

  **Commit**: SÍ
  - Mensaje: `feat: add PreferencesManager for handling video progress persistence`
  - Archivos: `PreferencesManager.kt`

- [x] 5. **Refactorizar MainActivity para usar managers**

  **Qué hacer**:
  - Reemplazar llamadas directas con delegación a managers
  - Reducir tamaño de MainActivity a <500 líneas
  - Mantener toda funcionalidad existente

  **No debe hacer**:
  - No cambiar comportamiento existente
  - No romper compatibilidad WebSocket

  **Agente recomendado**: deep
  - Razón: Refactorización compleja
  
  **Paralización**:
  - Puede ejecutarse en paralelo: NO
  - Grupo: Ola 3 (con Task 9)
  - Bloquea: Tasks 6, 7
  - Bloqueado por: Tasks 2, 3, 4

  **Criterios de aceptación**:
  - [ ] MainActivity < 500 líneas
  - [ ] BUILD SUCCESSFUL
  - [ ] Funcionalidad intacta

  **QA Scenarios**:
  ```
  Scenario: Verificar compilación después de refactor
    Tool: Bash
    Preconditions: Tasks 2, 3, 4 completadas
    Steps:
      1. cd /home/fullmetal/Descargas/MovieON/tv-app
      2. ./gradlew compileDebugKotlin
    Expected Result: BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-5-build.log
  ```

  **Commit**: SÍ
  - Mensaje: `refactor: refactor MainActivity to use manager classes`
  - Archivos: `MainActivity.kt`

- [x] 6. **Mejorar manejo de errores y logging**

  **Qué hacer**:
  - Reemplazar sendLog con Log proper (Log.d, Log.i, Log.w, Log.e)
  - Agregar retry mechanisms para WebSocket
  - Mejorar error handling en JavaScript bridge

  **No debe hacer**:
  - No cambiar lógica de negocio
  - No introducir nuevos bugs

  **Agente recomendado**: deep
  - Razón: Requiere análisis de manejo de errores
  
  **Paralización**:
  - Puede ejecutarse en paralelo: NO
  - Grupo: Ola 4 (con Task 7)
  - Bloquea: Task 9
  - Bloqueado por: Task 5

  **Criterios de aceptación**:
  - [ ] Logging mejorado
  - [ ] BUILD SUCCESSFUL

  **Commit**: SÍ
  - Mensaje: `feat: improve error handling and logging`

- [x] 7. **Actualizar naming y constantes**

  **Qué hacer**:
  - Extraer valores hardcoded a constantes
  - Mejorar naming de métodos/variables
  - Actualizar nombre de app si se decide

  **No debe hacer**:
  - No romper funcionalidad

  **Agente recomendado**: quick
  - Razón: Renaming straightforward
  
  **Paralización**:
  - Puede ejecutarse en paralelo: NO
  - Grupo: Ola 4 (con Task 6)
  - Bloquea: Ninguno
  - Bloqueado por: Task 5

  **Criterios de aceptación**:
  - [ ] Constantes extraídas
  - [ ] App renombrada si se decide
  - [ ] BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: Verificar nombre en manifest
    Tool: Bash
    Preconditions: Task 7 completada
    Steps:
      1. grep -i "label" tv-app/app/src/main/AndroidManifest.xml
    Expected Result: Muestra el nombre correcto
    Evidence: .sisyphus/evidence/task-7-naming.txt
  ```

  **Commit**: SÍ
  - Mensaje: `feat: extract constants and improve naming`

- [x] 8. **Agregar unit tests para managers**

  **Qué hacer**:
  - Crear tests para VideoPlayerManager
  - Crear tests para WebSocketManager
  - Crear tests para PreferencesManager

  **No debe hacer**:
  - No agregar tests que requieran device/emulator

  **Agente recomendado**: deep
  - Razón: Requiere setup de testing
  
  **Paralización**:
  - Puede ejecutarse en paralelo: NO
  - Grupo: Ola 2 (con Tasks 2, 3)
  - Bloquea: Ninguno
  - Bloqueado por: Tasks 2, 3, 4

  **Criterios de aceptación**:
  - [ ] Tests creados
  - [ ] Tests pasan

  **Commit**: SÍ
  - Mensaje: `test: add unit tests for manager classes`

- [x] 9. **Verificar compatibilidad WebSocket**

  **Qué hacer**:
  - Crear script de verificación
  - Probar todas las acciones
  - Verificar backward compatibility

  **No debe hacer**:
  - No cambiar protocolo

  **Agente recomendado**: deep
  - Razón: Requiere testing de protocolo
  
  **Paralización**:
  - Puede ejecutarse en paralelo: NO
  - Grupo: Ola 3 (con Task 5)
  - Bloquea: Ninguno
  - Bloqueado por: Tasks 1, 5, 6

  **Criterios de aceptación**:
  - [ ] Protocolo compatible
  - [ ] Todas las acciones funcionan

  **Commit**: SÍ
  - Mensaje: `feat: verify WebSocket protocol compatibility`

- [x] 10. **Actualizar nombre de app (si se decide)**

  **Qué hacer**:
  - Si usuario decide nuevo nombre:
    - Actualizar package name
    - Actualizar android:label en AndroidManifest
    - Actualizar applicationId en build.gradle.kts
  - Si no, mantener "MovieON TV"

  **No debe hacer**:
  - No cambiar sin confirmación

  **Agente recomendado**: quick
  - Razón: Straightforward renaming
  
  **Paralización**:
  - Puede ejecutarse en paralelo: SÍ
  - Grupo: Ola 1 (con Tasks 1, 4)
  - Bloquea: Ninguno
  - Bloqueado por: Ninguno

  **Criterios de aceptación**:
  - [ ] Nuevo nombre correctamente mostrado
  - [ ] BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: Verificar nombre en app
    Tool: Bash
    Preconditions: Task 10 completada
    Steps:
      1. grep "label" tv-app/app/src/main/AndroidManifest.xml
    Expected Result: Muestra el nombre correcto
    Evidence: .sisyphus/evidence/task-10-name.txt
  ```

  **Commit**: SÍ
  - Mensaje: `feat: update app name and package`

---

## Final Verification Wave

- [ ] F1. **Compliance Audit** — Verificar que todas las tareas fueron completadas
- [ ] F2. **Build Verification** — `./gradlew compileDebugKotlin` pasa
- [ ] F3. **Functional Test** — Probar que WebSocket y control de video funcionan
- [ ] F4. **Scope Fidelity Check** — Verificar que no hay feature creep

---

## Commit Strategy

1. `docs: document WebSocket protocol` - WEBSOCKET_PROTOCOL.md
2. `feat: add PreferencesManager` - PreferencesManager.kt
3. `feat: add VideoPlayerManager` - VideoPlayerManager.kt
4. `feat: add WebSocketManager` - WebSocketManager.kt
5. `refactor: refactor MainActivity` - MainActivity refactorizado
6. `feat: improve error handling and logging`
7. `feat: extract constants and improve naming`
8. `test: add unit tests for managers`
9. `feat: verify WebSocket protocol compatibility`
10. `chore: final review and cleanup`

---

## Success Criteria

### Verification Commands
```bash
./gradlew compileDebugKotlin  # Expected: BUILD SUCCESSFUL
```

### Final Checklist
- [ ] Todas las funcionalidades originales funcionan
- [ ] MainActivity < 500 líneas
- [ ] WebSocket protocol backward compatible
- [ ] Build pasa sin errores
