package co.autoscolombia.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
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
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(adminHeaders);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return http.exchange(
                url(path),
                HttpMethod.POST,
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
