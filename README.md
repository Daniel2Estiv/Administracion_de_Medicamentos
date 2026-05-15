Sistema de Localización
de Dispositivos Médicos en Bodega

Tecnología de Desarrollo de Sistemas Informáticos
II Semestre 2026

Profesor	Carlos Adolfo Beltrán Castro

Asignatura	Programación Orientada A Objetos

Institución	Unidades Tecnológicas de Santander
Ciudad	Bucaramanga, Santander
Fecha	10 de mayo de 2026

Equipo de Desarrollo
Integrante 1	Oscar Javier Serrano Gómez - 1098750879

Integrante 2	Daniel Estiven Cuesta Naranjo - 1005177008

Integrante 3	Jerson Stiven Díaz Ortiz - 1095302370


Unidades Tecnológicas De Santander 
Bucaramanga – Santander 
10/05/2026
1. Descripción del Proyecto

El proyecto consiste en el desarrollo de una aplicación de escritorio en Java orientada a la gestión y consulta de dispositivos médicos almacenados en una bodega. El sistema permite registrar, organizar y buscar insumos médicos mediante un código único, mostrando de forma rápida la información del producto y su ubicación exacta dentro del almacén.
La aplicación tiene como propósito optimizar el proceso de búsqueda, identificación y alistamiento de los dispositivos médicos, reduciendo el tiempo empleado por el personal encargado y mejorando la organización de la bodega. A través de una interfaz sencilla e intuitiva, los usuarios pueden consultar datos relevantes de cada insumo —como nombre, lote, cantidad y ubicación—, facilitando así el control y acceso a los productos almacenados.
El sistema fue desarrollado utilizando Programación Orientada a Objetos (POO) en Java, lo que permite una estructura organizada, reutilizable y escalable bajo una arquitectura por capas (Modelo / DAO / Vista). Adicionalmente, el proyecto se gestiona mediante un repositorio en GitHub para llevar control de versiones y facilitar el trabajo colaborativo durante el desarrollo.
Con esta solución se busca mejorar la eficiencia operativa dentro de la bodega, minimizar errores en la localización de productos y apoyar los procesos logísticos relacionados con el manejo de dispositivos médicos en el sector salud.
<img width="875" height="469" alt="image" src="https://github.com/user-attachments/assets/0379b676-ea77-4ad8-8824-2cf9bb469fc7" />
2. Objetivos

2.1 Objetivo General

Desarrollar una aplicación de escritorio en Java SE que permita la gestión integral del inventario de dispositivos médicos almacenados en una bodega, facilitando el registro, consulta y localización de los productos a través de una interfaz amigable y un modelo de datos centralizado.
2.2 Objetivos Específicos

•	Implementar un módulo de autenticación de usuarios con control de roles (ADMIN, OPERADOR y LECTURA).

•	Diseñar una base de datos relacional en SQLite que modele productos, lotes, ubicaciones y catálogos auxiliares.

•	Construir interfaces gráficas en Java Swing con navegación lateral tipo dashboard.

•	Implementar operaciones CRUD para Insumos, Medicamentos, Bienes Devolutivos, Ubicaciones y Usuarios.

•	Incorporar alertas visuales para lotes próximos a vencer.

•	Aplicar buenas prácticas de seguridad: hashing de contraseñas (SHA-256), comparación constant-time y consultas parametrizadas.

•	Documentar el proceso y publicar el proyecto en un repositorio GitHub.

3. Alcance del Sistema
   
El sistema cubre las funcionalidades nucleares de gestión de inventario de una bodega de dispositivos médicos. Se delimita el alcance del proyecto en los siguientes puntos:
Incluido
•	Autenticación local de usuarios contra base de datos SQLite.

•	CRUD completo de productos (insumos, medicamentos y bienes devolutivos).

•	Gestión de lotes con fecha de vencimiento, existencias y costo por lote.

•	Catálogos de presentaciones, tipos de producto y ubicaciones físicas.

