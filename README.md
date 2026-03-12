# PCBuilderShop

## 👥 Miembros del Equipo
| Nombre y Apellidos | Correo URJC | Usuario GitHub |
|:--- |:--- |:--- |
| David Díaz Gómez-Escalonilla | d.diaz.2021@alumnos.urjc.es | daviddge |
| Jonay Sebastián Ortiz Armas| js.ortiz.2023@alumnos.urjc.es | kuuharuh |
| Ramiro Daniel Flores Aquino | rd.flores.2025@alumnos.urjc.es | danilo-uni |
| Joel Domené Álvaro | j.domene.2022@alumnos.urjc.es |  joel-domene |

---

## 🎭 **Preparación: Definición del Proyecto**

### **Descripción del Tema**
Aplicación web dedicada exclusivamente a la venta de componentes de PC, orientada al sector de la informática y el hardware. La plataforma ofrece un catálogo especializado (CPU, GPU, RAM, placas base, SSDs y otros componentes) pensado para usuarios que desean montar, actualizar o personalizar su propio ordenador. Se busca aportar al usuario un entorno centrado únicamente en componentes, facilitando la comparación, selección y compra de piezas compatibles.

### **Entidades**

1. **Usuario**
2. **Producto**
3. **Pedido**
4. **Reseña**

**Relaciones entre entidades:**
- Usuario - Pedido: Cada usuario registrado puede generar uno o varios pedidos (1:N).
- Pedido - Producto: Un pedido está compuesto por distintos productos, y un mismo producto puede aparecer en varios pedidos (N:M).
- Usuario - Reseña : Un usuario puede escribir multiples reseñas sobre productos adquiridos (1:N).
- Producto - Reseña: Un producto puede tener distintas reseñas publicadas por los usuarios (1:N).

### **Permisos de los Usuarios**

**Usuario Anónimo**: 
  - Permisos: Explorar catálogo, buscar productos, registrarse/iniciar sesion.
  - No es dueño de ninguna entidad.

**Usuario Registrado**: 
  - Permisos: Gestionar su perfil, escribir reseñas y gestionar pedido.
  - Es dueño de sus propios pedidos, de su usuario y sus reseñas.

**Administrador**: 
  - Permisos: Gestión completa de productos (CRUD), visualizar estadísticas, administrar reseñas, gestionar promociones, supervisar usuarios y pedidos.
  - Tiene acceso y control sobre todas las entidades del sistema (Pedidos, Usuarios, Productos y Reseñas).

### **Imágenes**

- Usuario - Se podrá subir una imagen de perfil.
- Producto - Dispondrá de una galería de imágenes para mostrar distintos ángulos o detalles.
- Reseña - Permite añadir imagenes opcionales subidas por el usuario.

### **Gráficos**

- Evolución de ventas mensuales - Gráfico de lineas
- Stock de productos - Gráfico circular
- Distribución de pedidos por categoría - Gráfico de barras
- Valoración media de los productos - Sistemas de estrellas o barras

### **Tecnología Complementaria**

- Envío automático correos electrónicos mediante JavaMailSender.
- Generación de facturas en formato PDF usando iText o similar.

### **Algoritmo o Consulta Avanzada**

- **Algoritmo/Consulta**: Sistema de recomendaciones de distintos productos basado en el historial de compras del usuario.
- **Descripción**: Se analizarán los productos adquiridos anteriormente con el objetivo de sugerir otros productos similares o complementarios a través de técnicas de filtrado colaborativo.
- **Alternativa**: Consulta avanzada que agrupe ventas por categoría, mes y región, identificando patrones o tendencias.

---

## 🛠 **Práctica 1: Maquetación de páginas web con HTML y CSS**

### **Diagrama de Navegación**
Diagrama que muestra cómo se navega entre las diferentes páginas de la aplicación:

![Diagrama de Navegación](assets/images/readme-images/DiagramaNavegacion_PcBuilderShop.jpg)


