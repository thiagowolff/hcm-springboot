package br.com.litecode.controller;

import br.com.litecode.domain.Chamber;
import br.com.litecode.service.ChamberService;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestChamberManager {

	@Test
	public void testGetChambers() {
		ChamberService mockChamberService = mock(ChamberService.class);
		ChamberManager chamberManager = new ChamberManager();
		chamberManager.setChamberService(mockChamberService);

		List<Chamber> chamberList = Lists.newArrayList(new Chamber());
		when(mockChamberService.getChambers()).thenReturn(chamberList);

		List<Chamber> chambers = chamberManager.getChambers();

		assertThat(chambers, is(notNullValue(List.class)));
		verify(mockChamberService).getChambers();
	}
}
