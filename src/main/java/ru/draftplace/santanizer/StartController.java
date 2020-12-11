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

import java.util.Set;
import java.util.UUID;

@Controller
@Slf4j
public class StartController
{
    private final KeyPersonStorage storage;

    private final NotificationSender notificationSender;

    @Autowired
    public StartController(KeyPersonStorage storage, NotificationSender notificationSender)
    {
        this.storage = storage;
        this.notificationSender = notificationSender;
    }

    /**
     * Стартовый экран - список участников и форма добавления.
     *
     * @return String
     */
    @GetMapping("/")
    public String start(@RequestParam(required = false) String key, Model view)
    {

        if (key == null || !storage.has(key)) {
            // генерация ключа (ID "сессии")
            key = UUID.randomUUID().toString();
            storage.register(key);
        }

        view.addAttribute("person", new Person()); // для добавления
        view.addAttribute("persons", storage.get(key)); // список
        view.addAttribute("canNext", storage.size(key) > 1); // возможность перехода
        view.addAttribute("key", key);

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
            @RequestParam String key
    )
    {

        if (!person.isEmpty()) {
            storage.add(key, person);
            log.info("persons count: " + storage.size(key));
        }

        log.info("next action: " + nextAction);

        return nextAction.equals("next")
                ? new RedirectView("/processing?key=" + key)
                : new RedirectView("/?key=" + key);
    }

    /**
     * Обработка участников (формирование пар, отправка уведомлений).
     *
     * @return RedirectView
     */
    @GetMapping("/processing")
    public RedirectView processing(@RequestParam String key)
    {

        if (storage.size(key) < 2) {
            log.info("Persons count is too low. Required at least 2 persons.");
            return new RedirectView("/");
        }

        log.info("Run processing...");
        PairSelector pairSelector = new PairSelector(storage.get(key));

        // пары
        Set<Pair> result = pairSelector.select();
        log.info("ProcessingPairs selected.");

        System.out.println(result);
        for (Pair pair : result) {
            notificationSender.notifySanta(pair.getSanta());
        }
        log.info("Notifications sent.");

        return new RedirectView("/result?key=" + key);
    }

    /**
     * Завершение, уведомление об ошибке или успешной обработке.
     *
     * @return String
     */
    @GetMapping("result")
    public String result(@RequestParam String key, Model view)
    {
        boolean processed = true;

        if (storage.size(key) < 2) {
            processed = false;
            view.addAttribute("error", "Persons list is too short. Need at least 2 entries.");
        }

        view.addAttribute("processed", processed);

        storage.forget(key);
        log.info("Persons list was cleaned.");

        return "result";
    }
}
