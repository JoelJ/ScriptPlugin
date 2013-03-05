package com.attask.jenkins;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 8/29/12
 * Time: 5:47 PM
 */
public class FindScriptsOnMaster implements FilePath.FileCallable<Map<String, Script>> {
	private final FilePath userContent;
	private final List<String> fileTypes;

	public FindScriptsOnMaster(FilePath userContent, List<String> fileTypes) {
		this.userContent = userContent;
		this.fileTypes = fileTypes;
	}

	public Map<String, Script> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
		Map<String, Script> result = new HashMap<String, Script>();
		List<File> files = findFiles(f, this.fileTypes);
		for (File file : files) {
			result.put(file.getAbsolutePath(), new Script(new FilePath(userContent, file.getPath())));
		}
		return Collections.unmodifiableMap(result);
	}

	private List<File> findFiles(File directory, final List<String> fileTypes) {
		assert directory != null : "directory cannot be null";
		assert directory.isDirectory() : "directory must be a directory";

		File[] files = directory.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(file.getName().startsWith(".")) {
					return false;
				}
				if (file.isDirectory()) {
					return true;
				}
				String name = file.getName();
				for (String fileType : fileTypes) {
					if (name.endsWith(fileType) || (fileType.equals(".*") && file.canExecute())) {
						return true;
					}
				}
				return false;
			}
		});

		List<File> result = new ArrayList<File>();
		for (File file : files) {
			if(file.isDirectory()) {
				List<File> foundFiles = findFiles(file, fileTypes);
				result.addAll(foundFiles);
			} else {
				result.add(file.getAbsoluteFile());
			}
		}
		Collections.sort(result, new Comparator<File>() {
			public int compare(File file1, File file2) {
				return file1.getPath().compareTo(file2.getPath());
			}
		});
		return result;
	}
}