•	Alertas de vencimiento visibles desde el panel de inicio.

•	Administración de usuarios con roles diferenciados.

4. Arquitectura de la Aplicación
   
La aplicación sigue una arquitectura por capas que separa claramente las responsabilidades de presentación, lógica de acceso a datos y modelo de dominio. La estructura interna del proyecto está organizada bajo el paquete raíz Centro, dividido en cuatro subpaquetes:
Capa	Paquete	Responsabilidad
Vista	Centro.Interfaz	Ventanas Swing (Login, MenuPrincipal y paneles CRUD)
Modelo	Centro.modelo	Clases de dominio o DTO (Producto, LoteExistencia, Usuario, etc.)
DAO	Centro.dao	Acceso a la base de datos mediante PreparedStatement
Utilidades	Centro.util	Conexión a BD, hashing de contraseñas y tema visual

<img width="632" height="243" alt="image" src="https://github.com/user-attachments/assets/042f02dc-3711-4fe4-82b2-92c774d230c4" />


5. Estructura del Proyecto e Interfaz de Usuario
   
5.1 Ventana de Login

Punto de entrada del sistema. Solicita las credenciales del usuario y, tras verificarlas contra la tabla usuarios, redirige al menú principal con el rol correspondiente.
Componentes

•	Icono de usuario — Imagen decorativa del formulario

•	JTextField — Campo de ingreso del nombre de usuario

•	JPasswordField — Campo de ingreso de contraseña (oculta)

•	JButton: Ingresar — Botón principal que dispara la autenticación

<img width="719" height="447" alt="image" src="https://github.com/user-attachments/assets/b3ccc751-9973-4a1d-a899-50575ec14b1a" />

5.2 Menú Principal y Panel de Inicio

Ventana central del sistema. Cuenta con un panel lateral de navegación (CardLayout) y un área de contenido dinámica donde se muestra cada módulo.
Panel de Opciones

•	Inicio — Dashboard con tarjetas de resumen y alertas

•	Insumos — CRUD de insumos médicos

•	Medicamentos — CRUD de medicamentos

•	Bienes Devolutivos — CRUD de bienes devolutivos

•	Ubicaciones — CRUD de ubicaciones físicas

•	Usuarios — Administración de usuarios (solo ADMIN)

•	Cerrar Sesión — Salida controlada con confirmación
Panel de Inicio

•	JPanel de tarjetas — Tarjetas resumen para cada módulo del sistema

•	JTable de alertas — Listado de lotes próximos a vencer, ordenados por fecha

<img width="875" height="525" alt="image" src="https://github.com/user-attachments/assets/7ce998d5-84a0-4078-863b-b55696bef572" />

5.3 Módulo de Insumos

Permite administrar los insumos médicos no consumibles por dosis (gasas, vendas, jeringas, etc.). Cada insumo puede tener uno o más lotes con fecha de vencimiento y existencias.
Componentes

•	Panel de búsqueda — JTextField + botón Buscar para filtrar por nombre o código

•	JTable de insumos — Listado tabular con código, INVIMA, nombre, presentación y datos del lote

•	Formulario de producto — Código, INVIMA, nombre, presentación (JComboBox); botones Nuevo, Guardar, Eliminar

•	Formulario de lote — Lote, vencimiento, existencias, costo; botones Nuevo, Guardar, Eliminar

<img width="906" height="544" alt="image" src="https://github.com/user-attachments/assets/582fe31b-2fd9-43b3-b935-ba408ba9b512" />

5.4 Módulo de Medicamentos

Idéntico en estructura al módulo de Insumos, pero filtra automáticamente los productos cuyo tipo es Medicamento. Resulta útil para llevar un control independiente del stock de medicamentos, sus lotes y sus fechas de vencimiento, donde el seguimiento del vencimiento es especialmente crítico.
Componentes

•	Panel de búsqueda — JTextField + botón Buscar para filtrar medicamentos

