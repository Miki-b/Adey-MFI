package et.adey.mfi.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByUsernameAndKey(String username, String key);

    @Modifying
    @Transactional
    @Query("delete from IdempotencyRecord r where r.expiresAt <= :now")
    int deleteExpired(Instant now);
}
