# Historias de usuario

**HU-01 — Registrar vehículo**  
Como operario, quiero registrar la placa y los datos del propietario para identificar el vehículo en futuras visitas.  
**Criterios:** placa y propietario obligatorios; placa normalizada en mayúsculas; placa duplicada rechazada; confirmación visible al guardar.

**HU-02 — Registrar entrada**  
Como operario, quiero registrar el ingreso de un vehículo para controlar su permanencia.  
**Criterios:** seleccionar vehículo existente; registrar fecha/hora del servidor; no admitir otra estancia abierta; asignar celda disponible; mostrar confirmación y celda asignada.

**HU-03 — Registrar salida**  
Como operario, quiero registrar la salida de un vehículo para cerrar su estancia y liberar la celda.  
**Criterios:** buscar por placa; mostrar entrada activa; registrar hora de salida; salida posterior a entrada; liberar celda; impedir doble cierre.

**HU-04 — Consultar historial**  
Como administrador, quiero consultar los movimientos por placa y fechas para auditar la operación.  
**Criterios:** filtro por placa/rango; mostrar entradas, salidas y celda; informar cuando no haya resultados.

**HU-05 — Gestionar celdas**  
Como administrador, quiero crear y mantener celdas para conocer su disponibilidad.  
**Criterios:** código único; estado disponible/ocupada/mantenimiento; no asignar celdas ocupadas o en mantenimiento.

**HU-06 — Registrar novedad**  
Como operario, quiero registrar una novedad asociada a un vehículo para dejar evidencia de incidentes u observaciones.  
**Criterios:** vehículo o estancia asociada; descripción obligatoria; fecha/hora y responsable; consulta posterior.

**HU-07 — Registrar pago mensual**  
Como administrador, quiero registrar el pago de mensualidad para mantener control de cartera.  
**Criterios:** seleccionar un usuario inscrito como mensualista; período obligatorio en formato `YYYY-MM`; aceptar períodos actuales o anteriores, no futuros; valor positivo ingresado por el administrador; cobrar el valor mensual completo si la membresía cubre al menos un día del período, sin prorrateo ni redondeo; método efectivo/tarjeta/transferencia; fecha y hora tomadas del servidor; una repetición idéntica devuelve el pago existente sin duplicarlo; cambios de valor o método para usuario/período existentes responden conflicto sin sobrescribir.

**HU-08 — Consultar panel**  
Como administrador, quiero ver un resumen de vehículos y ocupación para conocer el estado del parqueadero.  
**Criterios:** total de vehículos; celdas disponibles/ocupadas; estancias abiertas; movimientos recientes.

**HU-09 — Gestionar usuarios**  
Como administrador, quiero registrar y actualizar los datos de usuarios para mantener la información de contacto al día.  
**Criterios:** datos obligatorios validados; identificación única; datos actualizados reflejados en consultas.

**HU-10 — Gestionar mensualistas**  
Como administrador, quiero inscribir y finalizar la membresía mensual de un usuario para controlar quién genera períodos por pagar.  
**Criterios:** usuario existente y activo; fecha de inicio obligatoria y no futura; no permitir membresías vigentes duplicadas; guardar fecha de finalización al cerrar la membresía.

**HU-11 — Consultar estado de mensualidad**  
Como administrador, quiero consultar el estado de cada mensualista por período para identificar pagos pendientes y vencidos.  
**Criterios:** filtrar por período `YYYY-MM`; mostrar pagado, pendiente o vencido; un período anterior al mes actual sin pago se muestra vencido; períodos futuros no consultables.

## Historias de la Iteración 3 — Gestión de pagos

**HU-07 — Registrar pago mensual (prioridad alta)**  
**Precondiciones:** el usuario existe y su membresía cubre el período solicitado.  
**Flujo principal:** el administrador selecciona usuario, período, valor y método; el sistema valida la solicitud, registra la fecha/hora del servidor y confirma el resultado.  
**Criterios de aceptación:** período `YYYY-MM` actual o pasado; período futuro rechazado; valor mayor que cero; si la membresía cubre al menos un día del mes, cobrar el valor mensual completo ingresado manualmente, sin prorrateo ni redondeo; método efectivo/tarjeta/transferencia; fecha oficial del servidor; pago nuevo devuelve 201; repetición exacta devuelve 200 y el mismo ID; cambio de valor o método para usuario/período existentes devuelve 409 sin crear otra fila.

**HU-10 — Gestionar membresía mensual (prioridad alta)**  
**Precondiciones:** el usuario existe y está activo.  
**Criterios de aceptación:** registrar fecha de inicio no futura; rechazar membresía vigente duplicada; la repetición idéntica devuelve la membresía existente; finalizar una membresía guarda la fecha de fin y conserva el historial.

**HU-11 — Consultar estado y pagos (prioridad media)**  
**Criterios de aceptación:** filtrar por período; identificar pagado, pendiente y vencido según vigencia y registro; no permitir consultar períodos futuros; mostrar el pago registrado sin exponer datos de otros períodos.

## Priorización de Iteración 1
Prioridad alta: HU-01, HU-02, HU-03 y HU-04.  
Prioridad media/documentación de evolución: HU-05, HU-06, HU-07, HU-08 y HU-09.

Prioridad de Iteración 3: HU-07 alta; HU-10 alta; HU-11 media. Las historias HU-07, HU-10 y HU-11 no sustituyen las historias de entrada/salida ni el pago por estancia.