Todos los usuarios parte desde la página principal, y hay acceso sin restricciones a las páginas de busqueda, página del producto y las pantallas de inicio de sesión y registro. Sin embargo, las páginas correspondientes al perfil, el carrito de compra, proceso de pago y crear reseñas requieren que el usuario haya iniciado su sesión. El administrador tiene acceso a todas las pantallas anteriores, además del admin-dashboard, que es el panel de control del admin donde puede administrar cada entidad de la página.

### **Capturas de Pantalla y Descripción de Páginas**

#### **1. Página Principal / Home**

Esta es la pagina principal de la pagina en la que tenemos una serie de productos recomendados, además de distintas novedades en hardware. En el header se incluye la barra de navegación, el acceso a la cesta para usuarios registrados, y boton dropdown que permite acceder al perfil o cerrar sesión. En el caso de no estar logueado, se muestra un botón "Entrar" para iniciar sesión o registrarse.

![Página Principal](assets/images/readme-images/home-page.png)
-  La pagina de incio tambien te muestra las categorias de hardware disponibles, las cuales también se pueden visualizar desde el menu desplegable del header dandole a "categorias"
![Página Principal](assets/images/readme-images/home-page-sidebar.png)

#### **RESTO DE PÁGINAS**

#### **TODOS LOS USUARIOS**

#### **2. Pagina de busqueda:**
Muestra una lista de  productos en una cuadrícula con sus datos principales. También tiene implementada una barra lateral con filtros por marca y rango de precio, además de un selector para ordenar los resultados según lo que busque el usuario.

![Pagina de busqueda](assets/images/readme-images/search-result.png)

#### **3. Pagina de Producto:**
Maquetación detallada del componente con un carrusel de imágenes y la zona de compra. Contiene una tabla para las especificaciones técnicas y una sección de reseñas donde los usuarios pueden ver las valoraciones y opiniones de otros compradores.
![Pagina de producto](assets/images/readme-images/item-detail.png)

#### **4. Pagina de Login:**
En esta pantalla nos muestra como una persona que ya tiene cuenta de la pagina puede inciar sesion añadiendo su correo y contraseña o tambien te da la opcion de iniciar sesion con tu cuenta de google.

![Pagina de Login](assets/images/readme-images/login.png)

#### **5. Pagina de Registro:**
Esta pantalla es para que las personas que no tienen cuenta creada, la puedan tener añadiendo sus datos como correo, nombre, contraseña(repetir contraseña) y aceptar por obligacion las politicas de privacidad. Aqui tambien te da la opcion como el login en la que puedes resgistrarte con google.

![Pagina de Registro](assets/images/readme-images/user-registration.png)

#### **USUARIOS REGISTRADOS**

#### **6. Pagina de Perfil:**
Muestra el perfil del usuario. Incluye 3 pestañas:
- Perfil: muestra la página de perfil pública, mostrando el nombre, la foto de perfil y las reseñas.
- Pedidos: muestra los pedidos en curso del usuario.
- Modificar cuenta: permite modificar la información de la cuenta (nombre, teléfono, email, direcciones) o borrar la cuenta.
![Pagina de Perfil-Perfil](assets/images/readme-images/profile.png)
![Pagina de Perfil-Pedidos](assets/images/readme-images/profile-orders.png)
![Pagina de Perfil-Modificar cuenta](assets/images/readme-images/profile-modify.png)

#### **7. Pagina de Carrito:**
Muestra el carrito de compra del usuario, incluyendo el precio y la cantidad de cada artículo, y permitiendo quitar artículos o finalizar la compra. También aparecen artículos recomendados en la parte inferior.
![Pagina de Carrito](assets/images/readme-images/shopping-cart.png)

#### **8. Pagina de Pago:**
En esta pantalla podemos ver como hemos implentado la forma de pago en lo que podemos seleccionar(solo una) y añadir la direccion del envio , tambien nos permite el tipo de pago (Tarjeta o PayPal) y el relleno de los datos de la tajeta. Y para hacer un seguimiento de la compra ,en el lado derecho de la pantalla se ve el resumen de la compra con el precio total y listo el boton para comprar y para salir si se arrepiente.

