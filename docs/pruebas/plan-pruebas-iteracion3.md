# Plan de pruebas resumido — Iteracion 3: pagos mensuales

## Objetivo y alcance

Verificar el alta de membresias, el registro y consulta de pagos mensuales, la validacion de errores, la unicidad/idempotencia, las restricciones del esquema MySQL y el acceso del personal por sesion/rol. El cobro de salida por estancia permanece separado.

## Ambiente

- Backend: Java 21 y Spring Boot 3.4.5.
- Pruebas automatizadas: `mvn test`, H2 en memoria.
- Prueba de motor: MySQL 8.4 en contenedor desechable, puerto local limitado a `127.0.0.1`.
- Datos: usuarios de prueba sinteticos; no usar datos productivos.

## Casos ejecutados

| ID | Caso | Resultado esperado | Resultado observado |
|---|---|---|---|
| PT-01 | Ejecutar `mysql/tables.sql` | Crear tablas, claves, restricciones e indices en MySQL 8.4. | Conforme; esquema cargado. |
| PT-02 | Inscribir usuario existente | Primera solicitud 201; repetir la misma inscripcion 200 con el mismo registro. | Conforme: 201 / 200. |
| PT-03 | Registrar pago con membresia que cubre el periodo | 201 y un registro pagado con ID persistido. | Conforme: 201. |
| PT-04 | Repetir pago con exactamente los mismos datos | 200, mismo ID y sin duplicado. | Conforme secuencialmente y en carrera concurrente. |
| PT-05 | Reutilizar usuario/periodo cambiando el valor | 409; no modificar el pago original. | Conforme: 409. |
| PT-06 | Registrar periodo futuro o valor cero | 400 con error de validacion. | Conforme: ambas solicitudes 400. |
| PT-07 | Enviar 20 pagos identicos concurrentes | Una respuesta 201, 19 respuestas 200, un solo ID y una fila. | Conforme: 201 x1, 200 x19, una fila. |
| PT-08 | Insertar valor cero directamente en MySQL | Rechazo por `ck_pago_mensual_valor`. | Conforme; restriccion CHECK rechaza. |
| PT-09 | Insertar pago con usuario inexistente | Rechazo por clave foranea. | Conforme; FK rechaza. |
| PT-10 | Consultar pagos por usuario/periodo mediante `EXPLAIN` | Uso del indice unico compuesto de usuario y periodo. | Conforme; usa `uq_pago_mensual_usuario_periodo`. |
| PT-11 | Ejecutar suite automatizada | Sin fallos ni errores. | Conforme: 21 pruebas, 0 fallos, 0 errores (8 servicio, 3 controlador y 10 integración HTTP con H2). |
| PT-12 | Ejecutar `mysql/tables.sql` y arrancar la API | Crear esquema y conectar el backend a MySQL 8.4. | Conforme: esquema cargado y backend iniciado con MySQL 8.4.13. |
| PT-13 | Iniciar sesión con administrador y operario | Aceptar credenciales válidas y devolver sus roles. | Conforme: `ADMINISTRADOR` y `OPERARIO`. |
| PT-14 | Consultar mensualidades sin sesión | Rechazar acceso. | Conforme: 401. |
| PT-15 | Consultar mensualidades como administrador | Permitir acceso autorizado. | Conforme: 200. |
| PT-16 | Consultar mensualidades como operario | Rechazar acceso por rol. | Conforme: 403. |
| PT-17 | Consultar vehículos como operario | Permitir la consulta operativa autenticada. | Conforme: 200. |
| PT-18 | Crear celda como operario con CSRF | Rechazar acceso por rol antes de modificar datos. | Conforme: 403. |
| PT-19 | Ejecutar escritura sin token CSRF / login con contraseña incorrecta | Rechazar las solicitudes con errores seguros. | Conforme: 403 / 401. |
| PT-20 | Cerrar sesión y comprobar contraseña persistida | Invalidar sesión y almacenar contraseña con BCrypt. | Conforme: logout 204, sesión posterior 401 y hash BCrypt confirmado en MySQL. |
| PT-21 | Registrar un período cuya membresía está vigente un solo día | Aceptar el período y conservar el valor mensual completo ingresado, sin prorrateo ni redondeo. | Conforme: prueba automatizada con H2 conserva el valor mensual completo de $125.000. |
| PT-22 | Crear, consultar, actualizar y dar de baja un usuario | ADMINISTRADOR puede crear (201), consultar/actualizar (200) y dar de baja lógica (204); conservar la fila con `activo=false`. | Conforme: integración HTTP con H2; la baja no elimina el registro. |
| PT-23 | Validar usuarios y permisos | Rechazar campos obligatorios faltantes (400), identificación duplicada (409), acceso anónimo (401) y acceso de OPERARIO a usuarios (403). | Conforme: integración HTTP con sesión y roles. |
| PT-24 | Registrar y consultar novedades | ADMINISTRADOR/OPERARIO crean (201) y consultan (200); asociar exactamente un vehículo o estancia y usar responsable/fecha del servidor. | Conforme: integración HTTP con H2 verifica ambos tipos de asociación y metadatos de servidor. |
| PT-25 | Rechazar novedades inválidas o escritura sin CSRF | Descripción vacía o dos asociaciones simultáneas: 400; POST sin token CSRF: 403. | Conforme: integración HTTP con H2. |

## Criterio de salida

Casos PT-01 a PT-25 aprobados. No se particionan las tablas: el volumen de la actividad no lo justifica. Las pruebas MySQL usaron un contenedor local desechable y credenciales sintéticas; las pruebas nuevas de usuarios/novedades usaron H2. Ninguna equivale a certificación de producción.

## Pendientes fuera del alcance probado

- Configurar `SESSION_COOKIE_SECURE=true` y HTTPS al desplegar.
- Ejecutar la suite contra el entorno de despliegue una vez disponible.
