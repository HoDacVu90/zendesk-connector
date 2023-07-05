package com.axonivy.connector.zendesk.dto;

import com.axonivy.connector.zendesk.model.Comment;
import com.axonivy.connector.zendesk.model.Ticket.Requester;

public class RequestDTO {
	private String subject;
	private Comment comment;
	private Requester requester;
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	public Requester getRequester() {
		return requester;
	}
	public void setRequester(Requester requester) {
		this.requester = requester;
	}
	
}
