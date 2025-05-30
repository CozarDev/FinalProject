plugins {
    alias(libs.plugins.android.application)
    
    // 🔥 FIREBASE - NOTIFICACIONES PUSH (COMPLETAMENTE OPCIONAL) 🔥
    // DESCOMENTA la siguiente línea SOLO si quieres habilitar notificaciones push:
    // 1. Primero configura google-services.json (ver google-services.json.example)
    // 2. Luego descomenta esta línea y las dependencias de Firebase más abajo
    // 3. Rebuild el proyecto
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.proyectofinal.frontend"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.proyectofinal.frontend"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Dependencias para el sistema de incidencias
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.fragment:fragment:1.6.2")
    
    // JWT para decodificar tokens
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    
    // TapTargetView para guías visuales
    implementation(libs.tap.target.view)
    
    // 🔥 FIREBASE DEPENDENCIES - NOTIFICACIONES PUSH (COMENTADAS POR DEFECTO) 🔥
    // DESCOMENTA las siguientes líneas SOLO después de configurar google-services.json:
    // implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // implementation("com.google.firebase:firebase-messaging")
    // implementation("com.google.firebase:firebase-analytics")
    
    // Core library desugaring para compatibilidad con Java 8
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}