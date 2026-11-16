# Instrucciones de Copilot — Autos Colombia

## Objetivo del proyecto

Sistema académico de gestión del parqueadero Autos Colombia. La Iteración 3 incorpora pagos de mensualidad, manteniendo el flujo actual de cobro por estancia.

## Reglas obligatorias para cambios

- Antes de editar, revisar el código y los documentos que afectan el cambio.
- Hacer cambios pequeños, compatibles y probados; no reescribir módulos ajenos a la tarea.
- No cambiar ni eliminar el cobro por estancia: actualmente liquida a $2.000 COP por hora iniciada, acepta efectivo/tarjeta/transferencia y confirma el pago junto con la salida y liberación de la celda.
- No reutilizar ni transformar silenciosamente `pagos` para representar mensualidades. El flujo mensual debe ser independiente del pago por estancia.
- Mantener importes positivos, validaciones explícitas, integridad referencial y transacciones donde corresponda.
- La fecha y hora oficiales se obtienen del servidor. No agregar reloj visual ni proceso automático periódico: el usuario eligió validar con hora del servidor.
- No inferir que todo usuario activo es mensualista. La pertenencia al servicio mensual debe modelarse explícitamente.
- No afirmar que existen pruebas, diagramas StarUML, mockups, PDF o repositorio Git si no se han creado y verificado.
- Responder en español y explicar cualquier decisión aún no confirmada.

## Estado conocido

- Tecnologías: Java 21, Spring Boot, JPA, MySQL, React, Vite y Bootstrap.
- El backend ya liquida estancias y registra el pago antes de la salida.
- El CRUD de usuarios está disponible para ADMINISTRADOR, incluida baja lógica (`activo=false`); la baja no elimina registros relacionados con pagos, membresías o vehículos.
- RF-09 está implementado como novedades asociadas a exactamente un vehículo o estancia. El backend usa la sesión autenticada para el responsable y el reloj del servidor para la fecha/hora.
- Se implementó autenticación por sesión, BCrypt y CSRF; el backend exige sesión en `/api/**`, restringe mensualidades y creación de celdas a ADMINISTRADOR, y permite lectura operativa al OPERARIO.
- El directorio `docs/diagramas/` contiene fuentes PlantUML/Mermaid, no un proyecto nativo de StarUML (`.mdj`).
- El proyecto está publicado en `sofia0823/parqueadero-aut-colom`, rama `v1.0`, y la rama local sigue a `origin/v1.0`. Se conservaron los archivos remotos no conflictivos y el historial; evitar `force push`. Nunca compartir ni versionar tokens.

## Decisiones acordadas para pagos mensuales

- Crear una tabla separada para pagos mensuales; conservar el esquema y comportamiento de pagos por estancia.
- El administrador ingresará el valor en cada registro; exigir valor mayor que cero.
- Cada pago mensual debe referenciar a un usuario y un período en formato `YYYY-MM`.
- Se permiten pagos de períodos anteriores.
- Un período se considera vencido desde el día 1 del mes siguiente.
- Si una membresía está vigente al menos un día del período, se cobra el valor mensual completo ingresado manualmente, sin prorrateo ni redondeo. La regla aplica a pagos nuevos; conservar pagos históricos.
- Registrar fecha y hora del pago en el servidor; no aceptar la fecha oficial desde el navegador.
- No crear reloj en pantalla ni tarea programada. El vencimiento puede calcularse al consultar, una vez definidas las fechas de inscripción del mensualista.

## Modelo de mensualistas y roles

Separar dos conceptos:

1. **Mensualista (regla de negocio):** inscripción explícita de un usuario al servicio, con estado y fechas de inicio/fin. Se recomienda una entidad/tabla `membresias_mensuales` relacionada con `usuarios`; no basta el indicador general `usuarios.activo`.
2. **Rol de acceso (seguridad):** permisos del personal para operar el sistema mediante control de acceso basado en roles (**RBAC**).

La implementación crea `membresias_mensuales` y `pagos_mensuales`. La segunda contiene `id`, `usuario_id` (FK), `periodo` (`YYYY-MM`), `valor`, `fecha_pago` del servidor, `metodo`, `estado` y `referencia` opcional. Hay unicidad por usuario/período; no se debe permitir anular y volver a cobrar sin definir cómo se conserva la auditoría.

La deuda/vencimiento no debe inventarse como pago registrado. Se deriva de las membresías, sus fechas y la ausencia de un pago confirmado para el período. Un período se incluye cuando se cruza con la vigencia de una membresía; si hay al menos un día de vigencia, se cobra el valor mensual completo ingresado manualmente, sin prorrateo ni redondeo. Esta decisión aplica a pagos nuevos; no modificar pagos históricos. Confirmar reglas de suspensión y reactivación antes de ampliar el flujo.

## Acceso implementado y límites

