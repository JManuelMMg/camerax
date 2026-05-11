# 🧪 GUÍA DE TESTING - Verificación de Soluciones

**Objetivo:** Verificar que los problemas de congelamiento y codec de audio fueron completamente resueltos.

---

## 📱 Preparación del Test

### Requisitos
- Dispositivo Android 8.0+ (API 24+)  
- APK compilada: `app/build/outputs/apk/debug/app-debug.apk`
- ADB conectado (opcional para logs)

### Instalación
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ✅ Test 1: Detección de QR Code

### Objetivo
Verificar que la detección de QR NO congela la aplicación y procesa eficientemente.

### Procedimiento

1. **Abrir aplicación**
   - Presionar botón "📱 QR" (códigos QR)
   - ✅ RESULTADO ESPERADO: Pantalla fluida, sin congelamiento

2. **Apuntar a un QR code**
   - Dirigir cámara a un QR code válido
   - ✅ RESULTADO ESPERADO: 
     - QR se detecta en menos de 1 segundo
     - Texto aparece en pantalla UNA SOLA VEZ
     - No hay parpadeo de UI

3. **Cambiar entre múltiples QR codes**
   - Apuntar a 3-4 QR codes diferentes secuencialmente
   - ✅ RESULTADO ESPERADO:
     - Cada nuevo QR se detecta sin delay perceptible
     - UI se actualiza suavemente
     - No hay congelamiento

4. **Verificar en logs (ADB)**
   ```powershell
   adb logcat | grep "QrCodeAnalyzer"
   ```
   ✅ RESULTADO ESPERADO:
   ```
   D/CameraScreen: QrCodeAnalyzer análisis
   D/CameraViewModel: QR detectado: https://ejemplo.com
   (máximo 2-3 líneas por segundo, no 30+)
   ```

### Criterio de Éxito
- ✅ Sin congelamiento perceptible
- ✅ Procesamiento < 1 segundo
- ✅ UI suave durante detección
- ✅ No hay lag o stuttering

### Criterio de Falla
- ❌ Pantalla freezea al detectar QR
- ❌ Parpadeo o actualización excesiva
- ❌ Logs muestran 30+ análisis por segundo

---

## ✅ Test 2: Detección de Objetos

### Objetivo
Verificar que la detección de objetos funciona sin congelamiento y con debounce inteligente.

### Procedimiento

1. **Abrir aplicación**
   - Presionar botón "🔍 Detectar" (detección de objetos)
   - ✅ RESULTADO ESPERADO: Sin congelamiento

2. **Apuntar a un objeto**
   - Dirigir cámara a una persona, silla, perro, etc.
   - ✅ RESULTADO ESPERADO:
     - Objeto se detecta y dibuja cuadro de detección
     - Aparece label con confianza
     - Sin lag en el dibujo del canvas

3. **Cambiar entre objetos**
   - Cambiar lo que apunta la cámara rápidamente
   - ✅ RESULTADO ESPERADO:
     - Actualizaciones cada 1-2 segundos (no 30 por segundo)
     - Solo detectados si cambio es significativo (>10% confianza)
     - Sin congelamiento

4. **Verificar debounce inteligente**
   - Apuntar a mismo objeto (ej: persona)
   - Ver confianza cambiar de 92% → 93% → 91%
   - ✅ RESULTADO ESPERADO:
     - UI NO se actualiza constantemente
     - Solo se actualiza si hay cambio real (>10%)

5. **Verificar en logs (ADB)**
   ```powershell
   adb logcat | grep "ObjectDetectionAnalyzer"
   ```
   ✅ RESULTADO ESPERADO:
   ```
   D/CameraViewModel: Objeto detectado: person, confianza: 95.2%
   D/CameraViewModel: Objeto detectado: chair, confianza: 87.1%
   (máximo 1-2 líneas por segundo)
   ```

