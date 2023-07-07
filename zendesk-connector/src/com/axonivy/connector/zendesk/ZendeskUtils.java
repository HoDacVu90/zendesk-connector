package com.axonivy.connector.zendesk;

import com.axonivy.connector.zendesk.connector.rest.Upload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ZendeskUtils {
	public static Upload convertToUpload(String object) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		
		Upload upload = new Upload();
		upload.setToken(mapper.readTree(object).get("upload").get("token").asText());
		return upload;
	}
}
