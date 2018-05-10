package com.kairos.service.locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Created by vipul on 10/5/18.
 * Locale service implementation
 */
@Service
public class LocaleServiceImpl implements LocaleService{

    @Autowired
    private MessageSource messageSource;

    @Override
    public String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return this.messageSource.getMessage(code, null, locale);
    }

}