•	JTable de medicamentos — Listado tabular específico de medicamentos

•	Formulario de producto — Código, INVIMA, nombre, presentación; botones Nuevo, Guardar, Eliminar

•	Formulario de lote — Lote, vencimiento, existencias, costo; botones Nuevo, Guardar, Eliminar

<img width="906" height="600" alt="image" src="https://github.com/user-attachments/assets/8e8cd6be-5429-4a9f-b8d6-f23d710249b6" />

5.5 Módulo de Bienes Devolutivos

Gestiona los bienes devolutivos: equipos médicos no consumibles que deben rastrearse para su devolución (camillas, monitores, equipos quirúrgicos, etc.). Comparte la estructura general de los módulos anteriores.
Componentes

•	Panel de búsqueda — JTextField + botón Buscar para filtrar bienes devolutivos

•	JTable de bienes — Listado tabular específico de bienes devolutivos

•	Formulario de producto — Código, INVIMA, nombre, presentación; botones Nuevo, Guardar, Eliminar

•	Formulario de lote — Lote, vencimiento, existencias, costo; botones Nuevo, Guardar, Eliminar

<img width="875" height="572" alt="image" src="https://github.com/user-attachments/assets/2851628b-df7d-4f7c-be4e-98529d8b3a49" />

5.6 Módulo de Ubicaciones

Cataloga las ubicaciones físicas de la bodega (bodegas, pasillos, racks, niveles). Cada lote del inventario hace referencia a una ubicación para localizar rápidamente el producto.
Componentes

•	JTable de ubicaciones — Listado tabular de todas las ubicaciones registradas

•	Formulario de descripción — JTextField con la descripción libre de la ubicación (ej. "Bodega A - Pasillo 3")

•	Botonera — Botones Nuevo, Guardar y Eliminar para operar la tabla

<img width="844" height="547" alt="image" src="https://github.com/user-attachments/assets/3d1615f0-84e1-4930-870b-552167aa5a59" />

5.7 Módulo de Usuarios

Reservado para usuarios con rol ADMIN, este módulo permite crear, editar, activar y desactivar las cuentas de los demás usuarios del sistema. Las contraseñas se almacenan cifradas con SHA-256 y nunca se exhiben en texto plano.
Componentes

•	JTable de usuarios — Listado de todos los usuarios con su rol y estado

•	JTextField: Usuario — Nombre de usuario único

•	JPasswordField: Contraseña — Contraseña en claro al crear o cambiar (mínimo 4 caracteres)

•	JComboBox: Rol — ADMIN / OPERADOR / LECTURA

•	JCheckBox: Activo — Marca si la cuenta está habilitada

•	Botonera — Nuevo, Guardar, Desactivar y Limpiar

<img width="906" height="588" alt="image" src="https://github.com/user-attachments/assets/bf874c1c-95d0-462c-8e6f-19440cbf5636" />

6. Tecnologías Utilizadas
   
El proyecto se apoya en tecnologías estándar de la industria que permiten una ejecución ligera y sin dependencias de servidor. La siguiente tabla resume las herramientas utilizadas y su propósito en el ciclo de vida del proyecto.
<img width="628" height="342" alt="image" src="https://github.com/user-attachments/assets/978c3282-21bc-475f-9de3-e30a8121a9e9" />
<img width="625" height="93" alt="image" src="https://github.com/user-attachments/assets/ccca23c3-c6ab-419d-a748-8e0282b2ab85" />

7. Requisitos del Sistema
   
Requisitos de Hardware

•	Procesador: Intel/AMD de doble núcleo o superior

•	Memoria RAM: Mínimo 2 GB (recomendado 4 GB)

•	Disco duro: Espacio libre de al menos 200 MB

•	Pantalla: Resolución mínima de 1024 × 768
Requisitos de Software

•	Sistema operativo: Windows 10/11, Linux o macOS

•	Java: JDK 17 o superior (probado en JDK 24)

•	IDE: Apache NetBeans 18 o superior (recomendado)

