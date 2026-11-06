---
name: Tu Cuidado - Sistema de Acompañamiento y Gestión
colors:
  surface: '#f6fafd'
  surface-dim: '#d6dbdd'
  surface-bright: '#f6fafd'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f4f7'
  surface-container: '#eaeef1'
  surface-container-high: '#e5e9ec'
  surface-container-highest: '#dfe3e6'
  on-surface: '#171c1f'
  on-surface-variant: '#43474f'
  inverse-surface: '#2c3134'
  inverse-on-surface: '#edf1f4'
  outline: '#737780'
  outline-variant: '#c3c6d0'
  surface-tint: '#396092'
  primary: '#00274e'
  on-primary: '#ffffff'
  primary-container: '#0f3d6e'
  on-primary-container: '#84a9e0'
  inverse-primary: '#a5c8ff'
  secondary: '#006491'
  on-secondary: '#ffffff'
  secondary-container: '#7dc9fe'
  on-secondary-container: '#00547a'
  tertiary: '#481700'
  on-tertiary: '#ffffff'
  tertiary-container: '#6b2700'
  on-tertiary-container: '#ff854b'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d4e3ff'
  primary-fixed-dim: '#a5c8ff'
  on-primary-fixed: '#001c3a'
  on-primary-fixed-variant: '#1e4879'
  secondary-fixed: '#c9e6ff'
  secondary-fixed-dim: '#8aceff'
  on-secondary-fixed: '#001e2f'
  on-secondary-fixed-variant: '#004b6e'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb596'
  on-tertiary-fixed: '#360f00'
  on-tertiary-fixed-variant: '#7c2e00'
  background: '#f6fafd'
  on-background: '#171c1f'
  surface-variant: '#dfe3e6'
typography:
  display-elderly:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '800'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-elderly:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  body-elderly:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 34px
    letterSpacing: 0em
  headline-desktop-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-desktop-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.005em
  body-desktop-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
    letterSpacing: 0em
  body-desktop-md:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0em
  label-desktop:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-elderly:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: 0.01em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  margin: 1.5rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

Este sistema de diseño nace para articular una doble experiencia digital: por un lado, una interfaz móvil serena, empática y de extrema accesibilidad para personas mayores en Chile; por otro, un entorno operativo de escritorio eficiente, clínico y confiable para cuidadores y equipos de administración.

### Filosofía y Tono
- **Dignidad y Cercanía:** Tratamiento respetuoso, cálido y sin paternalismos. El lenguaje visual prescinde de iconografía o metáforas hospitalarias frías para el usuario mayor, priorizando la compañía humana, la autonomía y el bienestar cotidiano.
- **Español de Chile Claro:** Textos breves, comprensibles y sin tecnicismos innecesarios (e.g., "Avisar a mi cuidador", "Pedir ayuda", "Sin novedad").
- **Estilo Visual:** Fusión entre *Modern Humanist* y *Tactile Clarity*. Se utilizan superficies amplias con tintes azulados orgánicos, bordes suaves y elementos táctiles generosos que eliminan la fatiga visual y garantizan confianza inmediata en situaciones críticas y rutinarias.

## Colors

La paleta cromática se estructura en torno a la confiabilidad del azul marino profundo, la serenidad del azul medio y la urgencia calculada del naranja cálido para acciones de asistencia inmediata.

### Roles de Color

- **Primario (`#0F3D6E`):** Azul marino profundo. Utilizado en textos de máxima jerarquía, barras de navegación de escritorio y botones principales para cuidadores. Asegura ratio de contraste AAA (> 7:1) sobre superficies claras.
- **Secundario (`#3B8DBF`):** Azul medio. Conecta la identidad gráfica del servicio; sirve de soporte para elementos interactivos secundarios, cabeceras de módulos y acentos de acompañamiento.
- **Soporte / Acento Suave (`#6FA0C8`):** Azul cielo atenuado. Utilizado en fondos de selección, bordes activos y micro-interacciones.
- **Acento Crítico & SOS (`#E85D04` / `#F77F00`):** Naranja cálido y enérgico. Reservado estrictamente para llamadas de asistencia, botones de emergencia SOS y alertas de resolución inmediata, evitando el pánico asociado al rojo clínico tradicional pero garantizando visibilidad instantánea.
- **Superficies & Fondos:** Blanco puro (`#FFFFFF`) para tarjetas y paneles activos; azul pálido (`#F4F8FB`) como fondo general de lienzo para amortiguar el brillo de pantalla.

### Sistema Semáforo Triple (Accesibilidad Universal)
Todo indicador de estado debe presentar simultáneamente **color, ícono formal y etiqueta textual**:
1. **Sin novedad (`#2E7D32`):** Acompañado obligatoriamente de ícono de verificación continua (check) y texto explícito *"Sin novedad"*.
2. **Observar (`#E6A100`):** Acompañado obligatoriamente de ícono de atención visual (ojo o alerta) y texto explícito *"Observar"*.
3. **Revisar (`#C62828`):** Acompañado obligatoriamente de ícono de atención crítica (signo de exclamación dentro de rombo/círculo) y texto explícito *"Revisar"*.

## Typography

La selección combina la geometría amigable y curvada de **Plus Jakarta Sans** para titulares y etiquetas interactivas con la excelente legibilidad en pantalla de **Be Vietnam Pro** para lectura continua.

### Directrices Específicas por Plataforma

