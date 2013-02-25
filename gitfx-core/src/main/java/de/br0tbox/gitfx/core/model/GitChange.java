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
