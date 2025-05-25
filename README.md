# myMind - Frontend Android

![myMind Logo](https://img.shields.io/badge/myMind-Emotional%20Wellness-purple?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![API](https://img.shields.io/badge/Min%20SDK-26-orange?style=for-the-badge)
![Target](https://img.shields.io/badge/Target%20SDK-34-green?style=for-the-badge)

## 📖 Descripción

**myMind** es una aplicación móvil innovadora diseñada para fomentar el autoconocimiento y la autopercepción emocional mediante el uso de notas de voz y procesamiento de lenguaje natural (NLP). Este repositorio contiene el código del frontend desarrollado en Android Studio como trabajo de grado de cuatro estudiantes de Ingeniería de Sistemas de la Pontificia Universidad Javeriana.

### 🎯 Objetivo

Crear un espacio seguro y privado donde los usuarios puedan expresar sus emociones a través de grabaciones de voz, recibiendo análisis personalizados de su estado emocional para promover el bienestar mental.

## ✨ Características Principales

- **🎙️ Grabación de Notas de Voz**: Sistema intuitivo para registrar pensamientos y emociones (máximo 1 minuto)
- **🧠 Análisis Emocional con NLP**: Procesamiento automático de audio con detección de emociones y sentimientos
- **📊 Reportes Visuales**: Gráficos interactivos que muestran patrones emocionales a lo largo del tiempo
- **📱 Interfaz Intuitiva**: Diseño centrado en el usuario con navegación fluida
- **🔐 Privacidad y Seguridad**: Autenticación robusta con Auth0 y cifrado de datos
- **📈 Seguimiento Histórico**: Visualización de tendencias emocionales con charts personalizados
- **🔔 Recordatorios Inteligentes**: Notificaciones personalizables para mantener el hábito

## 🛠️ Tecnologías Utilizadas

### Core
- **Android Studio** - IDE de desarrollo
- **Kotlin** - Lenguaje principal
- **Android SDK 26-34** - Compatibilidad amplia
- **Material Design 3** - Sistema de diseño moderno

### Arquitectura y Patrones
- **MVVM (Model-View-ViewModel)** - Patrón arquitectónico
- **Fragments** - Navegación modular
- **ViewBinding** - Binding type-safe
- **Coroutines** - Programación asíncrona

### Networking y APIs
- **Retrofit 2** - Cliente HTTP type-safe
- **OkHttp** - Interceptores y logging
- **Gson** - Serialización JSON
- **Auth0** - Autenticación y autorización

### UI/UX
- **MPAndroidChart** - Gráficos interactivos
- **Glide** - Carga y caching de imágenes
- **Custom Views** - Componentes personalizados
- **Lottie Animations** - Animaciones fluidas

### Storage y Persistencia
- **SharedPreferences** - Configuraciones locales
- **Encrypted SharedPreferences** - Datos sensibles
- **File Storage** - Gestión de archivos de audio

### Servicios
- **WorkManager** - Tareas en segundo plano
- **NotificationManager** - Sistema de notificaciones
- **MediaRecorder** - Grabación de audio

## 📱 Estructura de la Aplicación

### Flujo Principal
```
Splash Screen → Welcome → Auth (Login/Register) → Home → Fragments
```

### Fragments Principales
- **🎙️ RecordingFragment**: Grabación de notas de voz
- **📊 ReportFragment**: Visualización de análisis agregado
- **📋 SpecificReportFragment**: Detalles de análisis individual
- **👤 ProfileFragment**: Gestión de perfil de usuario
- **⚙️ SettingsFragment**: Configuraciones de la aplicación
- **📂 AudioHistoryFragment**: Historial de grabaciones

## 🏗️ Arquitectura del Proyecto

```
app/
├── adapters/              # RecyclerView adapters y componentes UI
├── models/               # Modelos de datos (users, audios, visualizations)
├── services/             # Servicios de red y lógica de negocio
│   ├── audios/          # Gestión de audio y transcripciones
│   ├── users/           # Autenticación y gestión de usuarios
│   └── visualizations/  # Análisis y reportes
├── activities/          # Activities principales
├── fragments/           # Fragments de la UI
└── res/                # Recursos (layouts, drawables, strings)
```

## 🔧 Configuración del Entorno

### Prerrequisitos
- **Android Studio** Arctic Fox o superior
- **JDK 8** o superior
- **Android SDK** con API level 26-34
- **Gradle 8.11.1**

### Variables de Entorno
Crear archivo `app/secrets.properties`:
```properties
MGMT_CLIENT_ID=your_auth0_mgmt_client_id
MGMT_CLIENT_SECRET=your_auth0_mgmt_client_secret
AUTH_CLIENT_ID=your_auth0_client_id
AUTH_CLIENT_SECRET=your_auth0_client_secret
AUTH_DOMAIN=your_auth0_domain
AUTH_AUDIENCE_BACKEND=your_backend_api_audience
AUTH_AUDIENCE_MGMT=your_mgmt_api_audience
```

### Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/mymind-android.git
cd mymind-android
```

2. **Configurar secrets.properties**
```bash
cp app/secrets.properties.example app/secrets.properties
# Editar con tus credenciales
```

3. **Sincronizar dependencias**
```bash
./gradlew build
```

4. **Ejecutar la aplicación**
- Conectar dispositivo Android o iniciar emulador
- Run → Run 'app'

## 🌐 Integración con Backend

La aplicación se comunica con un backend desarrollado en paralelo que proporciona:

- **API de Autenticación**: Gestión de usuarios con Auth0
- **API de Transcripción**: Procesamiento de audio con NLP
- **API de Visualizaciones**: Análisis y agregación de datos emocionales
- **Base URL**: `https://mymind.serviamiga.com`

### Endpoints Principales
```kotlin
// Autenticación
POST /users/register
GET /users/profile
PATCH /users/update-name
DELETE /users/delete

// Audio y Transcripciones
POST /transcription/add-transcription/audio
GET /transcription/stream/{task_id}
DELETE /users/transcriptions/delete-transcription/{id}

// Visualizaciones
GET /transcriptions/user
GET /transcriptions/user/last-7-days
```

## 🎨 Diseño y UI

### Paleta de Colores
- **Primary**: Purple tones (#352b73, #584dc0, #8985e7)
- **Secondary**: Complementary pastels (#D1C9F1, #BDE4E9, #EFB3D7)
- **Background**: Gradient whites with purple accents

### Componentes Personalizados
- **VisualizerView**: Visualización de ondas de audio en tiempo real
- **Custom Charts**: Gráficos de radar, barras y pie personalizados
- **Animated Transitions**: Transiciones fluidas entre pantallas

## 🧪 Testing

### Estructura de Tests
```
src/test/
├── services/
│   ├── AudioRemoteServiceTest.kt
│   ├── UserRemoteServiceTest.kt
│   └── VisualizationRemoteServiceTest.kt
└── ExampleUnitTest.kt
```

### Ejecutar Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 📊 Funcionalidades Detalladas

### Grabación de Audio
- **Duración**: Máximo 60 segundos
- **Formato**: MP3/M4A
- **Controles**: Play/Pause/Stop/Delete
- **Visualización**: Waveform en tiempo real
- **Confirmación**: Preview antes del envío

### Análisis Emocional
- **Emociones**: Joy, Anger, Sadness, Disgust, Fear, Neutral, Surprise, Trust, Anticipation
- **Sentimientos**: Positive, Negative, Neutral
- **Visualización**: Radar charts, bar charts, pie charts
- **Histórico**: Tendencias de últimos 7 días

### Gestión de Usuario
- **Registro**: Completo con datos demográficos
- **Autenticación**: Auth0 con verificación de email
- **Perfil**: Edición de nombre, foto, contraseña
- **Privacidad**: Gestión de datos y eliminación de cuenta

## 🔐 Seguridad y Privacidad

### Medidas Implementadas
- **🔐 Autenticación JWT** con Auth0
- **🛡️ Cifrado de datos** sensibles
- **🚫 No almacenamiento local** de tokens críticos
- **⏰ Expiración de sesión** (30 días)
- **🗑️ Eliminación segura** de datos de usuario

### Cumplimiento Normativo
- **GDPR compliance** para datos europeos
- **Ley 1581 de 2012** (Colombia) de protección de datos
- **Términos y condiciones** integrados
- **Política de privacidad** transparente

## 🚀 Próximas Funcionalidades

### En Desarrollo
- [ ] **Modo offline** para grabaciones
- [ ] **Exportación de datos** personal
- [ ] **Temas personalizables** de UI
- [ ] **Compartir reportes** con profesionales
- [ ] **Integración con wearables**

### Backlog
- [ ] **Análisis de voz avanzado** (tono, velocidad)
- [ ] **Recomendaciones personalizadas**
- [ ] **Comunidad anónima** de apoyo
- [ ] **API pública** para desarrolladores

## 👥 Equipo de Desarrollo

Proyecto desarrollado como trabajo de grado por estudiantes de último semestre de Ingeniería de Sistemas - Pontificia Universidad Javeriana:

- **Desarrollador Frontend Android** - [Tu nombre]
- **Desarrollador Backend** - [Nombre del compañero]
- **Especialista en NLP** - [Nombre del compañero]
- **UX/UI Designer** - [Nombre del compañero]

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE.md](LICENSE.md) para detalles.

## 🤝 Contribuciones

Aunque este es un proyecto académico, agradecemos feedback y sugerencias:

1. **Fork** el proyecto
2. **Crea** una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. **Push** a la rama (`git push origin feature/AmazingFeature`)
5. **Abre** un Pull Request

## 📞 Contacto

**Proyecto myMind**
- **Email**: myMindProject@protonmail.com
- **Universidad**: Pontificia Universidad Javeriana
- **Ubicación**: Bogotá D.C., Colombia

## 🙏 Agradecimientos

- **Pontificia Universidad Javeriana** - Institución educativa
- **Auth0** - Plataforma de autenticación
- **MPAndroidChart** - Biblioteca de gráficos
- **Material Design** - Sistema de diseño
- **The American Heart Association** - Investigación citada sobre bienestar emocional

---

<div align="center">
  <p><strong>🧠 "Conocerte a ti mismo es el primer paso para transformar tu mundo." 🧠</strong></p>
  <p>Hecho con ❤️ por estudiantes para estudiantes</p>
</div>
