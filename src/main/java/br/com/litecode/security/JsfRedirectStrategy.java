package br.com.litecode.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.session.InvalidSessionStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsfRedirectStrategy extends LoginUrlAuthenticationEntryPoint implements InvalidSessionStrategy {
    private static final String FACES_REDIRECT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\"%s\"></redirect></partial-response>";

    public JsfRedirectStrategy(String loginUrl) {
        super(loginUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (isAjaxRedirect(request)) {
            facesRedirect(request, response);
        } else {
            super.commence(request, response, authException);
        }
    }

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAjaxRedirect(request)) {
            facesRedirect(request, response);
        } else {
            response.sendRedirect(request.getRequestURL().toString());
        }
    }

    private boolean isAjaxRedirect(HttpServletRequest request) {
        return "partial/ajax".equals(request.getHeader("faces-request"));
    }

    private void facesRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf(FACES_REDIRECT_XML, request.getContextPath() + getLoginFormUrl());
    }
}