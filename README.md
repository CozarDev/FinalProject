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
- **Firebase**: ⚠️ **COMPLETAMENTE OPCIONAL** - Solo necesario para notificaciones push

### 📱 **Detección Automática Android**

El frontend detecta automáticamente:
- **Emulador Android**: Usa `http://10.0.2.2:8080/`
- **Dispositivo físico**: Usa `http://192.168.1.100:8080/` (cambiar IP si es necesario)

### 🔥 **Firebase (Notificaciones Push) - OPCIONAL**

Firebase **NO es necesario** para que el proyecto funcione. Si no lo configuras:

- ✅ **Funcionará**: Login, registro, empleados, turnos, todas las funciones principales
- ❌ **No funcionará**: Solo las notificaciones push

**Para habilitar notificaciones push:**

**Backend:**
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un proyecto
3. Ve a Configuración > Cuentas de servicio
4. Descarga `firebase-service-account.json`
5. Copia el archivo a `backend/src/main/resources/`

**Frontend Android:**
1. En el mismo proyecto Firebase
2. Ve a Configuración > Aplicaciones
3. Añade una app Android con package: `com.proyectofinal.frontend`
4. Descarga `google-services.json`
5. Copia el archivo a `frontend/app/`
6. Rebuild el proyecto en Android Studio

**Logs que verás:**

**Backend:**
- Con Firebase: `🔥 Firebase: HABILITADO - Notificaciones push disponibles`
- Sin Firebase: `🚫 Firebase: DESHABILITADO - Solo funciones básicas disponibles`

**Frontend Android:**
- Con Firebase: `🔥 Firebase: HABILITADO - Notificaciones push configuradas`
- Sin Firebase: `🚫 Firebase: NO DISPONIBLE - Las notificaciones push están deshabilitadas`

**¡Ambos casos son completamente normales y funcionales!**

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
- **Firebase Admin SDK**

### Frontend
- **Android SDK**
- **Java**
- **Material Design**
- **Retrofit** (HTTP client)
- **Firebase FCM**

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
# Verifica que MongoDB esté ejecutándose
mongosh # Si conecta, MongoDB está activo

# Si no está activo:
# Windows: Inicia el servicio MongoDB
# macOS/Linux: sudo systemctl start mongod
```

### Frontend no se conecta al backend
```java
// Verifica la IP en ApiConfig.java
// Para dispositivo físico, cambia la IP por la de tu ordenador:
private static final String LOCAL_DEVICE_URL = "http://TU_IP_AQUI:8080/";

// Encuentra tu IP:
// Windows: ipconfig
// macOS/Linux: ifconfig
```

### Puerto 8080 ocupado
```bash
# Cambia el puerto en application.properties
server.port=8081

# Y actualiza ApiConfig.java en Android:
private static final String EMULATOR_URL = "http://10.0.2.2:8081/";
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

---

⭐ **¡Si te gusta el proyecto, dale una estrella!** ⭐