🌟 README – PLANTME (FRONTEND / ANDROID)
📱 Jetpack Compose • MVVM • Retrofit • DataStore • JWT
<div align="center">

# 🌱 PlantMe – Asistente Inteligente de Riego
# La app que te recuerda cuándo regar tus plantas 🌿💧
</div>

📌 Nuestra App

Las personas suelen olvidar regar sus plantas o no saber cada cuánto hacerlo. Esto provoca que muchas mueran innecesariamente.

PlantMe nace para resolver ese problema mediante:

Registro de plantas con especie y frecuencia.

Cálculo automático de próxima fecha de riego.

Contadores dinámicos en tiempo real.

Persistencia de datos entre sesiones.

Autenticación segura con JWT.

🧩 Arquitectura del Proyecto

PlantMe utiliza el patrón MVVM, lo cual permite:

Separar UI, lógica y datos.

Facilitar escalabilidad.

Mejorar mantenibilidad.

Habilitar pruebas unitarias efectivas.

🛠 Tecnologías Principales
Capa	Tecnología
Lenguaje	Kotlin
UI	Jetpack Compose
Estado	ViewModel + StateFlow
Persistencia	DataStore
Networking	Retrofit + GSON
Seguridad	JWT
Testing	JUnit

🔗 Integración con el Backend (Microservicios)

La aplicación móvil se comunica con una API REST desarrollada en Spring Boot.

⚠ Requisito obligatorio

➡️ Para que el FRONT funcione, el BACKEND debe estar levantado en localhost:8080.

📡 URL utilizada por Retrofit

En emuladores Android:

http://10.0.2.2:8080/api/

🔐  Manejo de Seguridad – JWT

Al iniciar sesión:

El servidor entrega un JWT

El token se guarda en DataStore

Se envía en cada request:

Authorization: Bearer <token>


Ejemplo en ViewModel:

private suspend fun getAuthHeader(): String? {
    val token = dataStore.data.first()[JWT_TOKEN_KEY]
    return token?.let { "Bearer $it" }
}

🧪  Pruebas Unitarias (IE 3.2.2)

Se implementaron pruebas para:

✔ Lógica de riego
✔ Cálculo de días restantes
✔ Frecuencias por especie
✔ Validaciones de formulario
✔ Formateo de UI
✔ Lógica de reinicio de contador

Ubicación:

app/src/test/java/com/example/plantme_grupo8/

▶️  Ejecución del Proyecto (Pasos de instalación)
🔧 Requisitos

Android Studio Flamingo o superior

SDK 33+

Backend funcionando en localhost:8080

🚀 Pasos
git clone https://github.com/PaulinaCampusano/PlantMe.git


Abrir en Android Studio

Instalar dependencias con Gradle

Levantar backend antes de correr la app

Ejecutar con un emulador o dispositivo físico

✔ Flujo funcional

Registro

Login

Crear planta

Ver contadores dinámicos

Presionar “Regar” → contador se reinicia correctamente

👥 Integrantes del equipo

Paulina Campusano

Karol Giraldo	

<div align="center">

# 🌿 PlantMe — Cuidar tus plantas nunca fue tan fácil
</div>
