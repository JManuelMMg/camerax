# 📦 GUÍA DE INSTALACIÓN Y DISTRIBUCIÓN - CameraX v2.0 Profesional

## 📍 Ubicación de Archivos APK

### APK de Depuración (Desarrollo)
```
Ruta: C:\Users\jmedi\AndroidStudioProjects\camerax\app\build\outputs\apk\debug\
Archivo: app-debug.apk
Tamaño: 80.6 MB
Uso: Desarrollo y testing
```

### APK de Distribución (Producción)
```
Ruta: C:\Users\jmedi\AndroidStudioProjects\camerax\app\build\outputs\apk\release\
Archivo: app-release-unsigned.apk
Tamaño: 76.8 MB
Uso: Distribución a usuarios
```

---

## 🔧 Instalación en Dispositivo

### Opción 1: Instalación vía ADB (Línea de Comandos)

#### Prerequisitos
- ADB instalado (Android SDK Tools)
- Dispositivo Android conectado con USB Debug habilitado
- Drivers USB actualizados

#### Pasos

**1. Verificar que el dispositivo está conectado:**
```bash
adb devices
```

**2. Instalar APK Debug:**
```bash
adb install C:\Users\jmedi\AndroidStudioProjects\camerax\app\build\outputs\apk\debug\app-debug.apk
```

**3. Instalar APK Release:**
```bash
adb install C:\Users\jmedi\AndroidStudioProjects\camerax\app\build\outputs\apk\release\app-release-unsigned.apk
```

**4. Verificar instalación:**
```bash
adb shell pm list packages | grep camerax
```

**5. Ejecutar la app:**
```bash
adb shell am start -n com.example.camerax/.MainActivity
```

### Opción 2: Instalación Manual en Emulador

1. Abre Android Studio
2. Abre el dispositivo virtual (AVD) deseado
3. Arrastra el APK al emulador
4. Espera a que se instale
5. La app aparecerá en el launcher

### Opción 3: Instalación vía Archivo Físico

1. Transferir APK a dispositivo Android
2. Abrir gestor de archivos
3. Navegar al APK
4. Tocar el archivo
5. Confirmar instalación
6. Dar permisos requeridos

---

## ✅ Verificación Post-Instalación

### En Dispositivo/Emulador

1. **Verifica que la app aparezca** en el launcher
2. **Abre la app**
3. **Verifica permisos** - Debería pedir:
   - CAMERA
   - RECORD_AUDIO
   - STORAGE
   - POST_NOTIFICATIONS (si API 13+)
4. **Prueba funciones básicas**:
   - Botón FOTO → Captura foto
   - Botón VIDEO → Graba video
   - Botón QR → Lee código QR
   - Botón OBJETOS → Detecta objetos

### Tests de Grabación de Video

#### Test 1: Grabación Normal (Con Audio)
```
1. Cambia a modo VIDEO
2. Presiona botón central
3. Espera 3 segundos
4. Presiona nuevamente
5. Ver toast: "🎥 Video guardado (con audio)" ✅
```

#### Test 2: Cambio de Cámara
```
1. Presiona botón GIRAR
2. Cámara cambia a frontal ✅
3. Repite cambio
4. Cámara regresa a trasera ✅
```

#### Test 3: Detección de Código QR
```
1. Cambia a modo QR
2. Apunta a código QR
3. Verifica que aparezca "QR DETECTADO" ✅
```

---

## 📋 Requisitos del Sistema

### Dispositivo Mínimo
- **Android**: 7.0 (API 24)
- **RAM**: 2 GB (recomendado 4 GB)
- **Almacenamiento**: 100 MB libres
- **Características**: Cámara, Micrófono

### Dispositivo Recomendado
- **Android**: 10.0+ (API 29+)
- **RAM**: 4 GB+
- **Almacenamiento**: 500 MB libres
- **Características**: Dos cámaras (frontal + trasera)

### Emulador (Android Studio)
- **API Level**: 24+
- **CPU**: x86_64
- **RAM**: 2GB
- **SD Card**: 500 MB
- **Características**: Cámara virtual, Micrófono virtual

---

## 🔐 Permisos en Runtime

La app solicitará permisos cuando:

### Primera instalación
```
✓ CAMERA - Para usar la cámara
✓ MICROPHONE/RECORD_AUDIO - Para grabar audio
✓ STORAGE - Para guardar fotos/videos
✓ NOTIFICATIONS - Para notificaciones (Android 13+)
```

### Cómo otorgar permisos

1. **Android 6+**: Se solicitan en runtime
2. **Android < 6**: Se otorgan automáticamente en instalación
3. **Cambiar permisos**: Configuración → Aplicaciones → CameraX → Permisos

---

