package ru.draftplace.santanizer.access.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, String>
{
    Optional<AccessRequest> findOneByStatusInAndEmail(Collection<AccessRequestStatus> statuses, String email);

    List<AccessRequest> findAllByStatusOrderByIdAsc(AccessRequestStatus status, Pageable pageable);

    List<AccessRequest> findAllByStatusAndExpirationBeforeOrderByIdAsc(
            AccessRequestStatus status,
            Time expiration,
            Pageable pageable
    );

    Optional<AccessRequest> findOneByKey(String key);
}
