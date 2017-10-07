package br.com.litecode.web;

import br.com.litecode.service.MailService;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.ViewExpiredException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter
@Slf4j
public class ExceptionReportFilter implements Filter {
	@Autowired
	private MailService mailService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			log.error("Unexpected error: {} ", e.getMessage());

			if (e.getCause().getClass() == ViewExpiredException.class) {
				request.getRequestDispatcher("/login.xhtml").forward(request, response);
			}

			request.setAttribute("javax.servlet.error.status_code", 500);
			request.setAttribute("javax.servlet.error.exception_type", e);
			request.setAttribute("javax.servlet.error.message", e.getMessage());
			request.getRequestDispatcher("/errorPage.xhtml").forward(request, response);

			mailService.sendEmail("thiago.wolff@gmail.com", "HCM - Exception", Throwables.getStackTraceAsString(e));
		}
	}

	@Override
	public void destroy() {
	}
}
