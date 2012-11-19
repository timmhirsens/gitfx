package de.br0tbox.gitfx.ui.message;

import java.util.List;

public class Message {

	private String titel;
	private MessageType type;
	private String masthead;
	private String text;
	private List<String> options;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public String getMasthead() {
		return masthead;
	}

	public void setMasthead(String masthead) {
		this.masthead = masthead;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public enum MessageType {
		INFO, CONFIRMATION, ERROR, WARNING
	}

}
