package de.br0tbox.gitfx.core.model;

public class GitChange {

	private String fileName;
	private ChangeType changeType;

	public GitChange(String fileName, ChangeType changeType) {
		super();
		this.fileName = fileName;
		this.changeType = changeType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ChangeType getChangeType() {
		return changeType;
	}

	public void setChangeType(ChangeType changeType) {
		this.changeType = changeType;
	}

	public static enum ChangeType {
		ADDED, DELETED, MODIFIED;
	}

}
