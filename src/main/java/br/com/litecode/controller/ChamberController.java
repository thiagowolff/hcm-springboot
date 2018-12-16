package br.com.litecode.controller;

import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.ChamberEvent;
import br.com.litecode.domain.repository.ChamberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;

@ViewScoped
@Component
@CacheConfig(cacheNames = "chamber")
public class ChamberController implements Serializable {
	@Autowired
	private ChamberRepository chamberRepository;

	private Iterable<Chamber> chambers;
	private Chamber chamber;

	public ChamberController() {
		chamber = new Chamber();
	}

	@Cacheable(key = "#root.methodName")
	public Iterable<Chamber> getChambers() {
		if (chambers == null) {
			chambers = chamberRepository.findAll();
		}
		return chambers;
	}

	@Cacheable(key = "#chamberId")
	public Map<ChamberEvent, Pair<Integer, String>> getChamberEventsChartData(Integer chamberId) {
		Chamber chamber = chamberRepository.findOne(chamberId);
        Map<ChamberEvent, Pair<Integer, String>> events = new LinkedHashMap<>();

        int i = 0;
        List<ChamberEvent> chamberEvents = chamber.getChamberEvents();
        int totalTimeout = chamberEvents.get(chamberEvents.size() - 1).getTimeout();
        for (ChamberEvent chamberEvent : chamberEvents) {
            if (chamberEvent.getTimeout() == 0) {
                i++;
                continue;
            }

            ChamberEvent previousChamberEvent = chamber.getChamberEvents().get(i - 1);
            int duration = chamberEvent.getTimeout() - previousChamberEvent.getTimeout();
            int percentage = Math.round((float) (duration) / totalTimeout * 100);

            String label = LocalTime.MIDNIGHT.plus(Duration.of(duration, SECONDS)).format(DateTimeFormatter.ofPattern("mm:ss"));
            events.put(previousChamberEvent, Pair.of(percentage, label));
            i++;
        }

        return events;
	}

	public Chamber getChamber() {
		return chamber;
	}

	public void setChamber(Chamber chamber) {
		this.chamber = chamber;
	}
}
