package et.adey.mfi.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReconciliationFindingRepository extends JpaRepository<ReconciliationFinding, Long> {

    List<ReconciliationFinding> findByRunIdOrderByIdAsc(Long runId);

    /** The latest RESOLVED finding of this type for this receipt (to carry the resolution into new runs). */
    java.util.Optional<ReconciliationFinding> findFirstByTypeAndReceiptNumberAndResolvedAtIsNotNullOrderByIdDesc(
            FindingType type, String receiptNumber);
}
