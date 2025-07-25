package com.ecommerce.ecommapis.services.auth;

import com.ecommerce.ecommapis.dto.auth.MailBody;
import com.ecommerce.ecommapis.exception.ResourceNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
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
        try
        {
            final MimeMessage message = javaMailSender.createMimeMessage();

            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(mailBody.to());
            helper.setFrom(mailFrom);
            helper.setSubject(mailBody.subject());
            helper.setText(mailBody.body(), true); // Set HTML content

            javaMailSender.send(message);
        }
        catch (final MessagingException e)
        {
            throw new ResourceNotFoundException("Failed to send email" + e);
        }
    }
}
