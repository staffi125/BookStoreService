package com.spring.project.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public LoginFailureHandler() {
        setDefaultFailureUrl("/login?error");
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof DisabledException) {
            log.warn("Login rejected: disabled account, remote={}", request.getRemoteAddr());
            getRedirectStrategy().sendRedirect(request, response, "/login?blocked");
            return;
        }
        log.warn("Login failed: {} remote={}", exception.getClass().getSimpleName(), request.getRemoteAddr());
        super.onAuthenticationFailure(request, response, exception);
    }
}
