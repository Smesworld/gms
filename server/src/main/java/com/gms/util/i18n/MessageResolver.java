package com.gms.util.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * MessageResolver
 *
 * @author Asiel Leal Celdeiro | lealceldeiro@gmail.com
 *
 * @version 0.1
 * Dec 12, 2017
 */
public class MessageResolver {

    private final MessageSource messageSource;

    public MessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @SuppressWarnings("WeakerAccess")
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }

    public String getMessage(String code) {
        return getMessage(code, new Object[0]);
    }
}