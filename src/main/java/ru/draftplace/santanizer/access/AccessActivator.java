package ru.draftplace.santanizer.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    private final JavaMailSender mailSender;

    @Value("${notification.mail.from}")
    private String mailFrom;

    @Autowired
    public AccessActivator(
            AccessRequestRepository requestRepository,
            JavaMailSender mailSender
    )
    {
        this.requestRepository = requestRepository;
        this.mailSender = mailSender;
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
            log.info("===\nKey: " + request.getKey() + "\n===");
            // send key by email
            notify(request);
        });
    }

    private String generateKey()
    {
        return UUID.randomUUID().toString();
    }

    private void notify(AccessRequest request)
    {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(request.getEmail());
        mailMessage.setSubject("Santanizer");
        mailMessage.setText("Your access request was accepted.\n\nUse key " + request.getKey());

        mailSender.send(mailMessage);
    }
}
