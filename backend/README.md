# Backend - Proyecto Final EStech

API REST desarrollada con Spring Boot para gestión de turnos de trabajo, desplegada en Google Cloud Run con MongoDB Atlas.

## 🏗️ Arquitectura

- **Backend**: Spring Boot 3.2 + MongoDB
- **Database**: MongoDB Atlas (Cloud)
- **Deployment**: Google Cloud Run
- **Authentication**: JWT
- **Build**: Maven + Docker

## 🚀 Despliegue en Cloud Run

### Configuración Inicial (Solo una vez)

1. **Configurar MongoDB Atlas**:
   ```bash
   # Sigue la guía completa en CONFIGURACION_CLOUD_RUN.md
   ```

2. **Configurar secretos en Google Cloud**:
   ```bash
   ./setup-secrets.sh
   ```

3. **Desplegar aplicación**:
   ```bash
   ./deploy.sh
   ```

### Comandos Rápidos

```bash
# Desplegar nueva versión
./deploy.sh

# Ver logs en tiempo real
gcloud run services logs tail proyecto-final-backend

# Ver estado del servicio
gcloud run services describe proyecto-final-backend

# Actualizar un secreto
echo "nuevo-valor" | gcloud secrets versions add SECRET_NAME --data-file=-
```

## 🔧 Desarrollo Local

### Requisitos
- Java 21+
- Maven 3.8+
- MongoDB local (opcional, se puede usar Atlas)

### Configuración Local
```bash
# 1. Clonar repositorio
git clone <tu-repo>
cd backend

# 2. Configurar MongoDB local (opcional)
# La app usará MongoDB Atlas por defecto

# 3. Ejecutar aplicación
mvn spring-boot:run

# 4. La API estará disponible en http://localhost:8080
```

### Variables de Entorno Locales
```bash
# Opcional: usar MongoDB local
export SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/db_fproject

# Opcional: JWT personalizado
export JWT_SECRET=tu-clave-secreta
```

## 📡 API Endpoints

### Autenticación
```
POST /api/auth/login
POST /api/auth/register
```

### Empleados
```
GET    /api/employees
POST   /api/employees
GET    /api/employees/{id}
PUT    /api/employees/{id}
DELETE /api/employees/{id}
```

### Turnos
```
GET /api/shift-assignments/employee/{employeeId}
GET /api/shift-assignments/employee/{employeeId}/current-month
GET /api/shift-assignments/employee/{employeeId}/month/{year}/{month}
```

### Excepciones (Vacaciones/Festivos)
```
GET /api/shift-exceptions/employee/{employeeId}
GET /api/shift-exceptions/employee/{employeeId}/month/{year}/{month}
```

### Tipos de Turno
```
GET /api/shifttypes
```

### Debug (Solo desarrollo)
```
GET /api/debug/test-calendarific
GET /api/debug/force-import-holidays
```

## 🔐 Seguridad

- **JWT**: Autenticación basada en tokens
- **CORS**: Configurado para permitir requests desde la app Android
- **Secret Manager**: Credenciales seguras en Google Cloud
- **MongoDB Atlas**: Conexión cifrada y autenticada

## 📊 Monitoreo

### Logs
```bash
# Ver logs en tiempo real
gcloud run services logs tail proyecto-final-backend

# Ver logs históricos
gcloud run services logs read proyecto-final-backend --limit=100
```

### Métricas
- **URL**: https://console.cloud.google.com/run/detail/europe-west1/proyecto-final-backend/metrics
- **Health Check**: `GET /actuator/health`

## 🛠️ Troubleshooting

### Error de conexión a MongoDB
```bash
# Verificar secreto
gcloud secrets versions access latest --secret="mongodb-uri"

# Verificar logs
gcloud run services logs tail proyecto-final-backend
```

### Error de autenticación JWT
```bash
# Verificar secreto JWT
gcloud secrets versions access latest --secret="jwt-secret"
```

### Error en el despliegue
```bash
# Ver builds recientes
gcloud builds list --limit=5

# Ver logs de build específico
gcloud builds log BUILD_ID
```

## 📱 Integración con Android

La app Android debe usar la URL del servicio Cloud Run:

```java
// En ApiClient.java
private static final String BASE_URL = "https://proyecto-final-backend-XXXXXXXXX-ew.a.run.app/";
```

Para obtener la URL:
```bash
gcloud run services describe proyecto-final-backend --format="value(status.url)"
```

## 🔄 CI/CD

### Despliegue Automático
1. Push a branch `main` → Trigger automático en Cloud Build
2. Build de imagen Docker
3. Despliegue en Cloud Run
4. Health check automático

### Rollback
```bash
# Ver revisiones
gcloud run revisions list --service=proyecto-final-backend

# Rollback a revisión anterior
gcloud run services update-traffic proyecto-final-backend \
    --to-revisions=REVISION_NAME=100
```

## 💰 Costos

- **Cloud Run**: Gratis hasta 2M requests/mes
- **MongoDB Atlas M0**: Gratis (512MB)
- **Cloud Build**: Gratis hasta 120 builds/día
- **Secret Manager**: ~$0.06/mes por secreto

**Total estimado**: $0-5/mes para desarrollo

## 📚 Documentación Adicional

- [Guía completa de configuración](CONFIGURACION_CLOUD_RUN.md)
- [API Documentation](http://localhost:8080/swagger-ui.html) (desarrollo local)
- [Google Cloud Run Docs](https://cloud.google.com/run/docs)
- [MongoDB Atlas Docs](https://docs.atlas.mongodb.com/) 