package com.attask.jenkins;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: Joel Johnson
 * Date: 1/4/13
 * Time: 7:37 PM
 */
public class ExecuteScriptCallable implements FilePath.FileCallable<Integer> {
	private final Map<String, Script> runnableScripts;
	private final String scriptName;
	private final List<Parameter> parameters;
	private final EnvVars environment;
	private final String workspacePath;
	private final BuildListener listener;

	public ExecuteScriptCallable(Map<String, Script> runnableScripts, String scriptName, List<Parameter> parameters, EnvVars environment, String workspacePath, BuildListener listener) {
		this.runnableScripts = runnableScripts;
		this.scriptName = scriptName;
		this.parameters = parameters;
		this.environment = environment;
		this.workspacePath = workspacePath;
		this.listener = listener;
	}

	public Integer invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
		Script script = runnableScripts.get(scriptName);

		int exitCode = script.execute(Parameter.toStringList(parameters, environment), workspacePath, listener);
		return exitCode;
	}
}
