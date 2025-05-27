#!/bin/bash

echo ""
echo "=========================================="
echo "  TurnadoApp - Configuración Completa"
echo "=========================================="
echo ""
echo "Este script configurará automáticamente:"
echo "- Backend (Spring Boot)"
echo "- Frontend (Android)"
echo ""

echo "[1/4] Configurando Backend..."
echo ""
cd backend

if [ ! -f "src/main/resources/application.properties" ]; then
    if [ -f "src/main/resources/application.properties.example" ]; then
        cp "src/main/resources/application.properties.example" "src/main/resources/application.properties"
        echo "✅ application.properties configurado"
    else
        echo "❌ No se encontró application.properties.example"
    fi
else
    echo "⚠️  application.properties ya existe"
fi

if [ ! -f "src/main/resources/firebase-service-account.json" ]; then
    if [ -f "src/main/resources/firebase-service-account.json.example" ]; then
        cp "src/main/resources/firebase-service-account.json.example" "src/main/resources/firebase-service-account.json"
        echo "✅ firebase-service-account.json configurado"
    else
        echo "❌ No se encontró firebase-service-account.json.example"
    fi
else
    echo "⚠️  firebase-service-account.json ya existe"
fi

echo ""
echo "[2/4] Configurando Frontend..."
echo ""
cd ../frontend

if [ ! -f "app/src/main/java/com/proyectofinal/frontend/Config/ApiConfig.java" ]; then
    if [ -f "app/src/main/java/com/proyectofinal/frontend/Config/ApiConfig.java.example" ]; then
        cp "app/src/main/java/com/proyectofinal/frontend/Config/ApiConfig.java.example" "app/src/main/java/com/proyectofinal/frontend/Config/ApiConfig.java"
        echo "✅ ApiConfig.java configurado"
    else
        echo "❌ No se encontró ApiConfig.java.example"
    fi
else
    echo "⚠️  ApiConfig.java ya existe"
fi

if [ ! -f "app/google-services.json" ]; then
    if [ -f "app/google-services.json.example" ]; then
        cp "app/google-services.json.example" "app/google-services.json"
        echo "✅ google-services.json configurado"
    else
        echo "❌ No se encontró google-services.json.example"
    fi
else
    echo "⚠️  google-services.json ya existe"
fi

cd ..

echo ""
echo "[3/4] Verificando configuración..."
backend_ok=0
frontend_ok=0

if [ -f "backend/src/main/resources/application.properties" ] && [ -f "backend/src/main/resources/firebase-service-account.json" ]; then
    backend_ok=1
fi

if [ -f "frontend/app/src/main/java/com/proyectofinal/frontend/Config/ApiConfig.java" ] && [ -f "frontend/app/google-services.json" ]; then
    frontend_ok=1
fi

echo ""
echo "[4/4] Resultado de la configuración:"
echo ""

if [ $backend_ok -eq 1 ]; then
    echo "✅ Backend configurado correctamente"
else
    echo "❌ Backend: Faltan archivos de configuración"
fi

if [ $frontend_ok -eq 1 ]; then
    echo "✅ Frontend configurado correctamente"
else
    echo "❌ Frontend: Faltan archivos de configuración"
fi

echo ""
if [ $backend_ok -eq 1 ] && [ $frontend_ok -eq 1 ]; then
    echo "=========================================="
    echo "  ¡CONFIGURACIÓN COMPLETA!"
    echo "=========================================="
    echo ""
    echo "Próximos pasos:"
    echo ""
    echo "1. BACKEND:"
    echo "   cd backend"
    echo "   ./mvnw spring-boot:run"
    echo "   (Ejecuta en http://localhost:8080)"
    echo ""
    echo "2. FRONTEND:"
    echo "   cd frontend"
    echo "   Abrir en Android Studio"
    echo "   Sync Project → Run"
    echo "   (Se conecta a http://10.0.2.2:8080)"
    echo ""
    echo "¡El proyecto está listo para desarrollar!"
else
    echo "=========================================="
    echo "  CONFIGURACIÓN INCOMPLETA"
    echo "=========================================="
    echo ""
    echo "Revisa que existan los archivos .example"
    echo "en las carpetas backend/ y frontend/"
fi

echo ""
echo "Presiona Enter para continuar..."
read
