package ru.draftplace.santanizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class NotificationSender
{
    private final JavaMailSender mailSender;

    @Value("${notification.mail.from}")
    private String mailFrom;

    @Autowired
    public NotificationSender(JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
    }

    public void notifySanta(Person santa)
    {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(santa.getEmail());
        mailMessage.setSubject("You are secret santa! Congratulations!");
        mailMessage.setText("For you was selected: " + santa.getName() + "\nCongratulate him(her).\n\nHappy New Year!");

        mailSender.send(mailMessage);
    }
}