![Pagina de Pago](assets/images/readme-images/payment.png)

En esta pantalla basicamente nos muestra el resultado de la compra al saber que el pago a sido recibido correctamente y además nos sale la opcion de descargar el pdf para ver la factura de la compra, al lado suyo tambien con el boton de volver a la tienda para seguir comprando o viendo mas componentes.
![Pagina de Pago correcto](assets/images/readme-images/payment-correct.png)

#### **9. Pagina de Crear Review:**
Formulario para que los usuarios valoren los productos comprados. Incluye un sistema de puntuación por estrellas, campos de texto para el título y el comentario, y secciones específicas para listar pros y contras, además de permitir la subida de imágenes reales del componente.
![Pagina de Crear Review](assets/images/readme-images/create-review.png)

#### **ADMINISTRADOR**

#### **10. Pagina de Admin-Dashboard:**

Página principal del panel de administración de la tienda, donde se muestra un resumen general del sistema con diferentes estadísticas como número de productos, usuarios, pedidos e ingresos. Incluye gráficas de ventas e inventario, accesos rápidos a funciones administrativas y una tabla con los pedidos recientes.
![Pagina de Admin-Dashboard](assets/images/readme-images/admin-dashboard.png)

#### **11. Pagina de Lista Producto:**

Página de gestión de productos del panel de administración que muestra el catálogo en formato tabular con información de cada artículo (imagen, categoría, precio y stock). Permite buscar y filtrar productos, así como realizar acciones de administración como crear, editar o eliminar.
![Pagina de Lista Producto](assets/images/readme-images/admin-item-list.png)

#### **12. Pagina de Crear Producto:**

Página que permite al administrador crear un nuevo producto para el catálogo de la tienda mediante un formulario con campos como nombre, descripción, categoría, precio y stock. Incluye la opción de subir múltiples imágenes (con previsualización de estas) antes de publicar el producto.
![Pagina de Crear Producto](assets/images/readme-images/admin-item-create.png)

#### **13. Pagina de Editar Producto:**

Página que permite al administrador editar la información de un producto existente del catálogo. El formulario muestra los datos previamente rellenados (nombre, descripción, categoría, precio y stock) y permite modificarlos antes de guardar los cambios.
![Pagina de Editar Producto](assets/images/readme-images/admin-item-edit.png)

#### **14. Pagina de Lista Usuarios:**

Página de administración que muestra el listado de usuarios registrados en la plataforma junto con información como nombre, correo electrónico, rol y estado de conexión. Permite buscar y filtrar usuarios, así como realizar acciones de gestión como ver, editar, crear o eliminar cuentas.
![Pagina de Lista Usuarios](assets/images/readme-images/admin-user-list.png)

#### **15. Pagina de Editar Usuarios:**

Página que permite al administrador editar la información de un usuario existente, incluyendo nombre, correo electrónico, contraseña y rol dentro del sistema. También muestra y permite actualizar la imagen de perfil del usuario antes de guardar los cambios.

![Pagina de Editar Usuarios](assets/images/readme-images/admin-user-edit.png)

#### **16. Pagina de Lista Reseñas:**

Página del panel de administración dedicada a la gestión de reseñas de los clientes sobre los productos de la tienda. Permite filtrar opiniones, responder o editar respuestas oficiales del administrador y eliminar reseñas inapropiadas.
![Pagina de Lista Reseñas](assets/images/readme-images/admin-review-list.png)

#### **17. Pagina de Lista Pedidos:**

Página del panel de administración para la gestión de pedidos de clientes. Muestra métricas generales, herramientas de control de inventario, filtros de búsqueda y una tabla con los pedidos donde el administrador puede consultar, editar o cancelar cada pedido.
![Pagina de Lista Pedidos](assets/images/readme-images/admin-order-list.png)

### **18. Pagina de Editar Pedidos:**