- **Administrador:** gestionar mensualidades y crear celdas; se verifica autorización en el backend.
- **Operario:** acceder a las operaciones autenticadas permitidas, incluida lectura operativa; no puede usar la API de mensualidades ni crear celdas.
- `/api/**` requiere autenticación salvo salud, CSRF y login. Las escrituras requieren token CSRF. La interfaz React también filtra pantallas, pero no sustituye la autorización del servidor.
- La matriz no cubre CRUD de usuarios/reportes aún no implementados ni define permisos granulares para todas las rutas existentes; no afirmar cobertura más amplia.
- Las cuentas iniciales se configuran mediante `APP_SECURITY_BOOTSTRAP_ADMIN_USERNAME` y `APP_SECURITY_BOOTSTRAP_ADMIN_PASSWORD` (obligatorias), con variables opcionales de operario. Contraseñas de al menos 12 caracteres; no versionar credenciales.

## Pendientes e inconsistencias verificadas

- RF-06 y RF-09 están implementados. Usuarios/mensualidades son de ADMINISTRADOR; ADMINISTRADOR y OPERARIO pueden registrar y consultar novedades.
- RF-13 tiene control por sesión/RBAC para las rutas documentadas; la matriz granular de todas las demás rutas aún debe revisarse antes de producción.
- Los diagramas de las iteraciones están disponibles como fuentes PlantUML/Mermaid; la estudiante confirmó que no requiere el proyecto nativo StarUML `.mdj`.
- El PDF actual tiene cinco páginas y su portada está completada con los datos proporcionados.
- La URL sin punto final devuelve 404 y la variante con punto final solo contiene un README; confirmar con la estudiante antes de publicar.

## Implementación mensual y validaciones

- Se agregaron los modelos `Usuario`, `MembresiaMensual` y `PagoMensual`, repositorios, servicio y endpoints bajo `/api/mensualidades`.
- Se agregaron inscripción/finalización de membresías, registro de pago mensual y consulta derivada de estado por período.
- La interfaz está en `/pagos-mensuales`; la inscripción usa el ID de un usuario que ya exista.
- `mysql/tables.sql` y `docs/modelo_datos.sql` ahora separan pagos por estancia de pagos mensuales.
- `docs/modelo_datos.sql` se ejecutó en MySQL 8.4 y se alineó con el esquema desplegable; RF-09 (novedades) se mantiene como pendiente, no como tabla instalada.
- Hay pruebas unitarias del servicio y de controlador mediante MockMvc, además de `docs/pruebas/mensualidades.http` para solicitudes manuales.
- Se generaron fuentes PlantUML para casos de uso, secuencia y componentes de las tres iteraciones, un mockup HTML y un plan de pruebas resumido; la estudiante confirmó que las fuentes son suficientes.
- Se generó `docs/entrega_iteracion3.pdf` (5 páginas) con resultados de RBAC actualizados y portada completada.
- Se instalaron JDK 21, Maven 3.10.1 y Node.js LTS/npm en el equipo; Maven quedó en `%LOCALAPPDATA%\Programs\Apache\Maven-3.10.1`.
- Validación ejecutada: `mvn test` pasó (21 pruebas, 0 fallos ni errores), incluyendo usuarios, novedades, sesión/CSRF, roles y pagos con H2 en memoria; `npm run build` pasó. El esquema y la autorización HTTP de pagos/autenticación también se probaron previamente con Spring Boot + MySQL 8.4 efímero.
- `docs/pruebas/mensualidades.http` permite probar manualmente contra una instancia MySQL, con backend iniciado y usuario/membresía de prueba.
- `mysql/tables.sql` se ejecutó en MySQL 8.4 efímero. Se validaron claves foráneas, `CHECK`, índices y planes `EXPLAIN`; no se justifica particionar el volumen académico.
- Backend y endpoints se probaron contra MySQL: pago repetido devuelve el mismo ID; 20 solicitudes concurrentes produjeron una fila, con una respuesta 201 y 19 respuestas 200. Las solicitudes conflictivas recibieron 409 y las inválidas 400.
- URL proporcionada para GitHub: https://github.com/sofia0823/parqueadero-aut-colom. Repositorio público, rama `v1.0`; el proyecto está publicado y la copia local está sincronizada con el remoto.

## Plan de trabajo

1. Definir la matriz RBAC básica (administrador/operario) está completado; también quedó confirmada la regla económica de cobro mensual completo para cualquier período con al menos un día de vigencia. Siguen pendientes las reglas de suspensión y reactivación.
2. Revisar el esquema MySQL/JPA de membresías y pagos mensuales, preservando la tabla y endpoints de cobro por estancia. **Completado y validado en MySQL 8.4.**
3. Implementar autenticación y autorización RBAC en backend según la matriz acordada; no confundir mensualista con rol. **Completado y probado con MySQL 8.4.**
4. Probar nuevas operaciones de usuarios/novedades, permisos, sesión/CSRF y no regresión de pagos; mantener pruebas de fechas de vigencia, idempotencia y autorización.
5. La portada está completa y la estudiante confirmó que PlantUML es suficiente. Organizar el repositorio público y conectar la carpeta de trabajo antes de publicar.

## Comandos de validación

- Backend: `cd backend` y `mvn test`.
- Frontend: `cd frontend` y `npm run build`.
- Ejecutar validaciones dirigidas al cambio y no instalar dependencias salvo que falten.
