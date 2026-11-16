# Parqueadero Autos Colombia — EA1
Proyecto monolítico académico con Spring Boot, React, Bootstrap y principios de Clean Architecture.

## Requisitos
- Java 21
- Maven 3.9+
- Node.js 20+ y npm
- MySQL 8 (o MariaDB compatible)

## Estructura
- `backend/`: API REST y aplicación Spring Boot.
- `frontend/`: interfaz React + Bootstrap.
- `docs/`: requisitos, historias, modelo de datos y fuentes de diagramas.
- `docs/entrega_iteracion3.pdf`: informe de cinco páginas de Iteración 3 con portada completada.
- `docs/informe_trabajo_completo.pdf`: informe integral de cinco páginas con alcance, reglas, resultados y pendientes.
- `docs/diagramas/`: diagramas editables en Mermaid y PlantUML.
- `docs/mockups/`: prototipo HTML estático de pagos mensuales.

## Ejecutar en desarrollo
1. Crear una base de datos vacía: `CREATE DATABASE autos_colombia CHARACTER SET utf8mb4;`
2. Configurar `DB_URL`, `DB_USER`, `DB_PASSWORD` y las cuentas iniciales del personal con variables de entorno. No guardar contraseñas en archivos versionados.
   - Obligatorio: `APP_SECURITY_BOOTSTRAP_ADMIN_USERNAME` y `APP_SECURITY_BOOTSTRAP_ADMIN_PASSWORD` (mínimo 12 caracteres).
   - Opcional: `APP_SECURITY_BOOTSTRAP_OPERATOR_USERNAME` y `APP_SECURITY_BOOTSTRAP_OPERATOR_PASSWORD` (deben configurarse juntos).
   - En despliegues HTTPS, configurar `SESSION_COOKIE_SECURE=true`; en desarrollo local HTTP se deja `false`.
3. En una terminal:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. En otra terminal:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
5. Abrir `http://localhost:5173`. La API está en `http://localhost:8080/api/vehiculos`.

## Construir frontend dentro del monolito
```bash
cd frontend
npm install
npm run build
```
Copia el contenido de `frontend/dist/` a `backend/src/main/resources/static/` y ejecuta:
```bash
cd ../backend
mvn clean package
java -jar target/autos-colombia-1.0.0.jar
```
El backend sirve el frontend compilado y la API desde el mismo despliegue. En desarrollo, Vite usa proxy hacia Spring Boot.

## Endpoints iniciales
- `GET /api/vehiculos` lista vehículos.
- `POST /api/vehiculos` registra un vehículo.
- `GET /api/health` verifica disponibilidad de la API.

## Usuarios y novedades
- `GET /api/usuarios` y `GET /api/usuarios/{id}` consultan usuarios (solo ADMINISTRADOR).
- `POST /api/usuarios` y `PUT /api/usuarios/{id}` crean/actualizan usuarios (solo ADMINISTRADOR).
- `DELETE /api/usuarios/{id}` realiza baja lógica (`activo=false`); no borra el registro ni su historial (solo ADMINISTRADOR).
- `GET /api/novedades` y `GET /api/novedades/{id}` consultan novedades.
- `POST /api/novedades` registra una novedad asociada a un vehículo o a una estancia.
- ADMINISTRADOR y OPERARIO pueden consultar/registrar novedades. El responsable y la fecha/hora se obtienen del usuario autenticado y del servidor.
- La autenticación sigue usando sesión de servidor, cookies y CSRF; no se migró a JWT. Las escrituras requieren el token CSRF del frontend.

## Pagos mensuales
- `GET /api/mensualidades/membresias` lista las membresías.
- `POST /api/mensualidades/membresias` inscribe un usuario existente (`usuarioId`, `fechaInicio`).
- `POST /api/mensualidades/pagos` registra un pago (`usuarioId`, `periodo` en `YYYY-MM`, `valor`, `metodo`).
- `GET /api/mensualidades/estado?periodo=YYYY-MM` consulta los estados pagado, pendiente o vencido.
- Regla acordada: si la membresía está vigente al menos un día del período, se cobra el valor mensual completo ingresado manualmente, sin prorrateo ni redondeo; los pagos históricos no se modifican.
- Repetir un pago con los mismos datos devuelve `200` y el registro existente; reutilizar usuario/período con valor o método distinto devuelve `409`.
- `GET /api/auth/csrf`, `POST /api/auth/login`, `GET /api/auth/session` y `POST /api/auth/logout` administran la sesión del personal. Las escrituras requieren el token CSRF enviado por el frontend.
- Las cuentas del personal se almacenan aparte de `usuarios`; el bootstrap inicial guarda contraseñas BCrypt. El administrador gestiona mensualidades; el operario no tiene permiso para esa API.

Pruebas automatizadas del backend: `cd backend` y `mvn test`. La suite (21 pruebas: 8 de servicio, 3 de controlador y 10 de integración HTTP con H2) incluye pagos, autenticación por sesión/CSRF, roles, usuarios, novedades y el cobro completo para una membresía vigente un solo día. El esquema y el backend de pagos/autenticación también se validaron previamente con MySQL 8.4 efímero. Para probar manualmente pagos contra backend y MySQL locales, usar `docs/pruebas/mensualidades.http` en VS Code con una extensión cliente HTTP; primero inicia sesión como administrador y copia las cookies/token CSRF indicados en el archivo. Los POST crean datos y no se deben ejecutar contra producción.

## Repositorio GitHub
https://github.com/sofia0823/parqueadero-aut-colom. El proyecto está publicado en la rama `v1.0`, que sigue a `origin/v1.0`. Se conservaron los archivos remotos no conflictivos y el historial; el README de la plantilla fue reemplazado por estas instrucciones del proyecto. Evitar `force push` y no compartir ni guardar tokens o contraseñas en el repositorio.

## Entregables académicos
- Requerimientos e historias: `docs/requerimientos.md` y `docs/historias_usuario.md`.
- Casos de uso, secuencia y componentes: fuentes editables en `docs/diagramas/`.
- Mockup: `docs/mockups/pagos-mensuales.html`.
- Plan y evidencia de pruebas: `docs/pruebas/plan-pruebas-iteracion3.md` y `docs/pruebas/checklist-iteracion3.md`.
- Informe PDF: `docs/entrega_iteracion3.pdf` (cinco páginas; portada completada).
- Informe integral PDF: `docs/informe_trabajo_completo.pdf` (cinco páginas; incluye resultados y limitaciones).

La gestión de pagos mensuales, entradas/salidas, usuarios y novedades está implementada. RBAC limita usuarios/mensualidades al administrador y permite al operario registrar/consultar novedades y realizar operaciones. La matriz granular de los demás endpoints aún no está completa. Los diagramas son fuentes PlantUML editables; falta generar el proyecto nativo de StarUML si la entrega exige formato `.mdj`.
