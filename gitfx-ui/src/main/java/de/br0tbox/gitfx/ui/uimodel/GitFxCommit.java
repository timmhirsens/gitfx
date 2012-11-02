package de.br0tbox.gitfx.ui.uimodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GitFxCommit {

	private StringProperty hash = new SimpleStringProperty();
	private StringProperty author = new SimpleStringProperty();
	private StringProperty message = new SimpleStringProperty();

	public GitFxCommit(String hash, String author, String message) {
		super();
		this.author.set(author);
		this.message.set(message);
		this.hash.set(hash);
	}

	public String getAuthor() {
		return author.get();
	}

	public void setAuthor(String author) {
		this.author.set(author);
	}

	public String getMessage() {
		return message.get();
	}

	public void setMessage(String message) {
		this.message.set(message);
	}

	public String getHash() {
		return hash.get();
	}

	public void setHash(String hash) {
		this.hash.set(hash);
	}

}
