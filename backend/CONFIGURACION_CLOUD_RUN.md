# Configuración Google Cloud Run + MongoDB Atlas

## Paso 1: Configurar MongoDB Atlas

### 1.1 Crear Cluster en MongoDB Atlas
1. Ve a [MongoDB Atlas](https://cloud.mongodb.com/)
2. Crea una cuenta o inicia sesión
3. Crea un nuevo cluster:
   - **Cluster Type**: M0 Sandbox (gratis)
   - **Cloud Provider**: Google Cloud Platform
   - **Region**: europe-west1 (mismo que Cloud Run)
   - **Cluster Name**: `cluster-proyecto-final`

### 1.2 Configurar Acceso a la Base de Datos
1. **Database Access**:
   - Crea un usuario de base de datos
   - Username: `admin-proyectofinal`
   - Password: Genera una contraseña segura y guárdala
   - Role: `Atlas admin`

2. **Network Access**:
   - Añade `0.0.0.0/0` (permite acceso desde cualquier IP)
   - Esto es necesario para Cloud Run ya que las IPs son dinámicas

### 1.3 Obtener Connection String
1. Ve a **Clusters** → **Connect** → **Connect your application**
2. Selecciona **Java** y **4.3 or later**
3. Copia la connection string, se verá así:
   ```
   mongodb+srv://admin-proyectofinal:<password>@cluster-proyecto-final.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```

## Paso 2: Configurar Google Cloud

### 2.1 Crear Proyecto en Google Cloud
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto:
   - **Project Name**: `proyecto-final-estech`
   - **Project ID**: `proyecto-final-estech` (debe ser único)

### 2.2 Habilitar APIs Necesarias
```bash
# Cloud Run API
gcloud services enable run.googleapis.com

# Cloud Build API
gcloud services enable cloudbuild.googleapis.com

# Container Registry API
gcloud services enable containerregistry.googleapis.com
```

### 2.3 Configurar gcloud CLI
```bash
# Instalar gcloud CLI si no lo tienes
# https://cloud.google.com/sdk/docs/install

# Autenticarse
gcloud auth login

# Configurar proyecto
gcloud config set project proyecto-final-estech

# Configurar región por defecto
gcloud config set run/region europe-west1
```

## Paso 3: Configurar Variables de Entorno

### 3.1 Variables Secretas en Google Cloud
```bash
# Crear secreto para MongoDB URI
echo "mongodb+srv://admin-proyectofinal:TU_PASSWORD@cluster-proyecto-final.xxxxx.mongodb.net/db_fproject?retryWrites=true&w=majority" | gcloud secrets create mongodb-uri --data-file=-

# Crear secreto para JWT
echo "clave-super-secreta-para-generar-tokens-jwt-segura-firebase-123456" | gcloud secrets create jwt-secret --data-file=-

# Dar permisos para acceder a los secretos
gcloud projects add-iam-policy-binding proyecto-final-estech \
    --member="serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
```

### 3.2 Actualizar cloudbuild.yaml con secretos
Actualiza la sección de variables de entorno en `cloudbuild.yaml`:
```yaml
- '--set-env-vars=SPRING_PROFILES_ACTIVE=firebase'
- '--set-secrets=MONGODB_URI=mongodb-uri:latest,JWT_SECRET=jwt-secret:latest'
```

## Paso 4: Despliegue

### 4.1 Configurar Cloud Build Trigger
1. Ve a **Cloud Build** → **Triggers**
2. Crea un nuevo trigger:
   - **Event**: Push to branch
   - **Source**: Conecta tu repositorio GitHub
   - **Branch**: `^main$`
   - **Configuration**: Cloud Build configuration file
   - **Location**: `cloudbuild.yaml`

### 4.2 Despliegue Manual (Primera vez)
```bash
# Desde la raíz del proyecto backend
gcloud builds submit --config cloudbuild.yaml .
```

### 4.3 Verificar Despliegue
```bash
# Ver servicios de Cloud Run
gcloud run services list

# Ver logs del servicio
gcloud run services logs read proyecto-final-backend --limit=50
```

## Paso 5: Configurar Android App

### 5.1 Obtener URL del Servicio
```bash
# Obtener URL del servicio desplegado
gcloud run services describe proyecto-final-backend --format="value(status.url)"
```

### 5.2 Actualizar URL en Android
En tu app Android, actualiza la URL base:
```java
// En tu clase ApiClient o similar
private static final String BASE_URL = "https://proyecto-final-backend-XXXXXXXXX-ew.a.run.app/";
```

## Paso 6: Monitoreo y Logs

### 6.1 Ver Logs en Tiempo Real
```bash
# Logs de Cloud Run
gcloud run services logs tail proyecto-final-backend

# Logs de Cloud Build
gcloud builds list
gcloud builds log BUILD_ID
```

### 6.2 Métricas y Monitoring
1. Ve a **Cloud Run** → **proyecto-final-backend** → **Metrics**
2. Configura alertas si es necesario

## Paso 7: Variables de Entorno de Producción

En Cloud Run, las siguientes variables se configurarán automáticamente:
- `SPRING_PROFILES_ACTIVE=firebase`
- `MONGODB_URI` (desde Secret Manager)
- `JWT_SECRET` (desde Secret Manager)
- `PORT=8080` (automático en Cloud Run)

## Comandos Útiles

```bash
# Redesplegar forzando nueva build
gcloud run deploy proyecto-final-backend --source . --region=europe-west1

# Ver configuración del servicio
gcloud run services describe proyecto-final-backend

# Actualizar variables de entorno
gcloud run services update proyecto-final-backend \
    --set-env-vars="NUEVA_VAR=valor"

# Ver revisiones del servicio
gcloud run revisions list --service=proyecto-final-backend
```

## Troubleshooting

### Problema: Conexión a MongoDB falla
- Verifica que la IP 0.0.0.0/0 esté en Network Access
- Verifica que la connection string sea correcta
- Revisa logs: `gcloud run services logs read proyecto-final-backend`

### Problema: Build falla
- Verifica que todas las APIs estén habilitadas
- Revisa logs de build: `gcloud builds list` → `gcloud builds log BUILD_ID`

### Problema: App Android no conecta
- Verifica la URL del servicio Cloud Run
- Asegúrate de que `--allow-unauthenticated` esté configurado

## Costos Estimados

- **MongoDB Atlas M0**: Gratis (512MB)
- **Cloud Run**: Gratis hasta 2M requests/mes
- **Cloud Build**: Gratis hasta 120 builds/día
- **Secret Manager**: ~$0.06/mes por secreto

**Total estimado para desarrollo**: $0-5/mes 