### Criterio de Éxito
- ✅ Sin congelamiento
- ✅ Cuadros se dibujan suavemente
- ✅ Debounce funciona (solo grandes cambios generan updates)
- ✅ Eficiencia energética mejorada

### Criterio de Falla
- ❌ Canvas parpadea constantemente
- ❌ UI se actualiza 20+ veces por segundo
- ❌ Temperatura del dispositivo > 50°C

---

## ✅ Test 3: Grabación de Video (Codec de Audio)

### Objetivo
Verificar que la grabación de video funciona con audio automático y fallback sin audio si es necesario.

### Procedimiento - Caso 1: Con Audio (Dispositivo Soportado)

1. **Cambiar a modo Video**
   - Presionar botón "🎥 Video"
   - ✅ RESULTADO ESPERADO: Interfaz sin congelamiento

2. **Presionar botón central para grabar**
   - ✅ RESULTADO ESPERADO: Toast "🎥 Grabando... (con audio)"

3. **Grabar 5-10 segundos**
   - ✅ RESULTADO ESPERADO:
     - Grabación fluida sin interrupciones
     - Sin errores en logs

4. **Presionar botón central nuevamente para detener**
   - ✅ RESULTADO ESPERADO: Toast "🎥 Video guardado (✅ Con audio)"

5. **Verificar archivo**
   ```powershell
   adb shell ls -la /sdcard/Movies/CameraProML/
   # Debería haber un archivo .mp4 reciente
   ```

6. **Reproducir con audio**
   - ✅ RESULTADO ESPERADO: Audio presente en el video

### Procedimiento - Caso 2: Sin Audio (Dispositivo No Soportado)

Si el dispositivo no soporta codec de audio (error 4):

1. **Presionar botón central para grabar**
   - ✅ RESULTADO ESPERADO: Toast "🎥 Grabando... (con audio)"

2. **Grabar 5-10 segundos**
   - Sistema detecta error de codec
   - Toast "⚠️ Error de codec de audio - Reintentando sin audio"

3. **Presionar botón central para detener**
   - ✅ RESULTADO ESPERADO: Toast "🎥 Video guardado (⚠️ Sin audio)"

4. **Verificar archivo**
   ```powershell
   adb shell ls -la /sdcard/Movies/CameraProML/
   ```
   - ✅ RESULTADO ESPERADO: Video guardado exitosamente sin audio

### Procedimiento - Caso 3: Validación de Micrófono

1. **Deshabilitar audio en configuración del dispositivo (si es posible)**
   ```powershell
   adb shell settings put secure assist_gesture_enabled 0
   ```

2. **Presionar botón central para grabar**
   - ✅ RESULTADO ESPERADO: 
     - Toast "🎥 Grabando... (sin audio)" (cambio automático)
     - No a mostrar error

3. **Presionar botón central para detener**
   - ✅ RESULTADO ESPERADO: Toast "🎥 Video guardado (⚠️ Sin audio)"

### Verificar en Logs (ADB)

```powershell
adb logcat | grep "CameraViewModel"
```

✅ RESULTADO ESPERADO:

**Escenario 1 (Con audio):**
```
D/CameraViewModel: AudioConfig: Grabando CON AUDIO
D/CameraViewModel: ✅ Grabación iniciada con éxito
D/CameraViewModel: ✅ Video guardado exitosamente
```

**Escenario 2 (Codec no soportado → Fallback):**
```
D/CameraViewModel: AudioConfig: Grabando CON AUDIO
E/CameraViewModel: ❌ Error de grabación: Código=4, Mensaje=Error de audio
W/CameraViewModel: Error de codec detectado - Reintentando sin audio...
D/CameraViewModel: AudioConfig: Grabando SIN AUDIO
D/CameraViewModel: ✅ Video guardado exitosamente
```

### Criterio de Éxito
- ✅ Grabación con audio si disponible
- ✅ Fallback automático sin audio si error
- ✅ No mostrar error al usuario (manejo transparente)
- ✅ Video siempre se guarda (con o sin audio)