•	Librerías: sqlite-jdbc-3.44.1.0.jar o superior

•	Opcional: flatlaf-3.x.x.jar para la apariencia moderna

Instalación del Proyecto
    
Los siguientes pasos describen la instalación completa del proyecto desde cero. Una vez instaladas las herramientas básicas, el sistema queda listo para ejecutarse en cualquier equipo compatible.

1.	Descargar e instalar el JDK de Java (versión 17 o superior) desde el sitio oficial de Oracle o Adoptium.
   
3.	Descargar e instalar Apache NetBeans IDE (https://netbeans.apache.org).
   
5.	Descargar el archivo sqlite-jdbc-3.44.1.0.jar (o superior) desde https://github.com/xerial/sqlite-jdbc/releases.
   
7.	(Opcional) Descargar flatlaf-3.x.x.jar desde https://github.com/JFormDesigner/FlatLaf/releases para activar el tema moderno.
   
9.	Clonar el repositorio del proyecto desde GitHub mediante git clone o descargarlo como archivo ZIP.
    
11.	Descomprimir el proyecto si se descargó como ZIP y abrirlo en NetBeans con Archivo → Abrir Proyecto.
    
13.	Agregar el driver JDBC: clic derecho sobre el proyecto → Properties → Libraries → Add JAR/Folder → seleccionar sqlite-jdbc.jar.
    
15.	Esperar a que NetBeans cargue las librerías y los archivos del proyecto.
    
Ejecución del Sistema

9.	Abrir el proyecto en NetBeans.
    
11.	Verificar que la clase principal sea Centro.Interfaz.Login (Properties → Run → Main Class).
    
13.	Ejecutar el proyecto presionando F6 o haciendo clic en el botón Run.
    
15.	En la ventana de Login, ingresar las credenciales por defecto (admin / admin123).
    
17.	Una vez dentro, navegar entre los módulos desde el menú lateral.
    
19.	Cerrar sesión con el botón Cerrar Sesión cuando se termine de operar.
    
Credenciales por Defecto

Usuario	- admin

Contraseña	- admin123

Rol	- ADMIN

Funcionalidades disponibles para el Usuario

•	Registrar dispositivos médicos (insumos, medicamentos y bienes devolutivos).

•	Consultar insumos mediante el código o nombre del producto.

•	Visualizar la ubicación de cada producto dentro de la bodega.

•	Gestionar el inventario: agregar, editar y eliminar productos y lotes.

•	Recibir alertas visuales de los lotes próximos a vencer.

•	Administrar usuarios del sistema (solo rol ADMIN).

10. Diagrama Entidad-Relación
    
El modelo de datos del sistema consta de seis entidades organizadas en tres capas funcionales: catálogos (TipoProducto, Presentacion, Ubicacion), entidad principal (Producto), existencias (LoteExistencia) y administración (usuarios). Todas las relaciones son de tipo 1 a N y la integridad referencial se garantiza mediante claves foráneas activadas en cada conexión.

<img width="969" height="613" alt="image" src="https://github.com/user-attachments/assets/364ac4be-49bf-40c7-abb0-9ee846701f0c" />

Descripción de las Entidades

•	TipoProducto — Clasifica los productos en INSUMO, MEDICAMENTO o BIEN DEVOLUTIVO.

•	Presentacion — Define la presentación física del producto (caja, tableta, ampolla, etc.).

•	Producto — Entidad principal con el detalle del dispositivo médico (código, INVIMA, nombre).

•	LoteExistencia — Lotes específicos de un producto con su fecha de vencimiento, existencias y costo.

•	Ubicacion — Ubicación física dentro de la bodega donde se almacena el lote.

•	usuarios — Usuarios del sistema con su rol (ADMIN, OPERADOR, LECTURA) y estado.

Cardinalidades

•	TipoProducto → Producto (1 : N) — Un tipo clasifica muchos productos

•	Presentacion → Producto (1 : N) — Una presentación se usa en muchos productos

•	Producto → LoteExistencia  (1 : N) — Un producto tiene varios lotes a lo largo del tiempo

•	Ubicacion → LoteExistencia  (1 : N) — Una ubicación puede almacenar varios lotes

11. Script SQL de la Base de Datos
    
El siguiente script reconstruye el modelo de datos completo del sistema en SQLite, incluyendo tablas, claves foráneas, índices de desempeño y datos semilla (tipos de producto, presentaciones, ubicaciones y el usuario administrador por defecto). El script es idempotente: usa CREATE TABLE IF NOT EXISTS e INSERT OR IGNORE, por lo que puede ejecutarse varias veces sin duplicar información.

-- ============================================================
--  ESQUEMA DE BASE DE DATOS — Centro Logístico
--  Motor:   SQLite 3.x
--  Archivo: inventario_general_2.db
--  Fecha:   2026-05-10
-- ============================================================
--  Este script reconstruye el modelo de datos productivo del
--  sistema (tablas TipoProducto, Presentacion, Ubicacion,
--  Producto, LoteExistencia, usuarios). Los nombres se
--  conservan tal y como los consume el código Java de los DAO.
-- ============================================================
 
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
 
-- ── Limpieza opcional (úsala con cuidado) ─────────────────────
-- DROP TABLE IF EXISTS LoteExistencia;
-- DROP TABLE IF EXISTS Producto;
-- DROP TABLE IF EXISTS Presentacion;
-- DROP TABLE IF EXISTS TipoProducto;
-- DROP TABLE IF EXISTS Ubicacion;
-- DROP TABLE IF EXISTS usuarios;
 
-- ============================================================
--  1) Catálogos
-- ============================================================
 
