package com.attask.jenkins;

import hudson.FilePath;
import java.io.*;

/**
 * Represents a file hosted on another machine to be executed on the current node.
 * <p/>
 * User: Joel Johnson
 * Date: 8/29/12
 * Time: 5:38 PM
 */
public class Script implements Serializable {
	private final FilePath file;

	/**
	 * Creates a new script
	 *
	 * @param file The path to the file to execute. Usually on the master node.
	 */
	public Script(FilePath file) {
		this.file = file;
	}

	public String findScriptContents() throws IOException {
		return file.readToString();
	}

	public FilePath getFile() {
		return file;
	}
}
