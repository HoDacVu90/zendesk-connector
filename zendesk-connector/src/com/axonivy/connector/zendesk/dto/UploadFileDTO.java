package com.axonivy.connector.zendesk.dto;

public class UploadFileDTO {
	private String fileName;
	private byte[] content;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
}
