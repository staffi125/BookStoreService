package com.spring.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

@Controller
public class LocaleSwitchController {

    @GetMapping("/switch-locale")
    public void switchLocale(
            @RequestParam String lang,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Locale locale = resolveLocale(lang);
        LocaleResolver resolver = RequestContextUtils.getLocaleResolver(request);
        if (resolver != null) {
            resolver.setLocale(request, response, locale);
        }
        String target = sanitizeReferer(request);
        response.sendRedirect(response.encodeRedirectURL(target));
    }

    private static Locale resolveLocale(String lang) {
        if (lang != null && lang.equalsIgnoreCase("uk")) {
            return Locale.forLanguageTag("uk");
        }
        return Locale.forLanguageTag("en");
    }

    private static String sanitizeReferer(HttpServletRequest request) {
        String ctx = request.getContextPath() == null ? "" : request.getContextPath();
        String fallback = (ctx.isEmpty() ? "" : ctx) + "/";
        String ref = request.getHeader("Referer");
        if (ref == null || ref.isBlank()) {
            return fallback;
        }
        try {
            URI uri = URI.create(ref);
            String host = uri.getHost();
            if (host != null && host.equalsIgnoreCase(request.getServerName())) {
                return ref;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return fallback;
    }
}
