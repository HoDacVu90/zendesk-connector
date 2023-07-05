package com.axonivy.connector.zendesk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.axonivy.connector.zendesk.dto.RequestDTO;
import com.axonivy.connector.zendesk.dto.TicketDTO;
import com.axonivy.connector.zendesk.enums.Priority;
import com.axonivy.connector.zendesk.model.Attachment;
import com.axonivy.connector.zendesk.model.Comment;
import com.axonivy.connector.zendesk.model.Request;
import com.axonivy.connector.zendesk.model.Ticket.Requester;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ivyteam.ivy.environment.Ivy;

public class ZendeskService {
	
	private static Zendesk zendesk;
	
	private static List<String> fileTokens = new ArrayList<>();

	private static ZendeskService instance = new ZendeskService();
	
	private ZendeskService() {};
	
	public static ZendeskService getInstance() {
		createClientWithToken();
		return instance;
	}
	
	public static void createClientWithToken() {
//		String url = Ivy.var().get("com.axonivy.connector.zendesk.auth.url");
//		String username = Ivy.var().get("com.axonivy.connector.zendesk.auth.username");
//		String token = Ivy.var().get("com.axonivy.connector.zendesk.token");
		String url = "https://coffee6767.zendesk.com";
		String username = "hodacvu90@gmail.com";
		String token = "5IfhWroC2G7ZdDiyr0YKQBtQ4fCLODywA7TLlge9";
		
		zendesk = new Zendesk.Builder(url)
				.setUsername(username)
				.setToken(token)
				.build();
	}

	public void closeClient() {
        if (zendesk != null) {
        	zendesk.close();
        }
        zendesk = null;
    }
	
	public Request createTicket(RequestDTO request) {
		Request result = zendesk.createRequest(request);
		return result;
	}
	
	public Attachment.Upload upload(File file) {
		Attachment.Upload result = null;
		try {
			result = zendesk.createUpload(file.getName(), FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			
		}
		return result;
	}
	
	public List<String> uploads(List<File> files) {		
		for (File file: files) {
			Attachment.Upload upload = upload(file);
			fileTokens.add(upload.getToken());
		}
		return fileTokens;
	}
	
//	public Request createTicket(TicketDTO ticket) {
//		uploads(ticket.getFiles());
//		Request request = convertToRequest(ticket);
//		return createTicket(request);
//	}
	
	public Request createTicketByRequest(TicketDTO ticket) {
		uploads(ticket.getFiles());
		RequestDTO request = convertToRequest(ticket);
		return createTicket(request);
	}

	private RequestDTO convertToRequest(TicketDTO ticket) {
		RequestDTO request = new RequestDTO();
		request.setSubject(ticket.getSubject());
		
		Comment comment = new Comment();
		comment.setUploads(fileTokens);
		comment.setBody(ticket.getBody());
		
		request.setComment(comment);
		
		Requester requester = new Requester();
		requester.setName(ticket.getName());
		requester.setEmail(ticket.getEmail());
		request.setRequester(requester);
		
		return request;
	}

}
