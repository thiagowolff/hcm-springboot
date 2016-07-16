package br.com.litecode.controller;

import br.com.litecode.domain.Address;
import br.com.litecode.domain.Address.State;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Named
@RequestScoped
public class AddressManager {
	private static final String CEPWS_URL = "http://www.cepfacil.com.br/service/?filiacao=71CBB8EF-B55D-46AD-BEDA-0899296E1696&formato=json&cep=%s";

	public void loadAddressByZipCode(Address address) {
		if (Strings.isNullOrEmpty(address.getZipCode())) {
			return;
		}

		Client client = ClientBuilder.newClient();
		String url = String.format(CEPWS_URL, address.getZipCode());

		try {
			Invocation.Builder builder = client.target(url).request(MediaType.APPLICATION_JSON);
			String jsonResponse = builder.get(String.class);
			JsonObject jsonObject = parseJsonResponse(jsonResponse);

			if (jsonObject != null) {
				populateAddress(address, jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JsonObject parseJsonResponse(String jsonResponse) {
		JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
		JsonObject jsonObject = jsonReader.readObject();
		jsonReader.close();
		return jsonObject;
	}

	private void populateAddress(Address address, JsonObject jsonObject) {
		String street = capitalizeFirst(jsonObject.getString("LogradouroTipo") + " " + jsonObject.getString("Logradouro"));
		String neighborhood = capitalizeFirst(jsonObject.getString("Bairro"));
		String city = capitalizeFirst(jsonObject.getString("Cidade"));
		String state = jsonObject.getString("UF");

		address.setAddressStreet(street);
		address.setNeighborhood(neighborhood);
		address.setCity(city);
		address.setState(State.valueOf(state));
	}

	private String capitalizeFirst(String word) {
		Function<String, String> transformFunction = input -> Strings.isNullOrEmpty(input) ? null : (Character.toUpperCase(input.charAt(0)) + input.substring(1).toLowerCase());
		return Joiner.on(" ").skipNulls().join(Iterables.transform(Splitter.on(" ").split(word), transformFunction));
	}

	public List<String> getStates() {
		return Arrays.asList(State.values()).stream().map(Enum::toString).collect(Collectors.toList());
	}
}
