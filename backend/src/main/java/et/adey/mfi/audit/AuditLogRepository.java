package et.adey.mfi.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data generates the implementation (save, findAll, and the query below from its name). */
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    List<AuditLogEntry> findByLoanIdOrderByIdAsc(Long loanId);
}
