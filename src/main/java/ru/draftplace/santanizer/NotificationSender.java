package ru.draftplace.santanizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Queue;

@Component
public class NotificationSender
{
    private final JavaMailSender mailSender;

    @Value("${notification.mail.from}")
    private String mailFrom;

    // очередь отправки писем
    private final Queue<SimpleMailMessage> mailQueue;

    @Autowired
    public NotificationSender(JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
        mailQueue = new ArrayDeque<>();
    }

    /**
     * Уведомление санты.
     */
    public void notifySanta(Person santa, Person person)
    {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(santa.getEmail());
        mailMessage.setSubject("You are secret santa! Congratulations!");
        mailMessage.setText("For you was selected: " + person.getName() + "\nCongratulate him(her).\n\nHappy New Year!");

        // письмо добавляется в очередь
        mailQueue.offer(mailMessage);
    }


    /**
     * Отправка одного письма из очередь.
     * Возвращает количество писем оставшихся в очереди.
     *
     * @return int
     */
    public int sendNextQueuedMail()
    {
        var mail = mailQueue.poll();

        if (mail != null) {
            mailSender.send(mail);
        }

        return mailQueue.size();
    }
}
