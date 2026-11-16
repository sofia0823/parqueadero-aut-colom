package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.PagoMensual;
import java.util.List;
import java.util.Optional;

public interface PagoMensualRepository {

    Optional<PagoMensual> buscarPorUsuarioYPeriodo(Long usuarioId, String periodo);

    List<PagoMensual> listarPorPeriodo(String periodo);

    List<PagoMensual> listar();

    PagoMensual guardar(PagoMensual pago);
}
