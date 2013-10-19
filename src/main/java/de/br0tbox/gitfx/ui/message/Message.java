/*
 * Copyright 2013 Timm Hirsens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.br0tbox.gitfx.ui.message;

import java.util.List;

public class Message {

	public Message(String titel, MessageType type, String masthead, String text, List<String> options) {
		this.titel = titel;
		this.type = type;
		this.masthead = masthead;
		this.text = text;
		this.options = options;
	}

	public Message() {
	}

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
