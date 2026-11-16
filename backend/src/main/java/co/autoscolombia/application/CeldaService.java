
package co.autoscolombia.application;

import co.autoscolombia.domain.model.Celda;
import co.autoscolombia.domain.port.CeldaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CeldaService {

    private final CeldaRepository celdaRepository;

    public CeldaService(CeldaRepository celdaRepository) {
        this.celdaRepository = celdaRepository;
    }

    @Transactional(readOnly = true)
    public List<Celda> listar() {
        return celdaRepository.listar();
    }

    @Transactional(readOnly = true)
    public List<Celda> listarDisponibles() {
        return celdaRepository.listarDisponibles();
    }

    @Transactional
    public Celda registrar(String codigo, String tipo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                "El código de la celda es obligatorio."
            );
        }

        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException(
                "El tipo de la celda es obligatorio."
            );
        }

        String codigoNormalizado = codigo.trim().toUpperCase();
        String tipoNormalizado = tipo.trim().toUpperCase();

        if (celdaRepository.existeCodigo(codigoNormalizado)) {
            throw new IllegalArgumentException(
                "Ya existe una celda con el código " + codigoNormalizado
            );
        }

        Celda celda = new Celda(codigoNormalizado, tipoNormalizado);
        celda.setEstado(Celda.Estado.DISPONIBLE);

        return celdaRepository.guardar(celda);
    }

    @Transactional
    public List<Celda> crearVeinte() {
        List<Celda> creadas = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            String codigo = String.format("C%02d", i);

            if (!celdaRepository.existeCodigo(codigo)) {
                Celda celda = new Celda(codigo, "CARRO");
                celda.setEstado(Celda.Estado.DISPONIBLE);

                creadas.add(celdaRepository.guardar(celda));
            }
        }

        return creadas;
    }
}