Página del panel de administración que muestra el detalle de un pedido individual de un cliente. Permite consultar la información del cliente, los productos comprados y el resumen económico, así como actualizar el estado del pedido, añadir notas internas y notificar al cliente por correo.
![Pagina de Editar Pedidos](assets/images/readme-images/pedidos-edit.png)

### **19. Pagina de Pedidos Reabastecimiento:**

Página del panel de administración destinada a la gestión del reabastecimiento de inventario. Permite visualizar el pedido semanal actual, planificar nuevos pedidos para semanas futuras y consultar el historial de pedidos realizados a proveedores. También ofrece herramientas para modificar cantidades, fechas o añadir productos a los pedidos.
![Pagina de Editar Pedidos](assets/images/readme-images/pedidos-reabastecimiento.png)

### **Participación de Miembros en la Práctica 1**

#### **Alumno 1 - Ramiro Daniel Flores Aquino**

Mi participación fue el encargado de la implementacion y edición de las paginas del menú principal(index.html), implementé las paginas de login(login.html) y registro(user_registration.html) y también las paginas de pago(payment.html) y la pantalla de pago correcto(payment_correct.html).

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Estrucutura base y navegación del menu principal ](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/68e97783e08ce6d9a00c434f09738d519f64456f)  | [index.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/index.html)   |
|2| [Implementación de formulario de login y validación básica ,pudiendo el usuario tener mas privilegios](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/0fef36f48eb0db1b5feb60c2c901625b43f277dc)  | [login.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/login.html)   |
|3| [Creación de pagina de registro de usuarios, permitiendo crear la cuenta del usuario con datos personales](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/ebcfcdc8e9ab164e8b244fc76657fcf496a8a7be)  | [user_registration.html](URL_archivo_3https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/user_registration.html)   |
|4| [Integración de pasarela de pago y campos de tarjeta para que el usuario pueda tener mas libertad de elección](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/a5b9f8911bf89ee3177a08034087619ae023b767)  | [payment.html](URL_archttps://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/payment.htmlhivo_4)   |
|5| [Diseño de pantalla de confirmación de pago con exito](URL_commihttps://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/f8c43e8576d007e2877ec18462194b2d3da58fedt_5)  | [payment_correct.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/payment_correct.html)   |

---

#### **Alumno 2 - David Díaz Gómez-Escalonilla**

Principal responsable de las páginas de search-result, item-detail, create-review y los headers. También he ayudado con la estructura del index.html para que fuera similar a la de search-result, además de crear la implementacion inicial de las paginas user-list, user-edit, order-list, order-edit, las cuales han sido mejoradas posteriormente por otros compañeros. Creación y diseño del diagrama de navegación de README

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Construccion de estructura básica de search-result html, incluyendo estilos css, cabecera, pie de pagina, filtros y los primeros ejemplos de productos](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/492fb1a6cb0778e6813eb550ac9eca71604968f7)  | [search-result.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/492fb1a6cb0778e6813eb550ac9eca71604968f7/pages/search-result.html)   |
|2| [Implementacion de la pagina del producto, añadiendo también la tabla de especificaciones y la seccion de comentarios y reseñas](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/bafb9f9ef63e13400de7bd6b56348491aaad526d)  | [item-detail](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/bafb9f9ef63e13400de7bd6b56348491aaad526d/pages/item-detail.html)   |
|3| [Implementacion de la pagina de creaciond e reseñas, permitiendo crear una reseña con una descripcion y destacar pros y contras del producto](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/07b45dbbc1914c7f83cfd0d22323ff531d967da7)  | [create-review](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/07b45dbbc1914c7f83cfd0d22323ff531d967da7/pages/create-review.html)   |
|4| [Creacion, optimizacion y modularizacion de headers independientes para usuarios logueados y no logueados con el objetivo de evitar codigo duplicado](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/a6e834f70750229803f921ac1b096a6e083267ba)  | [loged_header.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/a6e834f70750229803f921ac1b096a6e083267ba/pages/headers/loged_header.html)   | [unloged_header.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/a6e834f70750229803f921ac1b096a6e083267ba/pages/headers/unloged_header.html)
|5| [Creacion y modularizacion de footer](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/d953c975fd888a16b8a9fc3214d2ce4db1270252)  | [ArchivoX](URL_archivo_5)   |
|6| [Creacion de estructura inicial basica de paginas de admin e implementacion de user-list, permitiendo visualizar todos los perfiles y su info correspondiente en una tabla](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/0b310a7a427ae7dc01e95dc00e41efc0883fc885)  | [user-list.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/0b310a7a427ae7dc01e95dc00e41efc0883fc885/pages/admin/user-list.html)   |
|7| [Implementacion de pantallas de lista de productos y edicion de productos, pudiendo visualizar informacion relevante sobre cada pedido en una tabla y esditar su estado respectivamente](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/2f55ddab736a82327ef2cccd7a5c143c610ee390)  | [order-list.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/2f55ddab736a82327ef2cccd7a5c143c610ee390/pages/admin/order-list.html)   | [order-edit.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/2f55ddab736a82327ef2cccd7a5c143c610ee390/pages/admin/order-edit.html)  


---

#### **Alumno 3 - Jonay Sebastián Ortiz Armas**

Principal responsable de las páginas de perfil y carrito de compra, además de algunos cambios en el header y la página de pago. También hice el logo de la página.

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Añadir página de perfil de usuario](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/0a580df10a0e133a60755ceea097ec4c25b4ce75)  | [profile.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/profile.html)   |
|2| [Modificar barra lateral de la página de perfil](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/51eee89e8c182d4ad7268c30c9fa9bd8d157378e)  | [profile.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/profile.html)   |
|3| [Enlazar botones de compra en la página de carrito](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/ab334d8ac5258d692de3a15f06fd1f83374687e5)  | [shopping-cart.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/shopping-cart.html)   |
|4| [Arreglar botón de buscar en el loged_header](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/4d904aa1d8b2a732518f65579b8af51760da27f7)  | [loged_header.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/headers/loged_header.html)   |
|5| [Añadir a la página de pago la función de usar una dirección guardada en el perfil](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/a6d1c3125ca831d9c63b8d6eee45fee370f235bb)  | [payment.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/payment.html)   |
|6| [Añadir favicon e incluirlo en páginas de perfil y carrito](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/09479074f8c2a0ec56a74a3dde5ef9fd143f820a)  | [logo.png](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/assets/icons/logo.png)   |
|7| [AÑadir botón de admin en páginas de perfil y carrito](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/2284a36e0debfd42c837ef0cf9801a6335478c9f)  | [profile.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/profile.html)   |
|8| [Arreglar botón de buscar en el unloged_header](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/4d904aa1d8b2a732518f65579b8af51760da27f7)  | [unloged_header.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/headers/unloged_header.html)   |