### Criterio de Falla
- ❌ Error "No se pudo iniciar grabación"
- ❌ Crash de la aplicación durante grabación
- ❌ Video no se guarda

---

## ✅ Test 4: Consumo de Recursos

### Objetivo
Verificar que los cambios redujeron significativamente el consumo de recursos.

### Test de CPU (con QR mode activo)

```powershell
# Terminal 1: Monitorear CPU
adb shell "top -b -n 5 | grep com.example.camerax"

# Esperado: 5-15% CPU (antes: 30-50%)
```

### Test de Memoria

```powershell
adb shell "dumpsys meminfo com.example.camerax"

# Esperado:
# - Native Heap: < 50 MB
# - Dalvik Heap: < 100 MB
# - Total: < 150 MB
```

### Test de Threads

```powershell
adb shell "ps -p $(adb shell pidof com.example.camerax) | head -1"
adb shell "cat /proc/$(adb shell pidof com.example.camerax)/status | grep Threads"

# Esperado: 30-50 threads (antes: 80-120)
```

### Test de Temperatura

```powershell
adb shell "cat /sys/class/thermal/thermal_zone0/temp"

# Esperado: < 45°C en modo QR
# (antes: 50-60°C)
```

### Test de Batería (Long Test)

1. **Cargar dispositivo a 100%**

2. **Iniciar en modo QR durante 30 minutos**
   - Pantalla encendida
   - QR mode activo
   - Registrar tiempo de apagado

3. **Repetir con versión anterior**
   - Usar APK anterior si está disponible
   - Mismo test 30 minutos

4. **Comparar consumo**
   - Nueva versión debe durar significativamente más
   - Criterio: > 50% más de tiempo

---

## ✅ Test 5: Cambio de Modos

### Objetivo
Verificar que cambiar entre modos no causa leaks de memoria o congelamiento.

### Procedimiento

1. **Cambiar entre modos rápidamente**
   ```
   Foto → Foto
   Foto → QR
   QR → Objetos
   Objetos → Video
   Video → Foto
   (repetir 10 veces)
   ```

2. ✅ RESULTADO ESPERADO:
   - Sin congelamiento
   - Cambios instantáneos
   - Sin crash

3. **Verificar memoria (después de 10 ciclos)**
   ```powershell
   adb shell "dumpsys meminfo com.example.camerax | grep TOTAL"
   ```
   - ✅ RESULTADO ESPERADO: Memoria estable (no aumenta continuamente)

---

## ✅ Test 6: Cambio de Cámaras

### Objetivo
Verificar que cambiar entre cámaras frontal/trasera funciona correctamente.

### Procedimiento

1. **Estar en modo QR**

2. **Presionar botón de voltear cámara**
   - ✅ RESULTADO ESPERADO: Cambio instantáneo

3. **Cambiar 10 veces entre cámaras**
   - ✅ RESULTADO ESPERADO: Sin lag, sin congelamiento

4. **Test en cada cámara**
   - Detección QR en cámara trasera
   - Detección QR en cámara frontal
   - ✅ RESULTADO ESPERADO: Ambas funcionales

---

## 🎯 Test Final: Escenario Completo

### Procedimiento de Test Integral (15 minutos)

```
Inicio de Sesión (2 min)
├─ App inicia
├─ Solicita permisos
└─ Cámara lista

Modo Foto (1 min)
├─ Tomar 2-3 fotos
├─ Sin lag
└─ Fotos guardadas

Modo Video (2 min)
├─ Grabar 30 segundos
├─ Con audio (si soporta)
└─ Sin congelamiento

Modo QR (3 min)
├─ Apuntar a 5 QR diferentes
├─ Cada uno se detecta rápido
├─ Sin parpadeo de UI
└─ Sin artefactos

Modo Detección de Objetos (3 min)
├─ Señalar varios objetos
├─ Debounce funciona
├─ Sin lag en canvas
└─ Sin congelamiento

Cambios de Modo + Cámaras (4 min)
├─ Cambiar rápidamente entre modos
├─ Voltear cámara múltiples veces
├─ Sin crash
└─ Sin memory leaks
```

