package com.attask.jenkins;

import hudson.Extension;
import hudson.model.RootAction;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.lf5.util.StreamUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * User: Joel Johnson
 * Date: 1/10/13
 * Time: 1:14 PM
 */
@Extension
public class ScriptApi implements RootAction {
	public void doFile(StaplerRequest request, StaplerResponse response) throws IOException {
		String path = request.getParameter("path");
		ServletOutputStream outputStream = response.getOutputStream();
		FileInputStream input = new FileInputStream(new File(path));
		try {
			StreamUtils.copy(input, outputStream);
		} finally {
			input.close();
		}
		outputStream.flush();
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "scriptApi";
	}
}