---

#### **Alumno 4 - Joel Domené Álvaro**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Panel de administración, gestión de productos, usuarios y reseñas](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/22b0c83ef82111b8a5f8d482512c4f6189fe3990)  |  [admin-dashboaard]([pages/admin/admin-dashboard.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/admin/admin-dashboard.html)) 
|1| Algunas páginas: |  [item-list.html]([pages/admin/item-list.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/admin/item-list.html))
|1|  |  [user-list.html]([pages/admin/user-list.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/admin/user-list.html))
|1|  |  [review-list]([pages/admin/review-list.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/admin/review-list.html))
|2| [Gestión de Pedidos, de usuarios y reabastecimiento](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/commit/89d7e8b39d4967bb644eaa116d14a87df00ecbfa)  |  [order-list.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/admin/order-list.html)
|2| |  [order-management.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/admin/order-management.html)
|2|  |  [order-edit.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-4/blob/main/pages/admin/order-edit.html)

---

## 🛠 **Práctica 2: Web con HTML generado en servidor**

### **Navegación y Capturas de Pantalla**

#### **Diagrama de Navegación**

Solo si ha cambiado.

#### **Capturas de Pantalla Actualizadas**

Solo si han cambiado.

### **Instrucciones de Ejecución**

#### **Requisitos Previos**
- **Java**: versión 21 o superior
- **Maven**: versión 3.8 o superior
- **MySQL**: versión 8.0 o superior
- **Git**: para clonar el repositorio

