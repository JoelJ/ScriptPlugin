package com.attask.jenkins;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: Joel Johnson
 * Date: 1/4/13
 * Time: 7:38 PM
 */
public class InjectPropertiesCallable implements FilePath.FileCallable<Map<String, String>> {
	private final String workspacePath;
	private final String injectProperties;

	public InjectPropertiesCallable(String workspacePath, String injectProperties) {
		this.workspacePath = workspacePath;
		this.injectProperties = injectProperties;
	}

	public Map<String, String> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
		File workspaceDirectory = new File(workspacePath);
		File injectPropertiesFile = new File(workspaceDirectory, injectProperties);
		Properties properties = new Properties();
		FileInputStream fileStream = new FileInputStream(injectPropertiesFile);
		try {
			properties.load(fileStream);
		} finally {
			fileStream.close();
		}

		Map<String, String> result = new HashMap<String, String>(properties.size());
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			result.put((String) entry.getKey(), (String) entry.getValue());
		}
		return result;
	}
}
