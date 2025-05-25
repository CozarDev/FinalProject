#!/bin/bash

# Script de despliegue para Google Cloud Run
# Proyecto Final - Backend Spring Boot

set -e  # Salir si hay error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
PROJECT_ID="proyecto-final-estech"
SERVICE_NAME="proyecto-final-backend"
REGION="europe-west1"

echo -e "${BLUE}🚀 Iniciando despliegue en Google Cloud Run${NC}"
echo "=================================================="

# Verificar que gcloud está instalado
if ! command -v gcloud &> /dev/null; then
    echo -e "${RED}❌ Error: gcloud CLI no está instalado${NC}"
    echo "Instala gcloud desde: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Verificar que estamos autenticados
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q .; then
    echo -e "${YELLOW}⚠️  No estás autenticado en gcloud${NC}"
    echo "Ejecutando: gcloud auth login"
    gcloud auth login
fi

# Configurar proyecto
echo -e "${BLUE}🔧 Configurando proyecto...${NC}"
gcloud config set project $PROJECT_ID
gcloud config set run/region $REGION

# Verificar que las APIs están habilitadas
echo -e "${BLUE}📋 Verificando APIs necesarias...${NC}"
REQUIRED_APIS=(
    "run.googleapis.com"
    "cloudbuild.googleapis.com"
    "containerregistry.googleapis.com"
    "secretmanager.googleapis.com"
)

for api in "${REQUIRED_APIS[@]}"; do
    if gcloud services list --enabled --filter="name:$api" --format="value(name)" | grep -q "$api"; then
        echo -e "${GREEN}✅ $api habilitada${NC}"
    else
        echo -e "${YELLOW}⚠️  Habilitando $api...${NC}"
        gcloud services enable $api
    fi
done

# Limpiar builds anteriores
echo -e "${BLUE}🧹 Limpiando archivos temporales...${NC}"
if [ -d "target" ]; then
    rm -rf target
fi

# Ejecutar tests
echo -e "${BLUE}🧪 Ejecutando tests...${NC}"
if mvn test -q; then
    echo -e "${GREEN}✅ Tests pasaron correctamente${NC}"
else
    echo -e "${RED}❌ Los tests fallaron${NC}"
    read -p "¿Continuar con el despliegue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Build y despliegue
echo -e "${BLUE}🏗️  Iniciando build y despliegue...${NC}"
gcloud builds submit --config cloudbuild.yaml .

# Verificar despliegue
echo -e "${BLUE}🔍 Verificando despliegue...${NC}"
SERVICE_URL=$(gcloud run services describe $SERVICE_NAME --format="value(status.url)")

if [ -n "$SERVICE_URL" ]; then
    echo -e "${GREEN}✅ Despliegue exitoso!${NC}"
    echo "=================================================="
    echo -e "${GREEN}🌐 URL del servicio: $SERVICE_URL${NC}"
    echo -e "${GREEN}📊 Métricas: https://console.cloud.google.com/run/detail/$REGION/$SERVICE_NAME/metrics${NC}"
    echo -e "${GREEN}📋 Logs: gcloud run services logs tail $SERVICE_NAME${NC}"
    echo "=================================================="
    
    # Test básico del endpoint
    echo -e "${BLUE}🔄 Probando endpoint de salud...${NC}"
    if curl -s -f "$SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "${GREEN}✅ Servicio responde correctamente${NC}"
    else
        echo -e "${YELLOW}⚠️  Servicio desplegado pero no responde al health check${NC}"
        echo "Verifica los logs: gcloud run services logs tail $SERVICE_NAME"
    fi
    
else
    echo -e "${RED}❌ Error en el despliegue${NC}"
    echo "Revisa los logs: gcloud builds list"
    exit 1
fi

# Mostrar información útil
echo ""
echo -e "${BLUE}📝 Comandos útiles:${NC}"
echo "• Ver logs en tiempo real: gcloud run services logs tail $SERVICE_NAME"
echo "• Ver métricas: gcloud run services describe $SERVICE_NAME"
echo "• Actualizar secretos: gcloud secrets versions add NOMBRE_SECRETO --data-file=-"
echo "• Rollback: gcloud run services update-traffic $SERVICE_NAME --to-revisions=REVISION_NAME=100"

echo ""
echo -e "${GREEN}🎉 ¡Despliegue completado exitosamente!${NC}" 