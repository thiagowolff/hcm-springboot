package br.com.litecode.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

@Slf4j
public class LogPhaseListener implements PhaseListener {
	private StopWatch stopwatch = new StopWatch();

	public void afterPhase(PhaseEvent event) {
		if (!log.isDebugEnabled()) {
			return;
		}

		if (stopwatch.isRunning()) {
			stopwatch.stop();
		}

        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			System.out.println(stopwatch.prettyPrint());
			stopwatch = new StopWatch();
        }
	}

	public void beforePhase(PhaseEvent event) {
		if (!log.isDebugEnabled()) {
			return;
		}

		if (!stopwatch.isRunning()) {
			stopwatch.start(event.getPhaseId().getName());
		}
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}
}