CREATE TABLE IF NOT EXISTS TipoProducto (
    id_tipo INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre  TEXT    NOT NULL UNIQUE
);
 
CREATE TABLE IF NOT EXISTS Presentacion (
    id_presentacion INTEGER PRIMARY KEY AUTOINCREMENT,
    descripcion     TEXT    NOT NULL UNIQUE
);
 
CREATE TABLE IF NOT EXISTS Ubicacion (
    id_ubicacion INTEGER PRIMARY KEY AUTOINCREMENT,
    descripcion  TEXT    NOT NULL UNIQUE
);
 
-- ============================================================
--  2) Productos del inventario
-- ============================================================
 
CREATE TABLE IF NOT EXISTS Producto (
    id_producto     INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo          TEXT    NOT NULL,
    invima          TEXT,
    nombre          TEXT    NOT NULL,
    id_presentacion INTEGER NOT NULL,
    id_tipo         INTEGER NOT NULL,
    FOREIGN KEY (id_presentacion) REFERENCES Presentacion(id_presentacion),
    FOREIGN KEY (id_tipo)         REFERENCES TipoProducto(id_tipo)
);
 
-- ============================================================
--  3) Existencias y lotes
-- ============================================================
 
CREATE TABLE IF NOT EXISTS LoteExistencia (
    id_lote           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_producto       INTEGER NOT NULL,
    id_ubicacion      INTEGER NOT NULL,
    lote              TEXT,
    fecha_vencimiento TEXT,
    existencias       INTEGER NOT NULL DEFAULT 0,
    costo             REAL    NOT NULL DEFAULT 0.0,
    FOREIGN KEY (id_producto)  REFERENCES Producto(id_producto)   ON DELETE CASCADE,
    FOREIGN KEY (id_ubicacion) REFERENCES Ubicacion(id_ubicacion) ON DELETE SET NULL
);
 
-- ============================================================
--  4) Usuarios y autenticación
-- ============================================================
 
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario    INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,
    rol           TEXT    NOT NULL DEFAULT 'OPERADOR'
                  CHECK (rol IN ('ADMIN','OPERADOR','LECTURA')),
    activo        INTEGER NOT NULL DEFAULT 1
);
 
