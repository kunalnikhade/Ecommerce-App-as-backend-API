package com.ecommerce.ecommapis.services.forgotPassword;

import com.ecommerce.ecommapis.dto.MailBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService
{
    @Value("${app.mail.from}")
    private String mailFrom;

    private final JavaMailSender javaMailSender;

    public EmailService(final JavaMailSender javaMailSender)
    {
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleMessage(final MailBody mailBody)
    {
        final SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(mailBody.to());
        message.setFrom(mailFrom);
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());

        javaMailSender.send(message);
    }
}