### Criterios de Aceptación Final
- ✅ 0 crashes durante 15 minutos de uso intenso
- ✅ Temperatura < 50°C al final
- ✅ Memoria estable (no crece constantemente)
- ✅ Sin errores de codec de audio
- ✅ Todo funciona sin congelamiento

---

## 📊 Registro de Resultados

### Plantilla de Test

```
FECHA: ________________
DISPOSITIVO: ________________________ (Modelo, Android version)
APK VERSION: ________________________

TEST 1: QR Detection
├─ Sin congelamiento: ☐ PASS ☐ FAIL
├─ Detección rápida: ☐ PASS ☐ FAIL
├─ UI suave: ☐ PASS ☐ FAIL
└─ Logs correctos: ☐ PASS ☐ FAIL

TEST 2: Object Detection
├─ Sin congelamiento: ☐ PASS ☐ FAIL
├─ Debounce funciona: ☐ PASS ☐ FAIL
├─ Canvas suave: ☐ PASS ☐ FAIL
└─ Logs correctos: ☐ PASS ☐ FAIL

TEST 3: Video Recording
├─ Con audio: ☐ PASS ☐ FAIL
├─ Fallback sin audio: ☐ PASS ☐ FAIL
├─ Validación mic: ☐ PASS ☐ FAIL
└─ Logs correctos: ☐ PASS ☐ FAIL

TEST 4: Recursos
├─ CPU: ________________ (esperado: 5-15%)
├─ Memoria: ________________ MB (esperado: <150)
├─ Threads: ________________ (esperado: 30-50)
├─ Temperatura: ________________°C (esperado: <45)
└─ Batería (30min): ________________ % consumido

TEST 5: Cambio de Modos
├─ Sin crash: ☐ PASS ☐ FAIL
├─ Memory leak: ☐ PASS ☐ FAIL
└─ Cambios instantáneos: ☐ PASS ☐ FAIL

TEST 6: Cambio de Cámaras
├─ Cambio instantáneo: ☐ PASS ☐ FAIL
├─ Funcionan ambas: ☐ PASS ☐ FAIL
└─ Sin lag: ☐ PASS ☐ FAIL

TEST FINAL: Escenario Completo
├─ Crashes durante 15 min: ☐ 0 ☐ >0
├─ Estado final temperatura: ________________°C
├─ Final memory: ________________ MB
└─ RESULTADO GENERAL: ☐ PASS ☐ FAIL

NOTAS:
_________________________________________________________________
_________________________________________________________________
```

---

## 🐛 Si Encuentras Problemas

### Problema: Todavía hay congelamiento en QR
**Solución:**
```powershell
# Aumentar interval a 1000ms en QrCodeAnalyzer.kt
private val ANALYSIS_INTERVAL_MS = 1000L  # cambiar a 1000
# Recompilar
./gradlew assembleDebug
```

### Problema: Mucho lag en Object Detection
**Solución:**
```powershell
# Aumentar interval a 1200ms en ObjectDetectionAnalyzer.kt
private val ANALYSIS_INTERVAL_MS = 1200L  # cambiar a 1200
# Recompilar
./gradlew assembleDebug
```

### Problema: Codec de audio sigue fallando
**Solución:**
```powershell
# Forzar sin audio en CameraViewModel.kt
currentAudioStrategy = AudioConfigUtils.AudioFallbackStrategy.WITHOUT_AUDIO
# Recompilar y probar
./gradlew assembleDebug
```

---

**Generado:** 2026-05-11  
**Versión:** 1.0  
**Estado:** Listo para testing

