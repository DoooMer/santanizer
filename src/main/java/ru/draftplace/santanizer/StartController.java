package ru.draftplace.santanizer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import ru.draftplace.santanizer.access.dao.AccessRequest;
import ru.draftplace.santanizer.access.dao.AccessRequestRepository;
import ru.draftplace.santanizer.access.dao.AccessRequestStatus;

import java.util.Optional;
import java.util.Set;

@Controller
@Slf4j
public class StartController
{
    private final KeyPersonStorage storage;

    private final NotificationSender notificationSender;

    private final AccessRequestRepository accessRequestRepository;

    @Autowired
    public StartController(
            KeyPersonStorage storage,
            NotificationSender notificationSender,
            AccessRequestRepository accessRequestRepository
    )
    {
        this.storage = storage;
        this.notificationSender = notificationSender;
        this.accessRequestRepository = accessRequestRepository;
    }

    /**
     * Стартовый экран - список участников и форма добавления.
     *
     * @return String
     */
    @GetMapping("/")
    public String start(@RequestParam(required = false) String key, Model view)
    {
        try {
            AccessRequest accessRequest = validateAccessByKey(key);
        } catch (NoAccessException e) {
            return "register";
        }

        if (!storage.has(key)) {
            storage.register(key);
        }

        view.addAttribute("person", new Person()); // для добавления
        view.addAttribute("persons", storage.get(key)); // список
        view.addAttribute("canNext", storage.size(key) > 1); // возможность перехода
        view.addAttribute("key", key);

        log.info(logPrefix(key) + "Starting.");

        return "start";
    }

    /**
     * Добавление участника с возвратом на стартовый экран или переходом к обработке.
     *
     * @param person новый участник
     * @return RedirectView
     */
    @PostMapping("/add")
    public RedirectView add(
            @ModelAttribute Person person,
            @RequestParam(defaultValue = "add") String nextAction,
            @RequestParam String key,
            UriComponentsBuilder uriBuilder
    )
    {

        try {
            AccessRequest accessRequest = validateAccessByKey(key);
        } catch (NoAccessException e) {
            return new RedirectView("/access");
        }

        if (!person.isEmpty()) {
            storage.add(key, person);
            log.info(logPrefix(key) + "persons count: " + storage.size(key));
        }

        log.info(logPrefix(key) + "next action: " + nextAction);

        uriBuilder.queryParam("key", key);
        var uri = (nextAction.equals("next") ? uriBuilder.path("/processing") : uriBuilder.path("/"))
                .build()
                .toUri()
                .toString();

        return new RedirectView(uri);
    }

    /**
     * Обработка участников (формирование пар, отправка уведомлений).
     *
     * @return RedirectView
     */
    @GetMapping("/processing")
    public RedirectView processing(@RequestParam String key, UriComponentsBuilder uriBuilder)
    {
        try {
            AccessRequest accessRequest = validateAccessByKey(key);
        } catch (NoAccessException e) {
            return new RedirectView("/access");
        }

        if (storage.size(key) < 2) {
            log.info(logPrefix(key) + "Persons count is too low. Required at least 2 persons.");
            return new RedirectView("/");
        }

        log.info(logPrefix(key) + "Run processing...");
        PairSelector pairSelector = new PairSelector(storage.get(key));

        // пары
        Set<Pair> result = pairSelector.select();
        log.info(logPrefix(key) + "ProcessingPairs selected.");

        for (Pair pair : result) {
            notificationSender.notifySanta(pair.getSanta(), pair.getPerson());
        }

        log.info(logPrefix(key) + "Notifications sent.");
        var uri = uriBuilder
                .queryParam("key", key)
                .path("/result")
                .build()
                .toUri()
                .toString();

        return new RedirectView(uri);
    }

    /**
     * Завершение, уведомление об ошибке или успешной обработке.
     *
     * @return String
     */
    @GetMapping("result")
    public String result(@RequestParam String key, Model view)
    {
        AccessRequest accessRequest;

        try {
            accessRequest = validateAccessByKey(key);
        } catch (NoAccessException e) {
            return "register";
        }

        boolean processed = true;

        if (storage.size(key) < 2) {
            processed = false;
            view.addAttribute("error", "Persons list is too short. Need at least 2 entries.");
        }

        view.addAttribute("processed", processed);

        storage.forget(key);
        accessRequest.setStatus(AccessRequestStatus.CLOSED);
        log.info(logPrefix(key) + "Persons list was cleaned.");
        log.info(logPrefix(key) + "Access is closed.");

        return "result";
    }

    protected AccessRequest validateAccessByKey(String key)
    {
        Optional<AccessRequest> accessRequest = accessRequestRepository.findOneByKey(key);

        if (accessRequest.isEmpty()) {
            log.warn(logPrefix(key) + "Try access by invalid key.");
            throw new NoAccessException();
        }

        return accessRequest.get();
    }

    private String logPrefix(String key)
    {
        return "[" + key + "] ";
    }
}
