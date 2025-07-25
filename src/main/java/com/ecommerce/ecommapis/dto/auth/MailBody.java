package com.ecommerce.ecommapis.dto.auth;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String body)
{
}
