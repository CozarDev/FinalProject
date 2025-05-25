@echo off
echo Configurando variables de entorno para desarrollo local...

REM Configurar MongoDB Atlas URI (obtenido desde Google Cloud Secrets)
for /f "delims=" %%i in ('gcloud secrets versions access latest --secret="mongodb-uri"') do set MONGODB_URI=%%i

REM Configurar JWT Secret (obtenido desde Google Cloud Secrets)  
for /f "delims=" %%i in ('gcloud secrets versions access latest --secret="jwt-secret"') do set JWT_SECRET=%%i

REM Configurar base de datos
set MONGODB_DATABASE=db_fproject

REM Configurar Firebase (temporal - sin Firebase por ahora)
set FIREBASE_CREDENTIALS_PATH=

echo Variables configuradas:
echo MONGODB_URI: %MONGODB_URI:~0,50%...
echo JWT_SECRET: %JWT_SECRET:~0,20%...
echo MONGODB_DATABASE: %MONGODB_DATABASE%

echo.
echo Iniciando aplicación con perfil firebase...
mvn spring-boot:run -Dspring-boot.run.profiles=firebase 