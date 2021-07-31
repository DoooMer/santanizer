package ru.draftplace.santanizer.access;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;
import ru.draftplace.santanizer.access.dao.AccessRequestRepository;
import ru.draftplace.santanizer.access.dao.AccessRequestStatus;
import ru.draftplace.santanizer.access.model.AccessRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
public class AccessController
{
    private final AccessRequestRepository accessRequestRepository;

    @Value("${recaptcha.client.secret}")
    private String reCaptchaClientSecret;

    // временные ключи для отображения результата
    private final HashMap<UUID, Boolean> results;

    // ограничение частоты запросов
    private final Bucket rateLimit;

    @Autowired
    public AccessController(AccessRequestRepository accessRequestRepository)
    {
        this.accessRequestRepository = accessRequestRepository;
        results = new HashMap<>();
        rateLimit = Bucket4j.builder()
                .addLimit(Bandwidth.classic(3, Refill.greedy(5, Duration.ofHours(1))))
                .build();
    }

    @GetMapping("/access")
    public String form(Model view)
    {
        if (!rateLimit.tryConsume(1)) {
            log.warn("[/access] Request rate limit exceeded.");
            return "access/rate-limit";
        }

        view.addAttribute(new AccessRequest());
        view.addAttribute("recaptchaSecret", reCaptchaClientSecret);

        return "access/form";
        // 6Lea-sAbAAAAADEryWVUJeAsQJCjRu303KXTR1R1
    }

    @PostMapping("/access/request")
    public RedirectView request(@ModelAttribute AccessRequest accessRequest)
    {
        log.info("start processing request access from <" + accessRequest.getEmail() + ">");

        // проверить по email:
        // запрос ожидает - предупреждение
        // запрос одобрен и активен - ошибка
        // запрос одобрен, неактивен и не прошел таймаут - предупреждение
        // запрос отклонен и не прошел таймаут - предупреждение

        ArrayList<AccessRequestStatus> statuses = new ArrayList<>();
        statuses.add(AccessRequestStatus.WAITING);
        statuses.add(AccessRequestStatus.ACCEPTED);
        Optional<ru.draftplace.santanizer.access.dao.AccessRequest> currentAccess = accessRequestRepository.findOneByStatusInAndEmail(
                statuses,
                accessRequest.getEmail()
        );

        if (currentAccess.isPresent()) {
            if (currentAccess.get().getKey().isEmpty()) {
                log.info("warning request access to <" + currentAccess.get().getEmail() + "> already exists");
                return new RedirectView("/access/warning");
            }
            log.info("error request access to <" + currentAccess.get().getEmail() + "> already granted");
            return new RedirectView("/access/error");
        }

        // добавить запрос без времени истечения и кода

        var request = new ru.draftplace.santanizer.access.dao.AccessRequest();
        request.setEmail(accessRequest.getEmail());
        accessRequestRepository.save(request);
        UUID key = UUID.randomUUID();
        results.put(key, true);
        log.info("Access request from <" + request.getEmail() + "> registered.");

        return new RedirectView("/access/success/" + key);
    }

    @GetMapping("/access/warning/{uuid}")
    public String warning(@PathVariable UUID uuid)
    {
        var checkResult = checkResultID(uuid);

        return checkResult != null ? checkResult : "access/warning";
    }

    @GetMapping("/access/error/{uuid}")
    public String error(@PathVariable UUID uuid)
    {
        var checkResult = checkResultID(uuid);

        return checkResult != null ? checkResult : "access/error";
    }

    @GetMapping("/access/success/{uuid}")
    public String success(@PathVariable UUID uuid)
    {
        var checkResult = checkResultID(uuid);

        return checkResult != null ? checkResult : "access/success";
    }

    private String checkResultID(UUID uuid)
    {

        if (!results.getOrDefault(uuid, false)) {
            return "access/error";
        }

        results.remove(uuid);

        return null;
    }
}
