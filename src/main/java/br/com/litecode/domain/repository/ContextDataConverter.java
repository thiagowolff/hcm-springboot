package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session.ExecutionMetadata;
import com.google.gson.Gson;

import javax.persistence.AttributeConverter;

public class ContextDataConverter implements AttributeConverter<ExecutionMetadata, String> {
    @Override
    public String convertToDatabaseColumn(ExecutionMetadata executionMetadata) {
        Gson gson = new Gson();
        return gson.toJson(executionMetadata, ExecutionMetadata.class);
    }

    @Override
    public ExecutionMetadata convertToEntityAttribute(String sessionMetadataJson) {
    	if (sessionMetadataJson == null) {
    		return new ExecutionMetadata();
		}

        Gson gson = new Gson();
        return gson.fromJson(sessionMetadataJson, ExecutionMetadata.class);
    }
}