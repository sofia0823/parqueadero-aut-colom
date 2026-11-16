package co.autoscolombia.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.autoscolombia.application.dto.EstadoMensualidadResponse;
import co.autoscolombia.application.dto.RegistrarPagoMensualRequest;
import co.autoscolombia.application.ConflictoMensualidadException;
import co.autoscolombia.domain.model.MembresiaMensual;
import co.autoscolombia.domain.model.PagoMensual;
import co.autoscolombia.domain.model.Usuario;
import co.autoscolombia.domain.port.MembresiaMensualRepository;
import co.autoscolombia.domain.port.PagoMensualRepository;
import co.autoscolombia.domain.port.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MensualidadServiceTest {

    private UsuarioRepository usuarios;
    private MembresiaMensualRepository membresias;
    private PagoMensualRepository pagos;
    private MensualidadService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        membresias = mock(MembresiaMensualRepository.class);
        pagos = mock(PagoMensualRepository.class);
        usuario = mock(Usuario.class);
        service = new MensualidadService(usuarios, membresias, pagos);

        when(usuario.getId()).thenReturn(7L);
        when(usuario.getNombres()).thenReturn("Ana Pérez");
        when(usuario.getNumeroDocumento()).thenReturn("123456");
    }

    @Test
    void registrarPagoMensualAceptaPeriodoCubiertoYMetodoNormalizado() {
        String periodo = YearMonth.now().toString();
        when(usuarios.buscarPorIdConBloqueo(7L))
                .thenReturn(Optional.of(usuario));
        when(membresias.listarPorUsuario(7L)).thenReturn(List.of(
                new MembresiaMensual(usuario, LocalDate.of(2020, 1, 1))
        ));
        when(pagos.buscarPorUsuarioYPeriodo(7L, periodo))
                .thenReturn(Optional.empty());
        when(pagos.guardar(org.mockito.ArgumentMatchers.any(PagoMensual.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = service.registrarPago(new RegistrarPagoMensualRequest(
                7L,
                periodo,
                new BigDecimal("125000"),
                " efectivo "
        ));

        assertEquals(7L, resultado.resultado().usuarioId());
        assertEquals(periodo, resultado.resultado().periodo());
        assertEquals(new BigDecimal("125000"), resultado.resultado().valor());
        assertEquals("EFECTIVO", resultado.resultado().metodo());
        assertEquals("PAGADO", resultado.resultado().estado());
        assertEquals(false, resultado.repetida());
    }

    @Test
    void registrarPagoConMembresiaDeUnDiaConservaElValorMensualCompleto() {
        YearMonth periodo = YearMonth.now();
        LocalDate diaVigente = periodo.atDay(15);
        MembresiaMensual membresia = new MembresiaMensual(usuario, diaVigente);
        membresia.finalizar(diaVigente);
        when(usuarios.buscarPorIdConBloqueo(7L))
                .thenReturn(Optional.of(usuario));
        when(membresias.listarPorUsuario(7L))
                .thenReturn(List.of(membresia));
        when(pagos.buscarPorUsuarioYPeriodo(7L, periodo.toString()))
                .thenReturn(Optional.empty());
        when(pagos.guardar(org.mockito.ArgumentMatchers.any(PagoMensual.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = service.registrarPago(new RegistrarPagoMensualRequest(
                7L,
                periodo.toString(),
                new BigDecimal("125000"),
                "EFECTIVO"
        ));

        assertEquals(new BigDecimal("125000"), resultado.resultado().valor());
        assertEquals(false, resultado.repetida());
    }

    @Test
    void registrarPagoMensualRechazaUsuarioSinMembresiaEnElPeriodo() {
        String periodo = YearMonth.now().toString();
        MembresiaMensual membresia = new MembresiaMensual(
                usuario,
                LocalDate.of(2020, 1, 1)
        );
        membresia.finalizar(YearMonth.now().minusMonths(2).atEndOfMonth());
        when(usuarios.buscarPorIdConBloqueo(7L))
                .thenReturn(Optional.of(usuario));
        when(pagos.buscarPorUsuarioYPeriodo(7L, periodo))
                .thenReturn(Optional.empty());
        when(membresias.listarPorUsuario(7L)).thenReturn(List.of(membresia));

        assertThrows(IllegalArgumentException.class, () ->
                service.registrarPago(new RegistrarPagoMensualRequest(
                        7L,
                        periodo,
                        new BigDecimal("125000"),
                        "EFECTIVO"
                ))
        );
    }

    @Test
    void consultarEstadoMarcaPeriodosAnterioresComoVencidos() {
        YearMonth periodo = YearMonth.now().minusMonths(1);
        when(membresias.listar()).thenReturn(List.of(
                new MembresiaMensual(
                        usuario,
                        periodo.atDay(1).minusMonths(1)
                )
        ));
        when(pagos.listarPorPeriodo(periodo.toString())).thenReturn(List.of());

        List<EstadoMensualidadResponse> resultado =
                service.consultarEstado(periodo.toString());

        assertEquals(1, resultado.size());
        assertEquals("VENCIDO", resultado.get(0).estado());
    }

    @Test
    void registrarPagoMensualRechazaPeriodoFuturo() {
        String periodoFuturo = YearMonth.now().plusMonths(1).toString();

        assertThrows(IllegalArgumentException.class, () ->
                service.registrarPago(new RegistrarPagoMensualRequest(
                        7L,
                        periodoFuturo,
                        new BigDecimal("125000"),
                        "EFECTIVO"
                ))
        );
    }

    @Test
    void repetirElMismoPagoDevuelveElRegistroExistente() {
        String periodo = YearMonth.now().toString();
        PagoMensual existente = new PagoMensual(
                usuario,
                periodo,
                new BigDecimal("125000.00"),
                "EFECTIVO"
        );
        when(usuarios.buscarPorIdConBloqueo(7L))
                .thenReturn(Optional.of(usuario));
        when(pagos.buscarPorUsuarioYPeriodo(7L, periodo))
                .thenReturn(Optional.of(existente));

        var resultado = service.registrarPago(new RegistrarPagoMensualRequest(
                7L,
                periodo,
                new BigDecimal("125000"),
                " efectivo "
        ));

        assertEquals(true, resultado.repetida());
        assertEquals(
                new BigDecimal("125000.00"),
                resultado.resultado().valor()
        );
        assertEquals("EFECTIVO", resultado.resultado().metodo());
    }

    @Test
    void cambiarDatosDeUnPagoExistenteDevuelveConflicto() {
        String periodo = YearMonth.now().toString();
        PagoMensual existente = new PagoMensual(
                usuario,
                periodo,
                new BigDecimal("125000"),
                "EFECTIVO"
        );
        when(usuarios.buscarPorIdConBloqueo(7L))
                .thenReturn(Optional.of(usuario));
        when(pagos.buscarPorUsuarioYPeriodo(7L, periodo))
                .thenReturn(Optional.of(existente));

        assertThrows(ConflictoMensualidadException.class, () ->
                service.registrarPago(new RegistrarPagoMensualRequest(
                        7L,
                        periodo,
                        new BigDecimal("130000"),
                        "EFECTIVO"
                ))
        );
    }

    @Test
    void repetirLaMismaInscripcionDevuelveLaMembresiaExistente() {
        LocalDate inicio = LocalDate.of(2020, 1, 1);
        MembresiaMensual existente = new MembresiaMensual(usuario, inicio);
        when(usuarios.buscarPorIdConBloqueo(7L))
                .thenReturn(Optional.of(usuario));
        when(membresias.listarPorUsuario(7L)).thenReturn(List.of(existente));

        var resultado = service.registrarMembresia(
                new co.autoscolombia.application.dto.RegistrarMembresiaRequest(
                        7L,
                        inicio
                )
        );

        assertEquals(true, resultado.repetida());
        assertEquals(inicio, resultado.resultado().fechaInicio());
    }
}
