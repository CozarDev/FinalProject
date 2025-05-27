@echo off
chcp 65001 >nul

echo.
echo ==========================================
echo   TurnadoApp - Configuración Completa
echo ==========================================
echo.
echo Este script configurará automáticamente:
echo - Backend (Spring Boot)
echo - Frontend (Android)
echo.

echo [1/4] Configurando Backend...
echo.
cd backend

if not exist "src\main\resources\application.properties" (
    if exist "src\main\resources\application.properties.example" (
        copy "src\main\resources\application.properties.example" "src\main\resources\application.properties" >nul
        echo ✅ application.properties configurado
    ) else (
        echo ❌ No se encontró application.properties.example
    )
) else (
    echo ⚠️  application.properties ya existe
)

if not exist "src\main\resources\firebase-service-account.json" (
    if exist "src\main\resources\firebase-service-account.json.example" (
        copy "src\main\resources\firebase-service-account.json.example" "src\main\resources\firebase-service-account.json" >nul
        echo ✅ firebase-service-account.json configurado
    ) else (
        echo ❌ No se encontró firebase-service-account.json.example
    )
) else (
    echo ⚠️  firebase-service-account.json ya existe
)

echo.
echo [2/4] Configurando Frontend...
echo.
cd ..\frontend

if not exist "app\src\main\java\com\proyectofinal\frontend\Config\ApiConfig.java" (
    if exist "app\src\main\java\com\proyectofinal\frontend\Config\ApiConfig.java.example" (
        copy "app\src\main\java\com\proyectofinal\frontend\Config\ApiConfig.java.example" "app\src\main\java\com\proyectofinal\frontend\Config\ApiConfig.java" >nul
        echo ✅ ApiConfig.java configurado
    ) else (
        echo ❌ No se encontró ApiConfig.java.example
    )
) else (
    echo ⚠️  ApiConfig.java ya existe
)

if not exist "app\google-services.json" (
    if exist "app\google-services.json.example" (
        copy "app\google-services.json.example" "app\google-services.json" >nul
        echo ✅ google-services.json configurado
    ) else (
        echo ❌ No se encontró google-services.json.example
    )
) else (
    echo ⚠️  google-services.json ya existe
)

cd ..

echo.
echo [3/4] Verificando configuración...
set backend_ok=0
set frontend_ok=0

if exist "backend\src\main\resources\application.properties" if exist "backend\src\main\resources\firebase-service-account.json" set backend_ok=1
if exist "frontend\app\src\main\java\com\proyectofinal\frontend\Config\ApiConfig.java" if exist "frontend\app\google-services.json" set frontend_ok=1

echo.
echo [4/4] Resultado de la configuración:
echo.

if %backend_ok%==1 (
    echo ✅ Backend configurado correctamente
) else (
    echo ❌ Backend: Faltan archivos de configuración
)

if %frontend_ok%==1 (
    echo ✅ Frontend configurado correctamente
) else (
    echo ❌ Frontend: Faltan archivos de configuración
)

echo.
if %backend_ok%==1 if %frontend_ok%==1 (
    echo ==========================================
    echo   ¡CONFIGURACIÓN COMPLETA!
    echo ==========================================
    echo.
    echo Próximos pasos:
    echo.
    echo 1. BACKEND:
    echo    cd backend
    echo    mvnw.cmd spring-boot:run
    echo    (Ejecuta en http://localhost:8080)
    echo.
    echo 2. FRONTEND:
    echo    cd frontend
    echo    Abrir en Android Studio
    echo    Sync Project → Run
    echo    (Se conecta a http://10.0.2.2:8080)
    echo.
    echo ¡El proyecto está listo para desarrollar!
) else (
    echo ==========================================
    echo   CONFIGURACIÓN INCOMPLETA
    echo ==========================================
    echo.
    echo Revisa que existan los archivos .example
    echo en las carpetas backend\ y frontend\
)

echo.
echo Presiona cualquier tecla para continuar...
pause >nul
