package co.autoscolombia.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.autoscolombia.application.MensualidadService;
import co.autoscolombia.application.ConflictoMensualidadException;
import co.autoscolombia.application.dto.EstadoMensualidadResponse;
import co.autoscolombia.application.dto.PagoMensualResponse;
import co.autoscolombia.application.dto.RegistrarPagoMensualRequest;
import co.autoscolombia.application.dto.ResultadoIdempotente;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MensualidadControllerTest {

    private MensualidadService service;
    private MockMvc http;

    @BeforeEach
    void setUp() {
        service = mock(MensualidadService.class);
        http = MockMvcBuilders
                .standaloneSetup(new MensualidadController(service))
                .build();
    }

    @Test
    void postPagoMensualRecibeJsonYDevuelveElPagoCreado() throws Exception {
        when(service.registrarPago(any(RegistrarPagoMensualRequest.class)))
                .thenReturn(new ResultadoIdempotente<>(
                        new PagoMensualResponse(
                                45L,
                                12L,
                                "Ana Pérez",
                                "2026-11",
                                new BigDecimal("125000"),
                                "EFECTIVO",
                                "PAGADO",
                                null
                        ),
                        false
                ));

        http.perform(post("/api/mensualidades/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 12,
                                  "periodo": "2026-11",
                                  "valor": 125000,
                                  "metodo": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(45))
                .andExpect(jsonPath("$.usuarioId").value(12))
                .andExpect(jsonPath("$.periodo").value("2026-11"))
                .andExpect(jsonPath("$.valor").value(125000))
                .andExpect(jsonPath("$.estado").value("PAGADO"));

        ArgumentCaptor<RegistrarPagoMensualRequest> solicitud =
                ArgumentCaptor.forClass(RegistrarPagoMensualRequest.class);
        verify(service).registrarPago(solicitud.capture());
        assertEquals(12L, solicitud.getValue().usuarioId());
        assertEquals("2026-11", solicitud.getValue().periodo());
        assertEquals(new BigDecimal("125000"), solicitud.getValue().valor());
        assertEquals("EFECTIVO", solicitud.getValue().metodo());
    }

    @Test
    void postPagoMensualDevuelve409CuandoLaClaveYaExisteConOtrosDatos()
            throws Exception {
        when(service.registrarPago(any(RegistrarPagoMensualRequest.class)))
                .thenThrow(new ConflictoMensualidadException(
                        "Ya existe un pago distinto para ese usuario y período."
                ));

        http.perform(post("/api/mensualidades/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": 12,
                                  "periodo": "2026-11",
                                  "valor": 125000,
                                  "metodo": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("Ya existe un pago distinto para ese usuario y período."));
    }

    @Test
    void getEstadoMensualidadDevuelveDatosPorPeriodo() throws Exception {
        when(service.consultarEstado("2026-11")).thenReturn(List.of(
                new EstadoMensualidadResponse(
                        12L,
                        "Ana Pérez",
                        "123456",
                        "2026-11",
                        "PENDIENTE",
                        null,
                        null,
                        null
                )
        ));

        http.perform(get("/api/mensualidades/estado")
                        .param("periodo", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(12))
                .andExpect(jsonPath("$[0].periodo").value("2026-11"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }
}
