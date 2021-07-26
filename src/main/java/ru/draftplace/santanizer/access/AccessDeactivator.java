package ru.draftplace.santanizer.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.draftplace.santanizer.access.dao.AccessRequest;
import ru.draftplace.santanizer.access.dao.AccessRequestRepository;
import ru.draftplace.santanizer.access.dao.AccessRequestStatus;

import java.sql.Time;
import java.util.List;

@Service
@Slf4j
public class AccessDeactivator
{
    private final AccessRequestRepository requestRepository;

    @Autowired
    public AccessDeactivator(AccessRequestRepository requestRepository)
    {
        this.requestRepository = requestRepository;
    }

    public void deactivateByTimeout(int timeout)
    {
        List<AccessRequest> requests = requestRepository.findAllByStatusAndExpirationBeforeOrderByIdAsc(
                AccessRequestStatus.ACCEPTED,
                new Time(System.currentTimeMillis()),
                PageRequest.of(0, 5)
        );

        // активировать каждый
        requests.forEach(request -> {
            request.setStatus(AccessRequestStatus.CLOSED);
            requestRepository.save(request);
            log.info("Request for <" + request.getEmail() + "> closed by timeout " + timeout);
            log.info("===\nKey: " + request.getKey() + "\n===");
        });
    }
}
