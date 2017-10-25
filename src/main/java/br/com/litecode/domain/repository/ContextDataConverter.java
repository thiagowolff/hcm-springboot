package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session.SessionMetadata;
import com.google.gson.Gson;

import javax.persistence.AttributeConverter;

public class ContextDataConverter implements AttributeConverter<SessionMetadata, String> {
    @Override
    public String convertToDatabaseColumn(SessionMetadata sessionMetadata) {
        Gson gson = new Gson();
        return gson.toJson(sessionMetadata, SessionMetadata.class);
    }

    @Override
    public SessionMetadata convertToEntityAttribute(String sessionMetadataJson) {
    	if (sessionMetadataJson == null) {
    		return new SessionMetadata();
		}

        Gson gson = new Gson();
        return gson.fromJson(sessionMetadataJson, SessionMetadata.class);
    }
}