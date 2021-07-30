package ru.draftplace.santanizer.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.draftplace.santanizer.access.dao.AccessRequest;
import ru.draftplace.santanizer.access.dao.AccessRequestRepository;
import ru.draftplace.santanizer.access.dao.AccessRequestStatus;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AccessActivator
{
    private final AccessRequestRepository requestRepository;

    @Value("${notification.mail.from}")
    private String mailFrom;

    private final Session mailSession;

    @Value("${server.domain}")
    private String linkDomain;

    @Autowired
    public AccessActivator(
            AccessRequestRepository requestRepository,
            Session mailSession
    )
    {
        this.requestRepository = requestRepository;
        this.mailSession = mailSession;
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
            try {
                notify(request);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
    }

    private String generateKey()
    {
        return UUID.randomUUID().toString();
    }

    private void notify(AccessRequest request) throws MessagingException
    {
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(mailFrom);
        message.setRecipients(Message.RecipientType.TO, request.getEmail());
        message.setSubject("You request was accepted. Santanizer.");

        String link = linkDomain + "/?key=" + request.getKey();
        message.setContent("<p>Your access request was accepted.</p><p><a href=\"" + link + "\">Click to start</a></p><br><p>If you don't understand what this is about, just ignore the message.</p>",
                "text/html");

        Transport.send(message);
    }
}