- **Entorno Móvil (Persona Mayor):**
  - Ningún texto interactivo o informativo desciende de los `24px` de tamaño físico.
  - Los encabezados principales comienzan en `32px` hasta `36px` con peso extra audaz (`700` o `800`) para facilitar la lectura sin lentes o con visibilidad reducida.
  - Prohibido el uso de pesos ligeros (light o thin); se aplican únicamente pesos `600` (SemiBold) y `700`/`800` (Bold).
- **Paneles de Gestión (Escritorio / Cuidadores):**
  - Mínimo de `16px` para cuerpos de tabla y lectura general; etiquetas secundarias en `14px` sólo en datos complementarios.
  - Alturas de línea generosas (`1.5x` mínimo) para evitar fatiga en turnos de monitoreo prolongados.

## Layout & Spacing

El ritmo espacial atiende a dos modos operativos divergentes pero armónicos:

### Dispositivo Móvil para Adulto Mayor (Viewport Fijo / Sin Scroll)
- Estructura vertical fija sin barra de desplazamiento dentro de las vistas de acción. El contenido debe encajar limpiamente en el alto del dispositivo (`100dvh`).
- Disposición de tarjetas en 1 o máximo 2 bloques masivos por pantalla.
- Zonas táctiles separadas por espacios amplios (`space-lg` y `space-xl`) para erradicar pulsaciones accidentales por temblor o precisión limitada.

### Paneles de Escritorio (Gestión y Monitoreo)
- Rejilla fluida de 12 columnas con canales de `1.5rem` (`24px`) y márgenes laterales contenidos.
- Las tablas y fichas de seguimiento utilizan espaciado denso-moderado para mostrar bitácoras, registros de visitas y estado de salud sin saturación cognitiva.

## Elevation & Depth

La jerarquía visual combina sombras ambientales ultra suaves con contrastes superficiales tintados en azul para conservar la calidez y el orden clínico.

### Niveles de Elevación

1. **Nivel 0 (Fondo General):** Fondo plano `#F4F8FB`.
2. **Nivel 1 (Tarjetas y Contenedores):** Superficie `#FFFFFF` con borde sutil de `1px solid rgba(15, 61, 110, 0.08)` y sombra suave difusa: `0px 4px 16px rgba(15, 61, 110, 0.04)`.
3. **Nivel 2 (Estados Flotantes y Elementos Clave):** Sombra interactiva `0px 8px 24px rgba(15, 61, 110, 0.08)` para modales de confirmación y tarjetas activas.
4. **Nivel Prioritario (Botón de Ayuda / SOS Móvil):** Relieve táctil optimizado mediante una sombra envolvente cálida: `0px 10px 28px rgba(232, 93, 4, 0.28)`, otorgando presencia física inmediata para accionamiento instintivo.

## Shapes

El lenguaje de formas es orgánico y acogedor, evitando ángulos agresivos o rigidez institucional.

- **Nivel de Curvatura Base:** `2` (equivalente a radios de curvatura de `0.5rem` / `8px` en controles pequeños).
- **Tarjetas y Paneles de Control (Escritorio):** Aplican esquinas suaves con radio de `16px` (`rounded-2xl`), produciendo un entorno ordenado pero humano.
- **Acciones Móviles para la Persona Mayor:** Excepciones prioritarias hacia formas cápsula (`rounded-full`) o ultra redondeadas (`24px` a `32px` de radio), facilitando una silueta amigable que incita a ser tocada de forma natural y sin temor a error.

## Components

### 1. Botones Gigantes de Acción Móvil (Persona Mayor)
- **Dimensiones:** Altura fija mínima de `72px`, ancho completo del contenedor.
- **Tipografía:** `22px` - `24px` en `Plus Jakarta Sans Bold`.
- **Iconografía:** Íconos de trazo grueso (`32px` mínimo) ubicados a la izquierda del texto para contextualizar la orden.
- **Feedback Táctil:** Al pulsar, el botón no depende únicamente del cambio de color; incluye vibración suave (haptic feedback) y reducción de escala elástica suave (0.97).

### 2. Botón de Emergencia / SOS
- Color de fondo primario `#E85D04` con texto blanco puro `#FFFFFF`.
- Confirmación de seguridad mediante presión sostenida de 2 segundos o doble confirmación dialogada en tipografía gigante para evitar falsas alarmas sin frustrar al usuario.

### 3. Fichas de Semáforo de Estado (Cuidadores y Administradores)
- **Contenedores de Estado:** Cápsulas o chips con relleno tonal claro, borde definido de `1.5px` en el color de alerta y padding de `8px 16px`.
- **Estructura Mandatoria:**
  - Ícono identificativo a 20px (Check, Ojo, Exclamación).
  - Texto descriptivo en negrita con el estado exacto (*"Sin novedad"*, *"Observar"*, *"Revisar"*).
  - Jamás transmitir el estado clínico exclusivamente mediante el punto o círculo de color.

### 4. Tarjetas de Registro y Bitácora Diaria
- Encabezado con foto del acompañante/cuidador, fecha en lenguaje natural ("Hoy, Martes 14") y estado de semáforo visible.
- Fondo blanco puro, borde perimetral tintado en azul y separación clara entre observaciones médicas y notas personales de afecto o ánimo.

### 5. Campos de Entrada y Selectores
- Altura mínima en panel de escritorio de `48px`; en móvil de cuidador `56px`.
- Bordes definidos con foco azul marino `#0F3D6E` con halo suave `rgba(59, 141, 191, 0.2)`. Etiquetas siempre visibles y fijadas sobre el input (no relying on placeholders que desaparecen).