#### **Pasos para ejecutar la aplicación**

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/[usuario]/[nombre-repositorio].git
   cd [nombre-repositorio]
   ```

2. **AQUÍ INDICAR LO SIGUIENTES PASOS**

#### **Credenciales de prueba**
- **Usuario Admin**: usuario: `admin`, contraseña: `admin`
- **Usuario Registrado**: usuario: `user`, contraseña: `user`

### **Diagrama de Entidades de Base de Datos**

Diagrama mostrando las entidades, sus campos y relaciones:

![Diagrama Entidad-Relación](images/database-diagram.png)

> [Descripción opcional: Ej: "El diagrama muestra las 4 entidades principales: Usuario, Producto, Pedido y Categoría, con sus respectivos atributos y relaciones 1:N y N:M."]

### **Diagrama de Clases y Templates**

Diagrama de clases de la aplicación con diferenciación por colores o secciones:

![Diagrama de Clases](images/classes-diagram.png)

> [Descripción opcional del diagrama y relaciones principales]

### **Participación de Miembros en la Práctica 2**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

## 🛠 **Práctica 3: API REST, docker y despliegue**

### **Documentación de la API REST**

#### **Especificación OpenAPI**
📄 **[Especificación OpenAPI (YAML)](/api-docs/api-docs.yaml)**

#### **Documentación HTML**
📖 **[Documentación API REST (HTML)](https://raw.githack.com/[usuario]/[repositorio]/main/api-docs/api-docs.html)**

> La documentación de la API REST se encuentra en la carpeta `/api-docs` del repositorio. Se ha generado automáticamente con SpringDoc a partir de las anotaciones en el código Java.

### **Diagrama de Clases y Templates Actualizado**

Diagrama actualizado incluyendo los @RestController y su relación con los @Service compartidos:

![Diagrama de Clases Actualizado](images/complete-classes-diagram.png)

### **Instrucciones de Ejecución con Docker**

#### **Requisitos previos:**
- Docker instalado (versión 20.10 o superior)
- Docker Compose instalado (versión 2.0 o superior)

#### **Pasos para ejecutar con docker-compose:**

1. **Clonar el repositorio** (si no lo has hecho ya):
   ```bash
   git clone https://github.com/[usuario]/[repositorio].git
   cd [repositorio]
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**:

### **Construcción de la Imagen Docker**

#### **Requisitos:**
- Docker instalado en el sistema

#### **Pasos para construir y publicar la imagen:**

1. **Navegar al directorio de Docker**:
   ```bash
   cd docker
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**

### **Despliegue en Máquina Virtual**

#### **Requisitos:**
- Acceso a la máquina virtual (SSH)
- Clave privada para autenticación
- Conexión a la red correspondiente o VPN configurada

#### **Pasos para desplegar:**

1. **Conectar a la máquina virtual**:
   ```bash
   ssh -i [ruta/a/clave.key] [usuario]@[IP-o-dominio-VM]
   ```
   
   Ejemplo:
   ```bash
   ssh -i ssh-keys/app.key vmuser@10.100.139.XXX
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**:

### **URL de la Aplicación Desplegada**

🌐 **URL de acceso**: `https://[nombre-app].etsii.urjc.es:8443`

#### **Credenciales de Usuarios de Ejemplo**

| Rol | Usuario | Contraseña |
|:---|:---|:---|
| Administrador | admin | admin123 |
| Usuario Registrado | user1 | user123 |
| Usuario Registrado | user2 | user123 |

### **OTRA DOCUMENTACIÓN ADICIONAL REQUERIDA EN LA PRÁCTICA**

### **Participación de Miembros en la Práctica 3**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---
