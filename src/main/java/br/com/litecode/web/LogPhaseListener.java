package br.com.litecode.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

@Slf4j
public class LogPhaseListener implements PhaseListener {
	private StopWatch stopwatch = new StopWatch();

	public void afterPhase(PhaseEvent event) {
		stopwatch.stop();
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			if (log.isDebugEnabled()) {
				System.out.println(stopwatch.prettyPrint());
			}
			stopwatch = new StopWatch();
        }

		log.debug("Executed Phase " + event.getPhaseId());
	}

	public void beforePhase(PhaseEvent event) {
		stopwatch.start(event.getPhaseId().getName());
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}
}