-- ============================================================
--  5) Índices de desempeño
-- ============================================================
 
CREATE INDEX IF NOT EXISTS idx_producto_nombre   ON Producto(nombre);
CREATE INDEX IF NOT EXISTS idx_producto_codigo   ON Producto(codigo);
CREATE INDEX IF NOT EXISTS idx_producto_tipo     ON Producto(id_tipo);
CREATE INDEX IF NOT EXISTS idx_lote_vencimiento  ON LoteExistencia(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_lote_producto     ON LoteExistencia(id_producto);
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);
 
-- ============================================================
--  6) Datos semilla
-- ============================================================
 
--  Tipos de producto
INSERT OR IGNORE INTO TipoProducto(id_tipo, nombre) VALUES
    (1, 'Bienes  devolutivos'),
    (2, 'Medicamento'),
    (3, 'Insumos');
 
--  Presentaciones
INSERT OR IGNORE INTO Presentacion(id_presentacion, descripcion) VALUES
    (1,  'Lata 8 onzas'),
    (2,  'Caja x 50'),
    (3,  'Frasco x 400 ml'),
    (4,  'Paquete'),
    (5,  'Tableta'),
    (6,  'Ampolla'),
    (7,  'Jarabe'),
    (8,  'Crema'),
    (9,  'Sobre'),
    (10, 'Unidad'),
    (11, 'Caja'),
    (12, 'Galón'),
    (13, 'Botella'),
    (14, 'Bolsa');
 
--  Ubicaciones
INSERT OR IGNORE INTO Ubicacion(id_ubicacion, descripcion) VALUES
    (1,  'Depósito Sur - Pasillo A'),
    (2,  'Centro Logístico - Zona 2'),
    (3,  'Centro Logistico  FCV'),
    (4,  'Almacén Central - Rack 1'),
    (5,  'Depósito Norte - Nivel 2'),
    (6,  'Bodega A - Pasillo 1'),
    (7,  'Bodega A - Pasillo 2'),
    (8,  'Bodega B - Rack 3'),
    (9,  'Bodega B - Rack 4'),
    (10, 'Bodega C - Nivel 1'),
    (11, 'Bodega C - Nivel 2'),
    (12, 'Cuarentena'),
    (13, 'Devoluciones');
 
--  Usuario administrador por defecto

--  Contraseña: admin123  (SHA-256, sin sal — sólo compatibilidad)

INSERT OR IGNORE INTO usuarios(username, password_hash, rol, activo) VALUES
    ('admin',
     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
     'ADMIN',
     1);
 
-- ============================================================
--  Fin del script
-- ============================================================
 
12. Conclusiones
    
El desarrollo del Sistema de Localización de Dispositivos Médicos en Bodega permitió aplicar los conocimientos adquiridos a lo largo del semestre en una solución concreta y útil para el sector salud. La aplicación cumple con los objetivos planteados: ofrece un panel administrativo funcional, con CRUD completo para Usuarios y para los tres tipos de productos manejados en la bodega, navegación moderna entre secciones y una salida de sesión controlada.
Durante la implementación se aplicaron buenas prácticas de ingeniería de software: arquitectura por capas, uso de PreparedStatement para prevenir inyección SQL, hashing de contraseñas con SHA-256 y comparación constant-time para mitigar timing attacks, validaciones de entrada robustas y manejo consistente de recursos con try-with-resources.
El uso de SQLite como motor embebido demostró ser una decisión acertada: la aplicación funciona sin configuración previa de servidor y puede desplegarse en cualquier equipo con la JVM instalada, lo cual la hace ideal para entornos académicos, demostraciones y despliegues ligeros en bodegas reales.
Como trabajo futuro se proponen mejoras tales como la exportación de reportes a Excel y PDF, un log de auditoría de cambios, backups automáticos diarios de la base de datos y la migración del esquema de hashing a un algoritmo más robusto (PBKDF2 o Argon2).
