package de.br0tbox.gitfx.ui.message;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.br0tbox.gitfx.ui.message.Message.MessageType;

public class MessageBundle {

	private ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages");
	private static final String TITLE = "Title";
	private static final String MASTHEAD = "Masthead";
	private static final String TEXT = "Text";
	private static final String TYPE = "Type";

	public Message getMessage(String code, String... parameters) {
		final Message message = new Message();
		message.setTitel(getFormattedOutput(TITLE, code, parameters));
		message.setMasthead(getFormattedOutput(MASTHEAD, code, parameters));
		message.setText(getFormattedOutput(TEXT, code, parameters));
		message.setType(getMessageType(code));
		return message;
	}

	private String getFormattedOutput(String category, String code, String... parameters) {
		final MessageFormat format = new MessageFormat(bundle.getString(code + "." + category));
		final String formattedTitle = format.format(parameters);
		return formattedTitle;
	}

	private MessageType getMessageType(String code) {
		final String type = bundle.getString(code + "." + TYPE);
		switch (type) {
		case "ERROR":
			return MessageType.ERROR;
		case "WARNING":
			return MessageType.WARNING;
		case "INFO":
			return MessageType.INFO;
		case "CONFIRM":
			return MessageType.CONFIRMATION;
		}
		return null;
	}

}
