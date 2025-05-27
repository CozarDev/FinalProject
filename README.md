# 🏢 TurnadoApp - Sistema de Gestión de Turnos de Trabajo

**Proyecto Final de Grado Superior en Desarrollo de Aplicaciones Multiplataforma**

Un sistema completo de gestión de turnos de trabajo que permite a las empresas administrar empleados, departamentos, turnos, partes de trabajo e incidencias laborales de manera eficiente.

## 📋 **Descripción del Proyecto**

TurnadoApp es una solución integral que combina un backend robusto con una aplicación móvil Android para gestionar todos los aspectos relacionados con los turnos de trabajo en una empresa.

### **🎯 Funcionalidades Principales**

- **👥 Gestión de Empleados**: CRUD completo con roles y departamentos
- **🏢 Gestión de Departamentos**: Organización empresarial
- **⏰ Gestión de Turnos**: Asignación y control de horarios
- **📝 Partes de Trabajo**: Registro de jornadas laborales
- **🚨 Gestión de Incidencias**: Control de problemas laborales
- **🔐 Autenticación JWT**: Sistema seguro con roles diferenciados
- **📱 App Android**: Interfaz móvil moderna y funcional

## 🏗️ **Arquitectura del Sistema**

### **Backend (Spring Boot)**
- **Framework**: Spring Boot 3.4
- **Base de Datos**: MongoDB Atlas
- **Autenticación**: JWT con roles (ADMIN, DEPARTMENT_HEAD, EMPLOYEE)
- **API**: REST con documentación completa
- **Despliegue**: Google Cloud Run
- **Seguridad**: CORS configurado, validaciones robustas

### **Frontend (Android)**
- **Lenguaje**: Java nativo
- **UI**: Material Design
- **Arquitectura**: Fragmentos con ViewPager2
- **Navegación**: BottomNavigation + TabLayout
- **API**: Retrofit para comunicación con backend

## 🚀 **Instalación y Configuración**

### **📋 Prerrequisitos**

- **Java 17+** (para backend)
- **Android Studio** (para frontend)
- **MongoDB** (local o Atlas)
- **Git**

### **⚡ Configuración Rápida**

1. **Clonar el repositorio**:
   ```bash
   git clone <url-del-repositorio>
   cd Proyecto_Final
   ```

2. **Ejecutar script de configuración**:
   
   **En Windows**:
   ```cmd
   setup.bat
   ```
   
   **En Linux/Mac**:
   ```bash
   chmod +x setup.sh
   ./setup.sh
   ```

3. **Configurar archivos de ejemplo**:
   - El script copiará automáticamente los archivos `.example`
   - Edita las configuraciones según tu entorno

### **🔧 Configuración Manual**

#### **Backend**

1. **Configurar `application.properties`**:
   ```bash
   cd backend/src/main/resources
   cp application.properties.example application.properties
   ```

2. **Configurar Firebase** (opcional):
   ```bash
   cp firebase-service-account.json.example firebase-service-account.json
   ```

3. **Ejecutar backend**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

#### **Frontend**

1. **Configurar ApiConfig**:
   ```bash
   cd frontend/app/src/main/java/com/proyectofinal/frontend/Config
   cp ApiConfig.java.example ApiConfig.java
   ```

2. **Configurar Google Services** (opcional):
   ```bash
   cd frontend/app
   cp google-services.json.example google-services.json
   ```

3. **Abrir en Android Studio**:
   - Sync Project
   - Run en emulador o dispositivo

## 🌐 **URLs y Endpoints**

### **Backend (Desarrollo)**
- **Local**: `http://localhost:8080`

### **Principales Endpoints**
- `POST /api/auth/login` - Autenticación
- `GET /api/employees` - Listar empleados
- `POST /api/work-reports` - Crear parte de trabajo
- `GET /api/incidents` - Listar incidencias
- `GET /api/shifts` - Gestión de turnos

### **Frontend (Android)**
- **Emulador**: Se conecta a `http://10.0.2.2:8080`
- **Dispositivo físico**: Configurar IP local del backend

## 👥 **Roles y Permisos**

### **🔴 ADMIN**
- Acceso completo al sistema
- Gestión de empleados y departamentos
- Configuración de turnos
- Supervisión general

### **🟡 DEPARTMENT_HEAD**
- Gestión de empleados de su departamento
- Revisión de partes de trabajo
- Gestión de incidencias departamentales

### **🟢 EMPLOYEE**
- Crear partes de trabajo propios
- Ver turnos asignados
- Reportar incidencias

## 📱 **Funcionalidades de la App Android**

### **🏠 Home**
- Dashboard con resumen de información
- Accesos rápidos a funciones principales

### **📝 Work Reports**
- Listado de partes de trabajo
- Creación de nuevos partes
- Refresh automático
- Validaciones de fecha y horario

### **🚨 Incidencias**
- Gestión de incidencias laborales
- Filtros por estado y tipo
- Asignación y seguimiento

### **⚙️ Administración** (solo ADMIN)
- Gestión de empleados
- Configuración de departamentos
- Asignación de turnos

## 🔧 **Características Técnicas**

### **Backend**
- **Validaciones**: Robustas en todos los endpoints
- **Manejo de Errores**: Respuestas consistentes
- **Logs**: Sistema completo de logging
- **Seguridad**: JWT con expiración configurable
- **CORS**: Configurado para desarrollo y producción

### **Frontend**
- **Refresh Automático**: Los datos se actualizan automáticamente
- **Manejo de Estados**: Validaciones para evitar crashes
- **UI Responsiva**: Adaptada a diferentes tamaños de pantalla
- **Navegación Fluida**: Transiciones suaves entre fragmentos

## 🐛 **Solución de Problemas**

### **Problemas Comunes**

1. **Token JWT Expirado**:
   - La app redirige automáticamente al login
   - Configurar tiempo de expiración en `application.properties`

2. **Lista no se actualiza**:
   - Implementado refresh automático en `onResume()`
   - SwipeRefresh disponible en todas las listas

3. **Error de conexión**:
   - Verificar que el backend esté ejecutándose
   - Comprobar la URL en `ApiConfig.java`

### **Logs y Debugging**

- **Backend**: Logs en consola con nivel DEBUG
- **Frontend**: Logs en Logcat con tags específicos
- **Filtros útiles**: `WorkReportListFragment`, `MainActivity`, `ApiClient`

## 📚 **Estructura del Proyecto**

```
Proyecto_Final/
├── backend/                 # Spring Boot API
│   ├── src/main/java/      # Código fuente Java
│   ├── src/main/resources/ # Configuraciones
│   └── pom.xml            # Dependencias Maven
├── frontend/               # Aplicación Android
│   ├── app/src/main/      # Código fuente Android
│   └── build.gradle       # Configuración Gradle
├── setup.sh               # Script configuración Linux/Mac
├── setup.bat              # Script configuración Windows
└── README.md              # Este archivo
```


## 🤝 **Contribución**

Este es un proyecto académico de Grado Superior. Para sugerencias o mejoras:

1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Añadir nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 **Licencia**

Proyecto académico - Grado Superior en Desarrollo de Aplicaciones Multiplataforma


**Proyecto Final de Grado Superior**  
**Desarrollo de Aplicaciones Multiplataforma**

---

⭐ **¡Gracias por revisar TurnadoApp!** ⭐