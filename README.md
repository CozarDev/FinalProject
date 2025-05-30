# 🚀 TurnadoApp - Sistema de Gestión de Turnos de Trabajo

Una aplicación completa para gestionar turnos de trabajo, empleados y horarios, desarrollada con **Spring Boot** (backend) y **Android nativo** (frontend).

![TurnadoApp](https://img.shields.io/badge/TurnadoApp-v1.0-blue) ![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green) ![Android](https://img.shields.io/badge/Android-API%2021+-brightgreen) ![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green)

## 📋 Características

- **Gestión de Empleados**: Crear, editar y administrar empleados
- **Gestión de Turnos**: Asignación y control de turnos de trabajo
- **Autenticación JWT**: Sistema seguro de login y registro
- **Notificaciones Push**: Firebase Cloud Messaging
- **Interfaz Android Nativa**: Material Design
- **API REST**: Backend completo con Spring Boot
- **Base de Datos MongoDB**: Almacenamiento eficiente

## ⚡ INSTALACIÓN RÁPIDA - ¡SIN SCRIPTS!

### 🎯 Solo necesitas hacer esto:

### 1️⃣ **BACKEND** (API REST)

```bash
# 1. Clona el repositorio
git clone url que aparece en el botón verde del repositorio o descargar zip
cd FinalProject/backend

# 2. Copia el archivo de configuración (quita .example)
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 3. Instala y ejecuta MongoDB localmente y crea un instancia para la base de datos
# Windows: descarga desde https://www.mongodb.com/try/download/community
# macOS: brew install mongodb-community
# Ubuntu: sudo apt install mongodb

# 4. Ejecuta el backend
./mvnw spring-boot:run
```

**¡Ya está!** El backend estará ejecutándose en `http://localhost:8080`

### 2️⃣ **FRONTEND** (Aplicación Android)

```bash
# 1. Ve al directorio frontend
cd ../frontend

# 2. Copia el archivo de configuración (quita .example)
cp app/src/main/java/com/proyectofinal/frontend/config/ApiConfig.java.example app/src/main/java/com/proyectofinal/frontend/config/ApiConfig.java

# 3. Abre el proyecto en Android Studio
# File > Open > Selecciona la carpeta 'frontend'

# 4. Ejecuta la aplicación
# Presiona el botón ▶️ (Run) en Android Studio
```

**¡Ya está!** La app se conectará automáticamente al backend local.

---

## 🔧 CONFIGURACIÓN AUTOMÁTICA

### ✅ **FUNCIONA SIN CAMBIOS**

Los archivos `.example` están **completamente configurados** para funcionar inmediatamente:

- **Backend**: Se conecta a MongoDB local en puerto 27017
- **Frontend**: Se conecta automáticamente al backend (detecta emulador vs dispositivo)
- **Base de datos**: Se crea automáticamente al iniciar el backend
- **Firebase**: ⚠️ **COMENTADO POR DEFECTO** - Las notificaciones push están deshabilitadas

### 📱 **Detección Automática Android**

El frontend detecta automáticamente:
- **Emulador Android**: Usa `http://10.0.2.2:8080/`
- **Dispositivo físico**: Usa `http://192.168.1.100:8080/` (cambiar IP si es necesario)

### 🔥 **Firebase (Notificaciones Push) - DESHABILITADO POR DEFECTO**

**Estado actual: Firebase está COMENTADO**
- ✅ **Funcionará**: Login, registro, empleados, turnos, todas las funciones principales
- ❌ **No funcionará**: Solo las notificaciones push
- 📱 **Logs que verás**: "🚫 Firebase: DESHABILITADO - Solo funciones básicas disponibles"

**Para HABILITAR notificaciones push (completamente opcional):**

**Paso 1 - Backend:**
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un proyecto
3. Ve a Configuración > Cuentas de servicio
4. Descarga `firebase-service-account.json`
5. Copia el archivo a `backend/src/main/resources/`

**Paso 2 - Frontend Android:**
1. En el mismo proyecto Firebase
2. Ve a Configuración > Aplicaciones
3. Añade una app Android con package: `com.proyectofinal.frontend`
4. Descarga `google-services.json`
5. Copia el archivo a `frontend/app/`

**Paso 3 - Descomenta código Firebase:**
6. En `frontend/app/build.gradle.kts`: Descomenta las líneas Firebase
7. En `frontend/app/src/main/java/.../MainActivity.java`: Descomenta las líneas Firebase
8. En `frontend/app/src/main/java/.../Services/TurnadoFirebaseMessagingService.java`: Descomenta TODO el archivo
9. En `frontend/app/src/main/AndroidManifest.xml`: Descomenta las configuraciones Firebase
10. Rebuild el proyecto en Android Studio

**Logs tras habilitar Firebase:**
- **Backend**: `🔥 Firebase: HABILITADO - Notificaciones push disponibles`
- **Frontend**: `🔥 Firebase: HABILITADO - Notificaciones push configuradas`

---

## 📁 Estructura del Proyecto

```
TurnadoApp/
├── backend/                          # API REST con Spring Boot
│   ├── src/main/resources/
│   │   ├── application.properties.example          # ✅ Configuración lista para usar
│   │   └── firebase-service-account.json.example   # ⚠️ OPCIONAL (solo para notificaciones push)
│   └── pom.xml                       # Dependencias Maven
├── frontend/                         # Aplicación Android
│   ├── app/src/main/java/com/proyectofinal/frontend/config/
│   │   └── ApiConfig.java.example    # ✅ Configuración automática
│   └── build.gradle                  # Configuración Android
└── README.md                         # Este archivo
```

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Java 21**
- **Spring Boot 3.x**
- **Spring Security** (JWT)
- **Spring Data MongoDB**
- **Maven**
- **Firebase Admin SDK** (comentado por defecto)

### Frontend
- **Android SDK**
- **Java**
- **Material Design**
- **Retrofit** (HTTP client)
- **Firebase FCM** (comentado por defecto)

### Base de Datos
- **MongoDB 7.0+**

---

## 🔐 Configuración de Seguridad

### JWT (JSON Web Tokens)
- **Clave secreta**: Configurada automáticamente para desarrollo
- **Expiración**: 24 horas
- **Headers**: `Authorization: Bearer <token>`

### CORS
- **Desarrollo**: Permite todas las origenes (`*`)

---

## 🐛 Troubleshooting

### Backend no se conecta a MongoDB
```bash
# Verificar que MongoDB está ejecutándose
# Asegúrate de que esté en puerto 27017 (puerto por defecto)
```

### App Android no se conecta al backend
```bash
# Si usas dispositivo físico, cambia la IP en ApiConfig.java:
# Encuentra tu IP local con: ipconfig (Windows) o ifconfig (Mac/Linux)
# Cambia "192.168.1.100" por tu IP real
```

### ¿Cómo sé si Firebase está funcionando?
```bash
# Backend logs:
# ✅ "🔥 Firebase: HABILITADO" = Funcionando
# ❌ "🚫 Firebase: DESHABILITADO" = Solo funciones básicas (normal por defecto)

# Frontend logs:
# ✅ "🔥 Firebase: HABILITADO" = Notificaciones disponibles  
# ❌ "🚫 Firebase: NO DISPONIBLE" = Solo funciones básicas (normal por defecto)
```

### Puerto 8080 ocupado
```bash
# Cambia el puerto en application.properties
server.port=8081

**¡El proyecto está diseñado para funcionar inmediatamente sin configuración adicional!**

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

⭐ **¡Si te gusta el proyecto, dale una estrella!** ⭐