# Changelog

Todas las anotaciones importantes de este proyecto se documentarán en este archivo.
El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
y este proyecto adhiere al [Versionado Semántico](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-05-10

### ✅ Agregado
- **Captura de Fotos**: Ciello de fotos con calidad alta
- **Grabación de Videos**: Grabación de videos con audio sincronizado
- **Detección de Códigos QR**: Escaneo en tiempo real de códigos QR usando ML Kit
- **Detección de Objetos**: Identificación automática de objetos en la pantalla
- **Galería**: Visualización de la última foto o video capturado
- **Controles de Cámara**: 
  - Cambio entre cámara frontal y trasera
  - Toggle entre modo foto y video
  - Activación/desactivación de modos de análisis
- **Interfaz Material Design 3**: Componentes modernos con Jetpack Compose
- **Tema Oscuro Optimizado**: Diseño oscuro para uso cómodo en cámara
- **Manejo de Permisos**: Solicitud en tiempo de ejecución para cámara, micrófono y almacenamiento
- **Almacenamiento**: Guardado en directorios específicos:
  - Fotos: `Pictures/CameraProML/`
  - Videos: `Movies/CameraProML/`
- **Tests**:
  - Tests unitarios básicos
  - Tests instrumentados
- **Documentación**: README completo con estructura del proyecto

### 🔧 Configuración
- **SDK Mínimo**: Android 7.0 (API 24)
- **SDK Destino**: Android 15 (API 35)
- **Lenguaje**: Kotlin 2.0.21
- **Gradle**: 8.7.3

### 📦 Dependencias Principales
- CameraX: 1.4.1
- ML Kit Barcode Scanning: 17.3.0
- ML Kit Object Detection: 17.0.2
- Jetpack Compose: 2024.11.00
- Material 3: Último
- Coil: 2.7.0

### 📝 Nota
Versión inicial completamente funcional del proyecto CameraX con características de detección de objetos y códigos QR.

---

## [Próximas Características]

- [ ] Filtros de cámara en tiempo real
- [ ] Soporte para múltiples idiomas (internacionalización)
- [ ] Indicador visual mejorado para grabación
- [ ] Transformación de coordenadas para objetos detectados
- [ ] Historial de capturas
- [ ] Compartir fotos/videos
- [ ] Editor básico de fotos
- [ ] Configuración de calidad de cámara
- [ ] Soporte para cámara de profundidad

---

## Legendas

- **✅ Agregado** - Nuevas características
- **🔧 Cambiado** - Cambios en funcionalidad existente
- **🐛 Corregido** - Corrección de bugs
- **❌ Eliminado** - Características eliminadas
- **⚠️ Deprecado** - Características que van a ser removidas
- **🔐 Seguridad** - Cambios de seguridad

