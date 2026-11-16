package co.autoscolombia.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:autoscolombia;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "app.security.bootstrap-admin.username=test-admin",
                "app.security.bootstrap-admin.password=TestAdminPassword123!",
                "app.security.bootstrap-operator.username=test-operator",
                "app.security.bootstrap-operator.password=TestOperator123!"
        }
)
class MensualidadesHttpIntegrationTest {

    private static final long USUARIO_ID = 980001L;
    private static final long CELDA_ID = 970001L;
    private static final long VEHICULO_ID = 970002L;
    private static final long ESTANCIA_ID = 970003L;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpHeaders adminHeaders;
    private HttpHeaders operatorHeaders;

    @BeforeEach
    void crearUsuarioMensualistaDePrueba() throws Exception {
        limpiarDatos();
        jdbc.update(
                """
                INSERT INTO usuarios
                    (id, tipo_documento, numero_documento, nombres, activo, creado_en)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                USUARIO_ID,
                "CC",
                "PRUEBA-980001",
                "Usuario de integración",
                true
        );
        jdbc.update(
                """
                INSERT INTO celdas (id, codigo, tipo, estado)
                VALUES (?, ?, ?, ?)
                """,
                CELDA_ID,
                "TEST-NOVEDAD",
                "CARRO",
                "DISPONIBLE"
        );
        jdbc.update(
                """
                INSERT INTO vehiculos
                    (id, placa, propietario, creado_en)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                VEHICULO_ID,
                "NOV-001",
                "Usuario de integración"
        );
        jdbc.update(
                """
                INSERT INTO estancias (id, vehiculo_id, celda_id, entrada)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                ESTANCIA_ID,
                VEHICULO_ID,
                CELDA_ID
        );
        adminHeaders = iniciarSesion("test-admin", "TestAdminPassword123!");
        operatorHeaders = iniciarSesion(
                "test-operator",
                "TestOperator123!"
        );
    }

    @AfterEach
    void limpiarDatosDePrueba() {
        limpiarDatos();
    }

    @Test
    void aceptaHttpInscripcionYPagoYConsultaEstadoPersistido() throws Exception {
        String periodo = YearMonth.now().toString();
        ResponseEntity<String> membresia = post(
                "/api/mensualidades/membresias",
                Map.of(
                        "usuarioId", USUARIO_ID,
                        "fechaInicio", "2020-01-01"
                )
        );

        assertEquals(201, membresia.getStatusCode().value());
        JsonNode membresiaJson = objectMapper.readTree(membresia.getBody());
        assertEquals(
                USUARIO_ID,
                membresiaJson.path("usuarioId").asLong()
        );

        ResponseEntity<String> membresiaRepetida = post(
                "/api/mensualidades/membresias",
                Map.of(
                        "usuarioId", USUARIO_ID,
                        "fechaInicio", "2020-01-01"
                )
        );
        assertEquals(200, membresiaRepetida.getStatusCode().value());
        assertEquals(
                membresiaJson.path("id").asLong(),
                objectMapper.readTree(membresiaRepetida.getBody())
                        .path("id")
                        .asLong()
        );
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM membresias_mensuales WHERE usuario_id = ?",
                        Integer.class,
                        USUARIO_ID
                )
        );

        ResponseEntity<String> pago = post(
                "/api/mensualidades/pagos",
                Map.of(
                        "usuarioId", USUARIO_ID,
                        "periodo", periodo,
                        "valor", 125000,
                        "metodo", "EFECTIVO"
                )
        );

        assertEquals(201, pago.getStatusCode().value());
        JsonNode pagoJson = objectMapper.readTree(pago.getBody());
        assertEquals(USUARIO_ID, pagoJson.path("usuarioId").asLong());
        assertEquals(periodo, pagoJson.path("periodo").asText());
        assertEquals("PAGADO", pagoJson.path("estado").asText());

        ResponseEntity<String> pagoRepetido = post(
                "/api/mensualidades/pagos",
                Map.of(
                        "usuarioId", USUARIO_ID,
                        "periodo", periodo,
                        "valor", 125000,
                        "metodo", "EFECTIVO"
                )
        );
        assertEquals(200, pagoRepetido.getStatusCode().value());
        assertEquals(
                pagoJson.path("id").asLong(),
                objectMapper.readTree(pagoRepetido.getBody())
                        .path("id")
                        .asLong()
        );
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM pagos_mensuales WHERE usuario_id = ? AND periodo = ?",
                        Integer.class,
                        USUARIO_ID,
                        periodo
                )
        );

        ResponseEntity<String> estado = http.exchange(
                url("/api/mensualidades/estado?periodo=" + periodo),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class
        );
        JsonNode estadoJson = objectMapper.readTree(estado.getBody());
        assertEquals(200, estado.getStatusCode().value());
        assertEquals(USUARIO_ID, estadoJson.get(0).path("usuarioId").asLong());
        assertEquals("PAGADO", estadoJson.get(0).path("estado").asText());

        ResponseEntity<String> conflicto = post(
                "/api/mensualidades/pagos",
                Map.of(
                        "usuarioId", USUARIO_ID,
                        "periodo", periodo,
                        "valor", 130000,
                        "metodo", "EFECTIVO"
                )
        );
        assertEquals(409, conflicto.getStatusCode().value());
        assertTrue(objectMapper.readTree(conflicto.getBody())
                .path("mensaje")
                .asText()
                .contains("Ya existe un pago distinto"));
    }

    @Test
    void devuelve400ParaUnPeriodoFuturo() throws Exception {
        String periodoFuturo = YearMonth.now().plusMonths(1).toString();

        ResponseEntity<String> respuesta = post(
                "/api/mensualidades/pagos",
                Map.of(
                        "usuarioId", USUARIO_ID,
                        "periodo", periodoFuturo,
                        "valor", 125000,
                        "metodo", "EFECTIVO"
                )
        );

        assertEquals(400, respuesta.getStatusCode().value());
        assertTrue(objectMapper.readTree(respuesta.getBody())
                .path("mensaje")
                .asText()
                .contains("períodos futuros"));
    }

    @Test
    void exigeAutenticacionYRestringeMensualidadesAlAdministrador()
            throws Exception {
        ResponseEntity<String> anonimo = http.getForEntity(
                url("/api/mensualidades/membresias"),
                String.class
        );
        ResponseEntity<String> administrador = http.exchange(
                url("/api/mensualidades/membresias"),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class
        );
        ResponseEntity<String> operario = http.exchange(
                url("/api/mensualidades/membresias"),
                HttpMethod.GET,
                new HttpEntity<>(operatorHeaders),
                String.class
        );

        assertEquals(401, anonimo.getStatusCode().value());
        assertEquals(200, administrador.getStatusCode().value());
        assertEquals(403, operario.getStatusCode().value());
    }

    @Test
    void gestionaUsuariosComoAdministradorSinBorradoFisico() throws Exception {
        ResponseEntity<String> anonimo = http.getForEntity(
                url("/api/usuarios"),
                String.class
        );
        ResponseEntity<String> operario = http.exchange(
                url("/api/usuarios"),
                HttpMethod.GET,
                new HttpEntity<>(operatorHeaders),
                String.class
        );
        ResponseEntity<String> creado = postCon(
                adminHeaders,
                "/api/usuarios",
                Map.of(
                        "tipoDocumento", "CC",
                        "numeroDocumento", "NUEVO-100",
                        "nombres", "Ada Lovelace"
                )
        );
        ResponseEntity<String> incompleto = postCon(
                adminHeaders,
                "/api/usuarios",
                Map.of(
                        "tipoDocumento", "CC",
                        "numeroDocumento", "SIN-NOMBRES"
                )
        );

        assertEquals(401, anonimo.getStatusCode().value());
        assertEquals(403, operario.getStatusCode().value());
        assertEquals(201, creado.getStatusCode().value());
        assertEquals(400, incompleto.getStatusCode().value());
        JsonNode creadoJson = objectMapper.readTree(creado.getBody());
        long id = creadoJson.path("id").asLong();
        assertTrue(creadoJson.path("activo").asBoolean());
        assertTrue(creadoJson.path("telefono").isNull());

        ResponseEntity<String> listaUsuarios = http.exchange(
                url("/api/usuarios"),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class
        );
        assertEquals(200, listaUsuarios.getStatusCode().value());
        assertTrue(objectMapper.readTree(listaUsuarios.getBody()).findValuesAsText("id")
                .contains(Long.toString(id)));

        ResponseEntity<String> duplicado = postCon(
                adminHeaders,
                "/api/usuarios",
                Map.of(
                        "tipoDocumento", "CC",
                        "numeroDocumento", "NUEVO-100",
                        "nombres", "Otra persona"
                )
        );
        assertEquals(409, duplicado.getStatusCode().value());

        ResponseEntity<String> actualizado = putCon(
                adminHeaders,
                "/api/usuarios/" + id,
                Map.of(
                        "tipoDocumento", "CE",
                        "numeroDocumento", "NUEVO-100",
                        "nombres", "Ada Byron",
                        "telefono", "3001234567",
                        "correo", "ada@example.com"
                )
        );
        assertEquals(200, actualizado.getStatusCode().value());
        assertEquals(
                "Ada Byron",
                objectMapper.readTree(actualizado.getBody())
                        .path("nombres")
                        .asText()
        );

        ResponseEntity<String> desactivado = http.exchange(
                url("/api/usuarios/" + id),
                HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders),
                String.class
        );
        assertEquals(204, desactivado.getStatusCode().value());
        ResponseEntity<String> detalle = http.exchange(
                url("/api/usuarios/" + id),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class
        );
        assertEquals(200, detalle.getStatusCode().value());
        assertEquals(
                false,
                objectMapper.readTree(detalle.getBody())
                        .path("activo")
                        .asBoolean()
        );
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM usuarios WHERE id = ?",
                        Integer.class,
                        id
                )
        );
    }

    @Test
    void registraYConsultaNovedadesParaAmbosRolesConDatosDeSesion()
            throws Exception {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(10);
        ResponseEntity<String> novedadVehiculo = postCon(
                operatorHeaders,
                "/api/novedades",
                Map.of(
                        "vehiculoId", VEHICULO_ID,
                        "descripcion", "Observación de prueba",
                        "responsable", "usuario-falso",
                        "fechaHora", "2035-01-01T00:00:00"
                )
        );

        assertEquals(201, novedadVehiculo.getStatusCode().value());
        JsonNode respuesta = objectMapper.readTree(novedadVehiculo.getBody());
        assertEquals(
                "test-operator",
                respuesta.path("responsable").asText()
        );
        assertEquals(VEHICULO_ID, respuesta.path("vehiculoId").asLong());
        assertTrue(respuesta.path("estanciaId").isNull());
        assertTrue(LocalDateTime.parse(respuesta.path("fechaHora").asText())
                .isAfter(antes));
        assertNotEquals(
                "2035-01-01T00:00:00",
                respuesta.path("fechaHora").asText()
        );

        ResponseEntity<String> porEstancia = postCon(
                adminHeaders,
                "/api/novedades",
                Map.of(
                        "estanciaId", ESTANCIA_ID,
                        "descripcion", "Novedad de estancia"
                )
        );
        assertEquals(201, porEstancia.getStatusCode().value());
        assertEquals(
                ESTANCIA_ID,
                objectMapper.readTree(porEstancia.getBody())
                        .path("estanciaId")
                        .asLong()
        );

        ResponseEntity<String> listado = http.exchange(
                url("/api/novedades"),
                HttpMethod.GET,
                new HttpEntity<>(operatorHeaders),
                String.class
        );
        ResponseEntity<String> detalle = http.exchange(
                url("/api/novedades/"
                        + respuesta.path("id").asLong()),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class
        );
        assertEquals(200, listado.getStatusCode().value());
        assertEquals(2, objectMapper.readTree(listado.getBody()).size());
        assertEquals(200, detalle.getStatusCode().value());

        ResponseEntity<String> dosVinculos = postCon(
                operatorHeaders,
                "/api/novedades",
                Map.of(
                        "vehiculoId", VEHICULO_ID,
                        "estanciaId", ESTANCIA_ID,
                        "descripcion", "Vínculo inválido"
                )
        );
        ResponseEntity<String> sinDescripcion = postCon(
                operatorHeaders,
                "/api/novedades",
                Map.of("vehiculoId", VEHICULO_ID, "descripcion", "  ")
        );
        assertEquals(400, dosVinculos.getStatusCode().value());
        assertEquals(400, sinDescripcion.getStatusCode().value());
    }

    @Test
    void restringeUsuariosYProtegeNovedadesConSesionYCsrf() {
        ResponseEntity<String> anonimo = http.getForEntity(
                url("/api/novedades"),
                String.class
        );
        ResponseEntity<String> operarioNovedades = http.exchange(
                url("/api/novedades"),
                HttpMethod.GET,
                new HttpEntity<>(operatorHeaders),
                String.class
        );
        ResponseEntity<String> operarioUsuarios = http.exchange(
                url("/api/usuarios"),
                HttpMethod.GET,
                new HttpEntity<>(operatorHeaders),
                String.class
        );
        HttpHeaders sinCsrf = new HttpHeaders();
        sinCsrf.putAll(operatorHeaders);
        sinCsrf.remove("X-XSRF-TOKEN");
        sinCsrf.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> creacionSinCsrf = http.exchange(
                url("/api/novedades"),
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of(
                                "vehiculoId", VEHICULO_ID,
                                "descripcion", "Solicitud sin CSRF"
                        ),
                        sinCsrf
                ),
                String.class
        );

        assertEquals(401, anonimo.getStatusCode().value());
        assertEquals(200, operarioNovedades.getStatusCode().value());
        assertEquals(403, operarioUsuarios.getStatusCode().value());
        assertEquals(403, creacionSinCsrf.getStatusCode().value());
    }

    @Test
    void conservaRolesDeSesionYRestringeLaAdministracionDeCeldas()
            throws Exception {
        ResponseEntity<String> adminSession = http.exchange(
                url("/api/auth/session"),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class
        );
        ResponseEntity<String> operatorSession = http.exchange(
                url("/api/auth/session"),
                HttpMethod.GET,
                new HttpEntity<>(operatorHeaders),
                String.class
        );
        ResponseEntity<String> lecturaOperativa = http.exchange(
                url("/api/vehiculos"),
                HttpMethod.GET,
                new HttpEntity<>(operatorHeaders),
                String.class
        );
        HttpHeaders operatorJsonHeaders = new HttpHeaders();
        operatorJsonHeaders.putAll(operatorHeaders);
        operatorJsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> escrituraCelda = http.exchange(
                url("/api/celdas"),
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("codigo", "TEST-01", "tipo", "CARRO"),
                        operatorJsonHeaders
                ),
                String.class
        );
        String passwordHash = jdbc.queryForObject(
                "SELECT password_hash FROM cuentas_personal WHERE nombre_usuario = ?",
                String.class,
                "test-admin"
        );

        assertEquals(
                "ADMINISTRADOR",
                objectMapper.readTree(adminSession.getBody())
                        .path("role")
                        .asText()
        );
        assertEquals(
                "OPERARIO",
                objectMapper.readTree(operatorSession.getBody())
                        .path("role")
                        .asText()
        );
        assertEquals(200, lecturaOperativa.getStatusCode().value());
        assertEquals(403, escrituraCelda.getStatusCode().value());
        assertNotEquals("TestAdminPassword123!", passwordHash);
        assertTrue(passwordHash.startsWith("$2"));
    }

    @Test
    void exigeTokenCsrfParaEscriturasDeSesion() {
        HttpHeaders sinTokenCsrf = new HttpHeaders();
        sinTokenCsrf.putAll(adminHeaders);
        sinTokenCsrf.remove("X-XSRF-TOKEN");

        ResponseEntity<String> respuesta = http.exchange(
                url("/api/mensualidades/membresias"),
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of(
                                "usuarioId", USUARIO_ID,
                                "fechaInicio", "2020-01-01"
                        ),
                        sinTokenCsrf
                ),
                String.class
        );

        assertEquals(403, respuesta.getStatusCode().value());
    }

    @Test
    void rechazaCredencialesIncorrectasSinCrearSesion() throws Exception {
        ResponseEntity<String> csrfResponse = http.getForEntity(
                url("/api/auth/csrf"),
                String.class
        );
        String csrfCookie = cookie(csrfResponse.getHeaders(), "XSRF-TOKEN");
        String csrfValue = csrfCookie.substring("XSRF-TOKEN=".length());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.COOKIE, csrfCookie);
        headers.set("X-XSRF-TOKEN", csrfValue);

        ResponseEntity<String> respuesta = http.exchange(
                url("/api/auth/login"),
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of(
                                "username", "test-admin",
                                "password", "incorrecta-contraseña"
                        ),
                        headers
                ),
                String.class
        );

        assertEquals(401, respuesta.getStatusCode().value());
        assertTrue(objectMapper.readTree(respuesta.getBody())
                .path("mensaje")
                .asText()
                .contains("incorrectos"));
    }

    @Test
    void cerrarSesionInvalidaLaCookieDeSesion() {
        ResponseEntity<String> logout = http.exchange(
                url("/api/auth/logout"),
                HttpMethod.POST,
                new HttpEntity<>(adminHeaders),
                String.class
        );
        ResponseEntity<String> session = http.exchange(
                url("/api/auth/session"),
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class
        );

        assertEquals(204, logout.getStatusCode().value());
        assertEquals(401, session.getStatusCode().value());
    }

    private ResponseEntity<String> post(String path, Map<String, Object> body) {
        return postCon(adminHeaders, path, body);
    }

    private ResponseEntity<String> postCon(
            HttpHeaders authenticatedHeaders,
            String path,
            Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(authenticatedHeaders);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return http.exchange(
                url(path),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    private ResponseEntity<String> putCon(
            HttpHeaders authenticatedHeaders,
            String path,
            Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(authenticatedHeaders);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return http.exchange(
                url(path),
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    private HttpHeaders iniciarSesion(String username, String password)
            throws Exception {
        ResponseEntity<String> csrfResponse = http.getForEntity(
                url("/api/auth/csrf"),
                String.class
        );
        assertEquals(200, csrfResponse.getStatusCode().value());
        String csrfCookie = cookie(csrfResponse.getHeaders(), "XSRF-TOKEN");
        assertTrue(csrfCookie != null && !csrfCookie.isBlank());
        String csrfValue = csrfCookie.substring("XSRF-TOKEN=".length());

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        loginHeaders.set(HttpHeaders.COOKIE, csrfCookie);
        loginHeaders.set("X-XSRF-TOKEN", csrfValue);
        ResponseEntity<String> login = http.exchange(
                url("/api/auth/login"),
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("username", username, "password", password),
                        loginHeaders
                ),
                String.class
        );
        assertEquals(200, login.getStatusCode().value());

        String sessionCookie = cookie(login.getHeaders(), "JSESSIONID");
        assertTrue(sessionCookie != null && !sessionCookie.isBlank());
        HttpHeaders authenticatedHeaders = new HttpHeaders();
        authenticatedHeaders.set(
                HttpHeaders.COOKIE,
                sessionCookie + "; " + csrfCookie
        );
        authenticatedHeaders.set("X-XSRF-TOKEN", csrfValue);
        return authenticatedHeaders;
    }

    private String cookie(HttpHeaders headers, String name) {
        return headers.getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .map(value -> value.split(";", 2)[0])
                .filter(value -> value.startsWith(name + "="))
                .findFirst()
                .orElse(null);
    }

    private String url(String path) {
        return URI.create("http://localhost:" + port + path).toString();
    }

    private void limpiarDatos() {
        jdbc.update(
                "DELETE FROM novedades WHERE vehiculo_id = ? OR estancia_id = ?",
                VEHICULO_ID,
                ESTANCIA_ID
        );
        jdbc.update("DELETE FROM estancias WHERE id = ?", ESTANCIA_ID);
        jdbc.update("DELETE FROM vehiculos WHERE id = ?", VEHICULO_ID);
        jdbc.update("DELETE FROM celdas WHERE id = ?", CELDA_ID);
        jdbc.update(
                "DELETE FROM pagos_mensuales WHERE usuario_id = ?",
                USUARIO_ID
        );
        jdbc.update(
                "DELETE FROM membresias_mensuales WHERE usuario_id = ?",
                USUARIO_ID
        );
        jdbc.update("DELETE FROM usuarios WHERE id = ?", USUARIO_ID);
    }
}
