package com.attask.jenkins;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;
import hudson.security.AccessDeniedException2;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.lf5.util.StreamUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletOutputStream;
import java.io.*;

/**
 * User: Joel Johnson
 * Date: 1/10/13
 * Time: 1:14 PM
 */
@Extension
public class ScriptApi implements RootAction {
	public void doFile(StaplerRequest request, StaplerResponse response) throws IOException {
		if(!Jenkins.getAuthentication().isAuthenticated()) {
			throw new AccessDeniedException2(Jenkins.getAuthentication(), Permission.CONFIGURE);
		}
		User current = User.current();
		if(current != null) {
			current.checkPermission(Permission.CONFIGURE);
		}

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

	@RequirePOST
	public void doUpdateFile(StaplerRequest request, StaplerResponse response) throws IOException {
		if(!Jenkins.getAuthentication().isAuthenticated()) {
			throw new AccessDeniedException2(Jenkins.getAuthentication(), Permission.CONFIGURE);
		}
		User current = User.current();
		if(current != null) {
			current.checkPermission(Permission.CONFIGURE);
		}

		String path = request.getParameter("path");
		String content = request.getParameter("content");

		FileUtils.writeStringToFile(new File(path), content);
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
