package com.axonivy.connector.zendesk.dto;

import java.util.List;

import com.axonivy.connector.zendesk.enums.Priority;
import com.axonivy.connector.zendesk.enums.Type;
import com.axonivy.connector.zendesk.model.Comment;
import com.axonivy.connector.zendesk.model.CustomFieldValue;
import com.axonivy.connector.zendesk.model.Ticket.Requester;

public class TicketDTO {
	private String subject;
	private Comment comment;
	private Requester requester;

    protected Priority priority;
    protected List<CustomFieldValue> customFields;
    protected Type type;
	
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
