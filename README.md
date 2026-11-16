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

## Pagos mensuales
- `GET /api/mensualidades/membresias` lista las membresías.
- `POST /api/mensualidades/membresias` inscribe un usuario existente (`usuarioId`, `fechaInicio`).
- `POST /api/mensualidades/pagos` registra un pago (`usuarioId`, `periodo` en `YYYY-MM`, `valor`, `metodo`).
- `GET /api/mensualidades/estado?periodo=YYYY-MM` consulta los estados pagado, pendiente o vencido.
- Regla acordada: si la membresía está vigente al menos un día del período, se cobra el valor mensual completo ingresado manualmente, sin prorrateo ni redondeo; los pagos históricos no se modifican.
- Repetir un pago con los mismos datos devuelve `200` y el registro existente; reutilizar usuario/período con valor o método distinto devuelve `409`.
- `GET /api/auth/csrf`, `POST /api/auth/login`, `GET /api/auth/session` y `POST /api/auth/logout` administran la sesión del personal. Las escrituras requieren el token CSRF enviado por el frontend.
- Las cuentas del personal se almacenan aparte de `usuarios`; el bootstrap inicial guarda contraseñas BCrypt. El administrador gestiona mensualidades; el operario no tiene permiso para esa API.

Pruebas automatizadas del backend: `cd backend` y `mvn test`. La suite (18 pruebas) incluye servicio/controlador y pruebas HTTP con H2 en memoria, además de la regla de cobro completo para una membresía vigente un solo día. El esquema y el backend también se validaron con MySQL 8.4 efímero, incluyendo login, roles, CSRF y cierre de sesión. Para probar manualmente contra el backend y MySQL locales, usar `docs/pruebas/mensualidades.http` en VS Code con una extensión cliente HTTP; primero inicia sesión como administrador y copia las cookies/token CSRF indicados en el archivo, luego reemplaza el ID y período por datos válidos. Los POST crean datos y no se deben ejecutar contra producción.

## Repositorio GitHub
https://github.com/sofia0823/parqueadero-aut-colom. El proyecto está integrado localmente en la rama `v1.0`, que sigue a `origin/v1.0`, conservando los archivos remotos no conflictivos. El push quedó pendiente: GitHub rechazó la escritura para la cuenta actualmente autenticada. Se requiere iniciar sesión con una cuenta con acceso de escritura o pedir al propietario que conceda ese permiso. No compartir ni guardar tokens o contraseñas en el repositorio.

## Entregables académicos
- Requerimientos e historias: `docs/requerimientos.md` y `docs/historias_usuario.md`.
- Casos de uso, secuencia y componentes: fuentes editables en `docs/diagramas/`.
- Mockup: `docs/mockups/pagos-mensuales.html`.
- Plan y evidencia de pruebas: `docs/pruebas/plan-pruebas-iteracion3.md` y `docs/pruebas/checklist-iteracion3.md`.
- Informe PDF: `docs/entrega_iteracion3.pdf` (cinco páginas; portada completada).
- Informe integral PDF: `docs/informe_trabajo_completo.pdf` (cinco páginas; incluye resultados y limitaciones).

La gestión de pagos mensuales y el flujo de entradas/salidas están implementados. La autenticación por sesión y RBAC protegen la gestión mensual y la creación de celdas; el CRUD de usuarios y RF-09 (novedades) siguen pendientes. Los diagramas son fuentes PlantUML editables; falta generar el proyecto nativo de StarUML si la entrega exige formato `.mdj`.
