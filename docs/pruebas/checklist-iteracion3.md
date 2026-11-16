# Checklist de cierre — Iteración 3

Avance actualizado: 2026-11-15.

## 1. Validación con MySQL y revisión del esquema

- [x] Ejecutar `mysql/tables.sql` en MySQL 8.4 desechable (contenedor efímero).
- [x] Verificar claves foráneas y restricciones `CHECK`; ambas rechazaron inserciones inválidas.
- [x] Revisar índices y `EXPLAIN`: pagos usan `uq_pago_mensual_usuario_periodo`; membresías usan índice por usuario/fecha.
- [x] Ejecutar backend Spring Boot contra MySQL y probar endpoints HTTP reales.
- [x] Probar idempotencia secuencial y concurrente contra MySQL.
- [x] Ejecutar pruebas automatizadas backend/H2: 18 pruebas, 0 fallos, 0 errores.
- [x] Probar validaciones HTTP: alta 201/repetida 200; pago 201/repetido 200 con el mismo ID; conflicto 409; período futuro y valor cero 400.
- [x] Enviar 20 solicitudes concurrentes del mismo pago: una respuesta 201, 19 respuestas 200 y una sola fila en MySQL.
- [x] Revisar índices: membresías por usuario/vigencia; pagos mensuales por usuario/período (único), período y fecha.
- [x] No particionar: el volumen es académico y reducido; la partición añade complejidad sin beneficio medido y restringe el uso de claves foráneas en MySQL.

## 2. Autenticación y autorización RBAC

- [x] Separar cuentas del personal de los usuarios/clientes del parqueadero.
- [x] Implementar inicio/cierre de sesión, sesiones de servidor, contraseñas BCrypt y protección CSRF.
- [x] Aplicar autorización en el backend: administrador gestiona mensualidades y crea celdas; operario no accede a esos módulos.
- [x] Probar acceso anónimo, rol permitido/denegado, CSRF, login fallido y cierre de sesión contra MySQL 8.4.
- [x] Confirmar que las rutas `/api/**` requieren sesión excepto los endpoints públicos de salud/CSRF/login.

## 3. Entregables académicos

- [x] Completar RF/RNF e historias de usuario de pagos mensuales.
- [x] Crear fuentes editables de casos de uso, secuencia y componentes de las tres iteraciones (`docs/diagramas/*.puml`).
- [x] Confirmar con la estudiante que las fuentes PlantUML editables son suficientes; no se requiere proyecto nativo `.mdj`.
- [x] Crear mockup responsive de la gestión mensual en `docs/mockups/pagos-mensuales.html` (datos ficticios).
- [x] Añadir pruebas automatizadas de servicio y de integración HTTP con H2 (18 pruebas pasan).
- [x] Completar el plan resumido y registrar resultados en `docs/pruebas/plan-pruebas-iteracion3.md`.
- [x] Incorporar estudiante e institución y actualizar el PDF de cinco páginas en `docs/entrega_iteracion3.pdf`.
- [x] Completar los campos de portada con Sofia Gonzales Herrera, IU Digital y Esteban Montoya.

## 4. Publicación en GitHub

- [x] Verificar el repositorio proporcionado: es público, rama `v1.0`, pero contiene una plantilla ReadMe.com y algunos archivos de modelo/datos, no el código fuente del parqueadero.
- [x] Inicializar Git en la carpeta local, configurar `origin` y descargar la referencia `origin/v1.0`.
- [ ] Integrar la rama remota conservando sus archivos y publicar el código; aún no se creó un commit ni se hizo push.

## 5. Reglas de período y vencimiento

- [x] Período en formato `YYYY-MM`; aceptar mes actual o anterior y rechazar meses futuros.
- [x] Marcar como vencido al consultar desde el primer día del mes siguiente, sin proceso periódico.
- [x] Fecha y hora del pago tomadas del servidor.
- [x] Confirmar regla para membresías parciales: si hay al menos un día de vigencia en el mes, se cobra el valor mensual completo ingresado manualmente, sin prorrateo ni redondeo.

## Riesgos y límites conocidos

- No hay MySQL nativo disponible; el esquema y el backend, incluyendo autenticación/RBAC, se validaron con MySQL 8.4 en contenedores efímeros.
- RBAC protege mensualidades y creación de celdas; otras rutas autenticadas no tienen una matriz granular por rol implementada.
- En despliegue, exigir HTTPS y `SESSION_COOKIE_SECURE=true`; no se validó una instalación de producción.
- El repositorio proporcionado aún contiene una plantilla de documentación y no el proyecto. La carpeta local ya está conectada, pero se debe reconciliar el contenido remoto antes de publicar; no se hizo push.
- RF-09 (novedades) aparece en requerimientos/diseño, pero no existe en el backend ni en el esquema desplegable; `docs/modelo_datos.sql` ahora lo deja explícito.