## 🎬 Ubicación de Archivos Guardados

### Fotos
```
Almacenamiento interno / Pictures / CameraProML /
Formato: JPEG
Nombre: yyyyMMdd_HHmmss.jpg
Ejemplo: 20260511_133000.jpg
```

### Videos
```
Almacenamiento interno / Movies / CameraProML /
Formato: MP4
Nombre: yyyyMMdd_HHmmss.mp4
Ejemplo: 20260511_133000.mp4
```

---

## 🆘 Troubleshooting

### Problema: "No se puede instalar la app"

**Causa**: APK anterior instalado con firma diferente

**Solución**:
```bash
adb uninstall com.example.camerax
adb install app-debug.apk
```

### Problema: Cámara no funciona

**Causa**: Permiso CAMERA no otorgado

**Solución**:
1. Abre Configuración
2. Aplicaciones → CameraX
3. Permisos → CAMERA (Habilitar)
4. Reinicia la app

### Problema: No graba audio

**Causa**: Permiso RECORD_AUDIO no otorgado

**Solución**:
1. Abre Configuración
2. Aplicaciones → CameraX
3. Permisos → MICROPHONE (Habilitar)
4. Reinicia la app
5. Si sigue fallando, app reintentar sin audio automáticamente

### Problema: No se guardan archivos

**Causa**: Almacenamiento lleno o permiso STORAGE no otorgado

**Solución**:
1. Libera espacio (al menos 100 MB)
2. Otorga permiso STORAGE:
   - Configuración → Aplicaciones → CameraX → Permisos → STORAGE
3. Reinicia la app

### Problema: Error 4 en grabación

**Causa**: Codec de audio no soportado

**Solución**:
- ✅ App automáticamente reintentar sin audio
- El video se guardará correctamente
- **No requiere acción del usuario**

### Problema: App se congela

**Causa**: Procesamiento de ML Kit en hilo principal

**Solución**:
1. Reinicia la app
2. Desactiva detección QR/Objetos si no la usas
3. En emulador: Asigna más CPU

---

## 📊 Información de Compilación

### Datos de Build

```
Versión: 2.0
Build Type: Debug & Release
Gradle: 9.2.1
Kotlin: Latest
Android Gradle Plugin: Latest
Min SDK: 24
Target SDK: 35
```

### Tamaño de APK

```
Debug: 80.6 MB (sin optimizar)
Release: 76.8 MB (optimizado sin firma)
```

### Tiempo de Compilación

```
Clean Build: ~2m 13s
Incremental: ~30-60s
```

---

## 🚀 Distribución a Google Play

### Si deseas publicar en Google Play

1. **Firma el APK Release**:
```bash
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore my-release-key.keystore \
  app-release-unsigned.apk alias_name
```

2. **Optimiza con zipalign**:
```bash
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

3. **Sube a Google Play Console**
4. **Rellena información de la app**
5. **Publica a producción**

---

## 🔍 Información de Versión

### Versión de la App
```
Nombre: CameraX Pro
Package: com.example.camerax
Versión: 1.0
Código de versión: 1
```

### Nota sobre Versionado

Para actualizar en futuro:
- Aumentar `versionCode` en `build.gradle.kts`
- Aumentar `versionName` en `build.gradle.kts`
- Esto permite actualizaciones automáticas en dispositivos

---

## 📱 Emulador Recomendado

Para mejor experiencia de testing:

```
Configuración de ADV (Android Virtual Device):
- Dispositivo: Pixel 5
- API: 32 (Android 12.0)
- CPU: x86_64
- RAM: 4 GB
- Heap: 512 MB
- SD Card: 1 GB
- Features: Cámara frontal + trasera
```

---

## ✨ Características Destacadas en Esta Versión

✅ **Sistema de fallback de audio inteligente**  
✅ **Manejo robusto de errores**  
✅ **Logging profesional**  
✅ **UI/UX moderno**  
✅ **Detección QR en tiempo real**  
✅ **Detección de objetos**  
✅ **Cambio de cámara suave**  
✅ **Documentación profesional**  

---

## 🎯 Siguientes Pasos

1. **Instala el APK en tu dispositivo**
2. **Prueba todas las funciones**
3. **reporta cualquier issue**
4. **Comparte feedback**

---

## 📞 Soporte

En caso de problemas:
1. Revisa los logs: `adb logcat | grep CameraViewModel`
2. Verifica que permisos estén otorgados
3. Asegúrate de conexión a internet (para ML Kit)
4. Reinicia el dispositivo
5. Reinstala la app

---

**Guía de Instalación v2.0**  
**Fecha**: 2026-05-11  
**Estado**: ✅ Completa y Verificada  

**¡Disfruta usando CameraX Pro!** 🎉

