package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.PagoMensual;
import co.autoscolombia.domain.port.PagoMensualRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaPagoMensualRepository implements PagoMensualRepository {

    private final SpringDataPagoMensual jpa;

    public JpaPagoMensualRepository(SpringDataPagoMensual jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<PagoMensual> buscarPorUsuarioYPeriodo(
            Long usuarioId,
            String periodo) {
        return jpa.findByUsuario_IdAndPeriodo(usuarioId, periodo);
    }

    @Override
    public List<PagoMensual> listarPorPeriodo(String periodo) {
        return jpa.findAllByPeriodoOrderByFechaPagoDesc(periodo);
    }

    @Override
    public List<PagoMensual> listar() {
        return jpa.findAllByOrderByFechaPagoDesc();
    }

    @Override
    public PagoMensual guardar(PagoMensual pago) {
        return jpa.save(pago);
    }
}
