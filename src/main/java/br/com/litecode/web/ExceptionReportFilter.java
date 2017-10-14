package br.com.litecode.web;

import br.com.litecode.service.MailService;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.ProjectStage;
import javax.faces.application.ViewExpiredException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.time.Instant;

@Slf4j
@WebFilter(asyncSupported = true)
public class ExceptionReportFilter implements Filter {
	private static int MAX_EMAILS_PER_MINUTE = 10;

	@Autowired
	private MailService mailService;

	private ProjectStage projectStage;
	private int numberOfEmailsSent;
	private Instant lastEmailsSent;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		projectStage = ProjectStage.valueOf(filterConfig.getServletContext().getInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME));
		numberOfEmailsSent = 0;
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

			if (numberOfEmailsSent < MAX_EMAILS_PER_MINUTE && projectStage == ProjectStage.Production) {
				mailService.sendEmail("thiago.wolff@gmail.com", "HCM - Exception", Throwables.getStackTraceAsString(e));
				numberOfEmailsSent++;

				if (lastEmailsSent != null && Instant.now().minusSeconds(60).isAfter(lastEmailsSent)) {
					numberOfEmailsSent = 0;
				}

				lastEmailsSent = Instant.now();
			}
		}
	}

	@Override
	public void destroy() {
	}
}
