#!/bin/bash

# Script para configurar secretos en Google Cloud Secret Manager
# Proyecto Final - Backend Spring Boot

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_ID="proyecto-final-estech"

echo -e "${BLUE}🔐 Configurando secretos en Google Cloud${NC}"
echo "============================================="

# Verificar autenticación
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q .; then
    echo -e "${YELLOW}⚠️  No estás autenticado en gcloud${NC}"
    gcloud auth login
fi

# Configurar proyecto
gcloud config set project $PROJECT_ID

# Habilitar Secret Manager API
echo -e "${BLUE}📋 Habilitando Secret Manager API...${NC}"
gcloud services enable secretmanager.googleapis.com

# Función para crear secreto
create_secret() {
    local secret_name=$1
    local description=$2
    
    if gcloud secrets describe $secret_name >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  El secreto '$secret_name' ya existe${NC}"
        read -p "¿Quieres actualizarlo? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            return 0  # Continuar para actualizar
        else
            return 1  # Saltar
        fi
    else
        echo -e "${BLUE}📝 Creando secreto '$secret_name'...${NC}"
        gcloud secrets create $secret_name --replication-policy="automatic"
        return 0
    fi
}

# Configurar MongoDB URI
echo ""
echo -e "${BLUE}1. Configurar MongoDB URI${NC}"
echo "Necesitas la connection string de MongoDB Atlas"
echo "Ejemplo: mongodb+srv://usuario:password@cluster.xxxxx.mongodb.net/db_fproject?retryWrites=true&w=majority"

if create_secret "mongodb-uri" "Connection string para MongoDB Atlas"; then
    echo "Introduce la MongoDB connection string:"
    read -s mongodb_uri
    echo "$mongodb_uri" | gcloud secrets versions add mongodb-uri --data-file=-
    echo -e "${GREEN}✅ MongoDB URI configurada${NC}"
fi

# Configurar JWT Secret
echo ""
echo -e "${BLUE}2. Configurar JWT Secret${NC}"

if create_secret "jwt-secret" "Clave secreta para firmar tokens JWT"; then
    # Generar automáticamente una clave JWT segura
    jwt_secret=$(openssl rand -base64 64)
    echo "$jwt_secret" | gcloud secrets versions add jwt-secret --data-file=-
    echo -e "${GREEN}✅ JWT Secret configurado automáticamente${NC}"
fi

# Configurar permisos
echo ""
echo -e "${BLUE}3. Configurando permisos...${NC}"

# Obtener número del proyecto
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")

# Dar permisos al servicio de Cloud Run para acceder a los secretos
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"

echo -e "${GREEN}✅ Permisos configurados${NC}"

# Verificar secretos
echo ""
echo -e "${BLUE}4. Verificando secretos creados...${NC}"
gcloud secrets list

echo ""
echo -e "${GREEN}🎉 ¡Configuración de secretos completada!${NC}"
echo "============================================="
echo "Los siguientes secretos están disponibles:"
echo "• mongodb-uri: Connection string de MongoDB Atlas"
echo "• jwt-secret: Clave para firmar tokens JWT"
echo ""
echo "Ahora puedes ejecutar el despliegue con: ./deploy.sh" 