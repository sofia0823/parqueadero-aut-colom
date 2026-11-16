# Requerimientos — Autos Colombia

## Requerimientos funcionales
| ID | Requerimiento | Criterio de aceptación |
|---|---|---|
| RF-01 | Registrar vehículo y propietario | El sistema solicita placa y propietario; guarda el registro si los datos son válidos. |
| RF-02 | Consultar vehículos | El administrador visualiza los vehículos registrados con placa, propietario, tipo y contacto. |
| RF-03 | Registrar entrada | El operario identifica el vehículo, registra fecha/hora de entrada y asigna una celda libre. |
| RF-04 | Registrar salida | El operario registra fecha/hora de salida y libera la celda asociada. |
| RF-05 | Evitar entradas duplicadas | Un vehículo no puede tener dos estancias abiertas simultáneas. |
| RF-06 | Gestionar usuarios | Crear, consultar y actualizar usuarios/propietarios con datos de contacto. |
| RF-07 | Gestionar celdas | Crear y consultar celdas; estados: disponible, ocupada, mantenimiento. |
| RF-08 | Asignar celda disponible | Una entrada solo puede confirmarse si existe una celda disponible compatible. |
| RF-09 | Registrar novedades | Asociar novedad, descripción, fecha/hora y responsable a una estancia o vehículo. |
| RF-10 | Registrar pagos mensuales | Registrar en un flujo separado el usuario, período `YYYY-MM`, valor positivo ingresado manualmente, fecha del servidor, método y estado. Si la membresía cubre al menos un día del período, cobrar el valor mensual completo sin prorrateo ni redondeo. Una repetición idéntica no duplica el pago; si cambian valor o método para usuario/período, rechazarla como conflicto. El cobro por estancia conserva su flujo actual. |
| RF-11 | Consultar historial | Consultar entradas/salidas por placa y rango de fechas. |
| RF-12 | Validar integridad | Rechazar placas repetidas, campos obligatorios vacíos y operaciones incompatibles con el estado actual. |
| RF-13 | Control de acceso | Autenticar al personal y aplicar permisos por rol en el backend. El administrador gestiona mensualidades y crea celdas; ambos roles acceden solo a las operaciones autorizadas. |
| RF-14 | Gestionar mensualistas | Inscribir y finalizar la membresía de un usuario, conservando sus fechas de vigencia. |
| RF-15 | Consultar estado de mensualidad | Mostrar pagos, pendientes y vencidos por período; un período anterior al mes actual se considera vencido si no tiene pago registrado. |

## Requerimientos no funcionales
| ID | Requerimiento | Verificación propuesta |
|---|---|---|
| RNF-01 | Usabilidad | Formularios en español, etiquetas claras, validación y mensajes de éxito/error. |
| RNF-02 | Rendimiento | Las consultas comunes deben responder en menos de 2 s con hasta 5.000 vehículos en ambiente de prueba. |
| RNF-03 | Seguridad | Validar entradas, usar consultas parametrizadas, guardar contraseñas con BCrypt y secretos mediante variables de entorno; proteger escrituras con CSRF y desplegar con HTTPS. |
| RNF-04 | Integridad | Claves primarias, relaciones y restricciones de unicidad; transacciones para entrada/salida. |
| RNF-05 | Mantenibilidad | Separación en dominio, aplicación e infraestructura; nombres claros y responsabilidades únicas. |
| RNF-06 | Disponibilidad | Manejo de errores y endpoint de salud; respaldo periódico de la base de datos. |
| RNF-07 | Compatibilidad | Interfaz adaptable a escritorio, tableta y móvil; navegadores modernos. |
| RNF-08 | Trazabilidad | Registrar fechas de entrada/salida, pagos y novedades con sus responsables. |
| RNF-09 | Portabilidad | Ejecutar con Java 21, Maven, Node.js y MySQL 8, documentando variables de configuración. |
| RNF-10 | Privacidad | Usar datos personales solo para operar el servicio y limitar su acceso a personal autorizado. |
| RNF-11 | Trazabilidad de mensualidades | Registrar la fecha y hora del pago desde el servidor; no confiar en la hora enviada por el navegador. |

## Matriz de acceso propuesta (RBAC)

| Rol | Permisos previstos |
|---|---|
| Administrador | Inscribir/finalizar mensualistas; registrar y consultar pagos mensuales; gestionar usuarios y celdas; consultar reportes. |
| Operario | Registrar entradas y salidas; consultar información necesaria para la operación diaria. Sin acceso a la gestión de mensualidades. |

La autenticación por sesión y la autorización del backend están implementadas para la API de mensualidades (solo administrador) y la creación de celdas (solo administrador); las demás rutas `/api/**` requieren sesión. La lectura operativa está permitida al operario. El CRUD de usuarios y reportes no está implementado, por lo que la matriz no afirma permisos para esas funciones. La interfaz también oculta rutas por rol, pero la protección efectiva reside en el backend.

## Reglas de negocio
- La placa se normaliza a mayúsculas y no se puede duplicar.
- Una celda ocupada o en mantenimiento no puede asignarse.
- No se permite una segunda estancia abierta para el mismo vehículo.
- La salida debe ser posterior a la entrada.
- Una salida confirmada libera la celda en la misma transacción.
- Un pago mensual debe asociarse a un usuario/periodo y tener valor mayor que cero.
- Si la membresía está vigente al menos un día del mes, el período genera un cobro mensual completo. El valor se ingresa manualmente; no se prorratea ni redondea por meses parciales.
- Cada pago mensual debe corresponder a una membresía que cubra el período; solo se permite un pago por usuario y período. Repetir la misma solicitud devuelve el pago existente; si cambian valor o método para la misma clave usuario/período, responder conflicto sin sobrescribir.
- Se permiten pagos de períodos anteriores, pero no de períodos futuros. El vencimiento se calcula al consultar, sin reloj visual ni proceso periódico.
- Las novedades deben indicar fecha, descripción y vehículo/estancia relacionada.

## Alcance verificable de la Iteración 3

| ID | Requerimiento no funcional específico | Criterio de aceptación / verificación |
|---|---|---|
| RNF-12 | Idempotencia del pago mensual | Repetir concurrentemente la misma solicitud usuario/período/valor/método produce un único registro; el primer resultado es 201 y los repetidos devuelven 200 con el mismo identificador. |
| RNF-13 | Integridad transaccional | MySQL aplica FK al usuario, unicidad usuario/período y `CHECK (valor > 0)`; probar violaciones en la base real. |
| RNF-14 | Respuesta de errores | Entrada no válida devuelve 400, una solicitud diferente para una clave ya registrada devuelve 409 y no altera el pago existente. |
| RNF-15 | Rendimiento de consulta | Las búsquedas de pago por usuario/período deben utilizar el índice único compuesto; verificar con `EXPLAIN` sobre MySQL. |
| RNF-16 | Autenticación y autorización | El backend valida sesiones, rol y CSRF: anónimo 401, rol permitido 200, rol denegado 403 y escritura sin token CSRF 403. Verificar login/logout y almacenamiento BCrypt. |

RNF-16 se verificó con el backend Spring Boot conectado a MySQL 8.4 y cuentas de prueba desechables. No se validaron credenciales de despliegue ni se publicó el sistema en producción.
