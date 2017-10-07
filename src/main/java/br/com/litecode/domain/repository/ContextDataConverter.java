package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session.ContextData;
import com.google.gson.Gson;

import javax.persistence.AttributeConverter;

public class ContextDataConverter implements AttributeConverter<ContextData, String> {
    @Override
    public String convertToDatabaseColumn(ContextData contextData) {
        Gson gson = new Gson();
        return gson.toJson(contextData, ContextData.class);
    }

    @Override
    public ContextData convertToEntityAttribute(String contextDataJson) {
    	if (contextDataJson == null) {
    		return new ContextData();
		}

        Gson gson = new Gson();
        return gson.fromJson(contextDataJson, ContextData.class);
    }
}