# Gestión de Eventos

Sistema de gestión de eventos desarrollado en Java que permite administrar usuarios, eventos, ubicaciones e inscripciones de asistentes.

El sistema cuenta con funcionalidades diferenciadas para usuarios y administradores, incluyendo la creación y gestión de eventos, control de asistencia y administración de ubicaciones.

## 📋 Descripción

La aplicación permite a los usuarios:

- Iniciar sesión.
- Consultar los eventos disponibles.
- Inscribirse a eventos.
- Cancelar su inscripción.
- Consultar los eventos a los que están inscritos.

Los administradores pueden:

- Crear, modificar y eliminar eventos.
- Agregar o eliminar usuarios de un evento.
- Crear, modificar y eliminar ubicaciones.
- Gestionar la información relacionada con los eventos.

El sistema también aplica reglas para evitar conflictos de horarios y controlar la capacidad de los eventos y ubicaciones.

---

## 🚀 Funcionalidades

### 👤 Usuarios

- Inicio de sesión.
- Visualización de eventos.
- Inscripción a eventos.
- Cancelación de inscripción.
- Control de asistencia.

### 🔐 Administradores

- Crear eventos.
- Editar eventos.
- Eliminar eventos.
- Agregar usuarios a eventos.
- Remover usuarios de eventos.
- Crear ubicaciones.
- Editar ubicaciones.
- Eliminar ubicaciones.

### 📅 Eventos

Cada evento contiene información como:

- Nombre.
- Descripción.
- Fecha.
- Hora de inicio.
- Hora de finalización.
- Ubicación.
- Límite de asistentes.

### 📍 Ubicaciones

Las ubicaciones cuentan con:

- Nombre.
- Dirección.
- Capacidad máxima.

---

## ⚙️ Reglas de negocio

El sistema debe garantizar las siguientes condiciones:

1. **No pueden existir eventos simultáneos en la misma ubicación.**

2. **Un usuario no puede estar inscrito en dos eventos que ocurran al mismo tiempo.**

3. **La cantidad de asistentes no puede superar el límite establecido para el evento.**

4. **Un evento no puede superar la capacidad máxima de la ubicación asignada.**

5. **Un evento debe tener una ubicación válida antes de ser creado.**

6. **La fecha y hora del evento deben ser válidas.**

7. **Los usuarios solamente pueden realizar las acciones permitidas según su rol.**

---

## 🏗️ Tecnologías utilizadas

- **Java**
- **Maven**
- **JPA / Hibernate**
- **Base de datos relacional**
- **Git / GitHub**

---

## 📁 Estructura del proyecto

```text
gestion-eventos/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── eventos/
│   │               ├── entities/
│   │               ├── controllers/
│   │               ├── services/
│   │               ├── repositories/
│   │               └── ...
│   │
│   └── test/
│       └── java/
│
├── pom.xml
├── README.md
└── ...
```

### Principales paquetes

| Paquete | Responsabilidad |
|---|---|
| `entities` | Contiene las entidades del sistema |
| `controllers` | Gestiona las solicitudes de la aplicación |
| `services` | Contiene la lógica de negocio |
| `repositories` | Gestiona el acceso a los datos |

---

## 🧩 Entidades principales

El sistema está compuesto principalmente por las siguientes entidades:

### Usuario

Representa a las personas que utilizan el sistema.

Un usuario puede tener diferentes permisos dependiendo de su rol.

### Evento

Representa una actividad que se realizará en una fecha, hora y ubicación determinadas.

### Ubicación

Representa el lugar donde se realizará un evento y posee una capacidad máxima de asistentes.

### Inscripción

Representa la relación entre un usuario y un evento al que se encuentra inscrito.

---

## 🔄 Flujo principal

### Usuario

```text
Iniciar sesión
      ↓
Consultar eventos
      ↓
Seleccionar evento
      ↓
Ver disponibilidad
      ↓
Inscribirse
      ↓
Asistir al evento
```

También puede cancelar su inscripción antes del evento.

### Administrador

```text
Iniciar sesión
      ↓
Panel administrativo
      ↓
Gestionar ubicaciones
      ↓
Crear evento
      ↓
Seleccionar ubicación
      ↓
Establecer fecha y hora
      ↓
Validar disponibilidad
      ↓
Publicar evento
```

---

## 🛠️ Instalación

### Requisitos

Antes de ejecutar el proyecto se necesita tener instalado:

- Java JDK
- Maven
- Git
- Una base de datos compatible con el proyecto

Puedes comprobar las instalaciones utilizando:

```bash
java -version
mvn -version
git --version
```

### Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
```

Entrar al proyecto:

```bash
cd gestion-eventos
```

### Compilar el proyecto

```bash
mvn clean install
```

### Ejecutar el proyecto

```bash
mvn spring-boot:run
```

---

## 🧪 Pruebas

Para ejecutar las pruebas automatizadas:

```bash
mvn test
```

Las pruebas permiten verificar el correcto funcionamiento de las principales reglas de negocio y funcionalidades del sistema.

---

## 📊 Arquitectura

El proyecto utiliza una arquitectura organizada por responsabilidades:

```text
Cliente
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
Base de datos
```

### Controller

Recibe las solicitudes y devuelve las respuestas correspondientes.

### Service

Contiene la lógica de negocio y las validaciones del sistema.

### Repository

Se encarga de las operaciones relacionadas con la persistencia de datos.

### Entity

Representa las entidades que serán almacenadas en la base de datos.

---

## 👥 Roles

El sistema contempla dos tipos principales de usuarios:

### Usuario

Puede:

- Iniciar sesión.
- Consultar eventos.
- Inscribirse.
- Cancelar inscripciones.

### Administrador

Además de las funcionalidades del usuario, puede:

- Gestionar eventos.
- Gestionar ubicaciones.
- Administrar asistentes de los eventos.

---

## 📐 Diagramas

La documentación del proyecto incluye:

- Diagrama Entidad-Relación (ER).
- Diagrama de clases UML.
- Diagrama de casos de uso.
- Diagramas de flujo de los principales procesos.

---

## 🔮 Posibles mejoras

Entre las funcionalidades que podrían incorporarse posteriormente:

- Notificaciones por correo electrónico.
- Confirmación de asistencia mediante código QR.
- Historial de eventos.
- Sistema de búsqueda y filtros.
- Reportes de asistencia.
- Recuperación de contraseña.
- Panel administrativo con estadísticas.
- Control de permisos más detallado.

---

Proyecto académico de gestión de eventos desarrollado como parte del aprendizaje de desarrollo de software.

---

## 📄 Licencia

Este proyecto fue desarrollado con fines educativos.
