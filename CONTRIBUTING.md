# Guía de Contribución

¡Gracias por tu interés en contribuir a **Camera Pro ML**! Este documento proporciona directrices para contribuir al proyecto.

## Cómo Empezar

1. **Fork el repositorio** en GitHub
2. **Clona tu fork** localmente:
   ```bash
   git clone https://github.com/tu-usuario/camerax.git
   cd camerax
   ```
3. **Crea una rama** para tu feature o fix:
   ```bash
   git checkout -b feature/mi-caracteristica
   ```

## Compilación y Testing

### Compilar el Proyecto
```bash
./gradlew build
```

### Ejecutar Tests Unitarios
```bash
./gradlew test
```

### Ejecutar Tests Instrumentados
```bash
./gradlew connectedAndroidTest
```

### Compilar sin Tests
```bash
./gradlew build -x test
```

## Estándares de Código

### Kotlin Guidelines
- Seguir la [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html)
- Usar `val` en lugar de `var` cuando sea posible
- Mantener funciones pequeñas y enfocadas
- Documentar funciones públicas con KDoc

### Ejemplo de KDoc
```kotlin
/**
 * Captura una foto usando el controlador de cámara.
 * 
 * @param controller Controlador de CameraX
 * @param context Contexto de la aplicación
 */
fun capturePhoto(controller: LifecycleCameraController, context: Context) {
    // Implementación
}
```

### Compose Best Practices
- Usar `remember`, `LaunchedEffect` apropiadamente
- Mantener composables sin estado cuando sea posible
- Documentar comportamiento observable
- Evitar side effects no intencionales

## Estructura de Commits

Usar mensajes de commit descriptivos:

```
[Feature/Fix/Docs] Descripción breve

Descripción más detallada si es necesario.
Explica el "por qué" más que el "qué".

Fixes #123 (si arregla un issue)
```

### Tipos de Commit
- **Feature**: Nueva característica
- **Fix**: Corrección de bug
- **Refactor**: Cambio de código sin cambiar funcionalidad
- **Docs**: Cambios en documentación
- **Test**: Agregar o mejorar tests
- **Style**: Cambios de formato o linea

## Pull Requests

1. **Actualiza tu rama** con main:
   ```bash
   git pull origin main
   ```

2. **Push a tu fork**:
   ```bash
   git push origin feature/mi-caracteristica
   ```

3. **Crea un Pull Request** en GitHub con:
   - Descripción clara de los cambios
   - Referencia a issues relacionados
   - Screenshots si aplica

4. **Responde a revisiones**: Sé receptivo al feedback

## Checklist para PR

- [ ] El código compila sin errores
- [ ] Todos los tests pasan
- [ ] Se agregaron tests para nueva funcionalidad
- [ ] Se actualizó la documentación
- [ ] El código sigue las guidelines
- [ ] Los commits tienen mensajes descriptivos
- [ ] No hay merge conflicts

## Reporte de Bugs

Si encuentras un bug, reporta usando el template:

```markdown
### Descripción
Descripción clara del bug

### Pasos para Reproducir
1. Abre la app
2. Activa modo QR
3. Apunta a código QR

### Comportamiento Esperado
Debería mostrar el código QR

### Comportamiento Actual
No muestra nada

### Detalles
- Dispositivo: [e.g. Pixel 6]
- Android: [e.g. 12]
- Versión app: [e.g. 1.0.0]
```

## Solicitud de Features

Abre una issue describiendo:
- El problema que soluciona
- Casos de uso
- Posibles soluciones
- Alternativas consideradas

## Áreas de Contribución

### Código
- Mejorar detección de objetos
- Agregar filtros de cámara
- Optimización de batería
- Mejor manejo de errores

### Documentación
- Traducir README
- Mejorar ejemplos
- Crear tutoriales
- Documentar APIs

### Testing
- Agregar tests
- Mejorar cobertura
- Tests de rendimiento
- Casos edge

### Localización
- Agregar nuevos idiomas
- Mejorar traducciones
- Adaptar UI por idioma

## Código de Conducta

Por favor, sé respetuoso con otros colaboradores:
- Sé profesional en comunicación
- Respeta opiniones diferentes
- Ayuda a nuevos colaboradores
- Reporta comportamiento inapropiado

## Preguntas?

- 📧 Abre una issue con tu pregunta
- 📝 Revisa la documentación existente
- 💬 Participa en discusiones

¡Gracias por contribuir! 🚀

