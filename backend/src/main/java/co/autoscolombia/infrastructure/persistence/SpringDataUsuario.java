package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Usuario;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataUsuario extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByIdDesc();

    boolean existsByNumeroDocumentoIgnoreCase(String numeroDocumento);

    boolean existsByNumeroDocumentoIgnoreCaseAndIdNot(
            String numeroDocumento,
            Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);
}
