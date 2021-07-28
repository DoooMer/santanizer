package ru.draftplace.santanizer.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.draftplace.santanizer.access.dao.AccessRequestRepository;
import ru.draftplace.santanizer.access.dao.AccessRequestStatus;
import ru.draftplace.santanizer.access.model.AccessRequest;

import java.util.ArrayList;
import java.util.Optional;

@Controller
@Slf4j
public class AccessController
{
    private final AccessRequestRepository accessRequestRepository;

    @Value("${recaptcha.client.secret}")
    private String reCaptchaClientSecret;

    @Autowired
    public AccessController(AccessRequestRepository accessRequestRepository)
    {
        this.accessRequestRepository = accessRequestRepository;
    }

    @GetMapping("/access")
    public String form(Model view)
    {
        view.addAttribute(new AccessRequest());
        view.addAttribute("recaptchaSecret", reCaptchaClientSecret);

        return "access/form";
        // 6Lea-sAbAAAAADEryWVUJeAsQJCjRu303KXTR1R1
    }

    @PostMapping("/access/request")
    public String request(@ModelAttribute AccessRequest accessRequest)
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
                log.info("warning request access to <> already exists");
                return "access/warning";
            }
            log.info("error request access to <> already granted");
            return "access/error";
        }

        // добавить запрос без времени истечения и кода

        ru.draftplace.santanizer.access.dao.AccessRequest request = new ru.draftplace.santanizer.access.dao.AccessRequest();
        request.setEmail(accessRequest.getEmail());
        accessRequestRepository.save(request);
        log.info("Access request from <" + request.getEmail() + "> registered.");

        return "access/success";
    }
}
