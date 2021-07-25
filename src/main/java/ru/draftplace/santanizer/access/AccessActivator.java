package ru.draftplace.santanizer.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.draftplace.santanizer.access.dao.AccessRequest;
import ru.draftplace.santanizer.access.dao.AccessRequestRepository;
import ru.draftplace.santanizer.access.dao.AccessRequestStatus;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AccessActivator
{
    private final AccessRequestRepository requestRepository;

    @Autowired
    public AccessActivator(AccessRequestRepository requestRepository)
    {
        this.requestRepository = requestRepository;
    }

    public void activate()
    {
        // получить запросы в порядке добавления
        List<AccessRequest> requests = requestRepository.findAllByStatusOrderByIdAsc(
                AccessRequestStatus.WAITING,
                PageRequest.of(0, 5)
        );

        // активировать каждый
        requests.forEach(request -> {
            request.setKey(generateKey());
            request.setStatus(AccessRequestStatus.ACCEPTED);
            requestRepository.save(request);
            log.info("Request for <" + request.getEmail() + "> accepted");
            log.info("===\nUse key: " + request.getKey() + "\n===");
            // send key by email
        });
    }

    private String generateKey()
    {
        return UUID.randomUUID().toString();
    }
}
