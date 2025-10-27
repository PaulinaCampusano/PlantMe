# PlantMe

# 🌿 PlantMe

**PlantMe** es una aplicación móvil desarrollada en **Kotlin (Jetpack Compose)** que permite al usuario registrar y administrar sus plantas, visualizar la lista de especies agregadas y recibir recordatorios para regarlas según su tipo y último riego.  
Su objetivo es fomentar el cuidado responsable de las plantas a través de una interfaz intuitiva, atractiva y funcional.

---

## 👩‍💻 Integrantes del equipo
- **Karol Giraldo**
- **Paulina Campusano**
---

## 🪴 Descripción del proyecto

La aplicación está compuesta por tres pantallas principales, accesibles mediante una **barra de navegación inferior (BottomBar)**:

1. **HomeScreen** → Muestra la lista de plantas registradas por el usuario y el tiempo restante para regarlas.  
   - Si una planta llega a su tiempo de riego, se muestra el botón **“Regado listo”**, el cual reinicia el contador automáticamente.
2. **AddPlantScreen** → Permite agregar una nueva planta seleccionando su nombre, tipo y fecha/hora del último riego.  
   - El intervalo de riego se calcula automáticamente según el tipo de planta elegido.
3. **AccountScreen** → Muestra la información del usuario (nombre, cantidad de plantas registradas) y opciones de configuración como **cerrar sesión** o **eliminar cuenta**.

Además, la aplicación incluye:
- **Pantalla de Login y Registro** con validación de credenciales y spinner.  
- **Animación de carga (loader)** al iniciar sesión.  
- **Fondo personalizado** y colores adaptados a la temática natural de la aplicación.

---

## ⚙️ Funcionalidades implementadas

| Categoría | Descripción |
|------------|-------------|
| **Arquitectura** | MVVM (Model - ViewModel - View) |
| **Persistencia local** | DataStore para guardar plantas y sesión |
| **Interfaz** | Jetpack Compose (Material 3) |
| **Navegación** | Navigation Compose + NavHostController |
| **Validaciones** | Email, contraseña y campos vacíos |
| **Notificaciones lógicas** | Botón dinámico “Regado listo” |
| **Diseño** | Imagen de fondo, gradientes y componentes personalizados |

---

🚀 Pasos para ejecutar el proyecto

1️⃣ Requisitos previos

Android Studio Flamingo o superior

Kotlin configurado (versión 1.9+)

Gradle actualizado (mínimo versión 8.0)

Emulador Android o dispositivo físico conectado


2️⃣ Clonar el repositorio

git clone https://github.com/usuario/PlantMe.git

3️⃣ Abrir en Android Studio

1. Abrir Android Studio


2. Seleccionar File → Open...


3. Buscar la carpeta del proyecto PlantMe


4. Esperar a que Gradle sincronice las dependencias



4️⃣ Ejecutar la aplicación

Selecciona el emulador o tu dispositivo físico

Presiona ▶️ Run App (Shift + F10)
