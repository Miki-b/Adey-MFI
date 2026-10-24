package et.adey.mfi.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, Long> {

    Optional<ReconciliationRun> findTopByBusinessDateAndStatusOrderByIdDesc(LocalDate date, String status);
}
