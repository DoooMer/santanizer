package ru.draftplace.santanizer.access.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, String>
{
    Optional<AccessRequest> findOneByEmail(String email);

    List<AccessRequest> findAllByStatusOrderByIdAsc(AccessRequestStatus status, Pageable pageable);

    Optional<AccessRequest> findOneByKey(String key);
}
