package com.attask.jenkins;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;

import java.io.IOException;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 1/24/13
 * Time: 8:48 AM
 */
public class ScriptMatrixAggregator extends MatrixAggregator {
	private final boolean runBefore;
	private final boolean runAfter;
	private final List<ScriptBuilder> scripts;
	private static final boolean CONTINUE_ON_FAILURE = false;
	private static final boolean FAIL_ON_FIRST_FAILURE = true;

	public ScriptMatrixAggregator(MatrixBuild build, Launcher launcher, BuildListener listener, boolean runBefore, boolean runAfter, List<ScriptBuilder> scripts) {
		super(build, launcher, listener);

		this.runBefore = runBefore;
		this.runAfter = runAfter;
		this.scripts = scripts;
	}

	@Override
	public boolean startBuild() throws InterruptedException, IOException {
		if(!runBefore) {
			return true;
		}

		listener.getLogger().println("Executing Pre-Matrix scripts");
		return execute(FAIL_ON_FIRST_FAILURE);
	}

	@Override
	public boolean endBuild() throws InterruptedException, IOException {
		if(!runAfter) {
			return true;
		}

		listener.getLogger().println("Executing Post-Matrix scripts");
		return execute(CONTINUE_ON_FAILURE);
	}

	private boolean execute(boolean failOnFirstFailure) throws IOException, InterruptedException {
		boolean result = true;
		for (ScriptBuilder script : scripts) {
			result = result && script.perform(build, launcher, listener);
			if(!result && failOnFirstFailure) {
				return false;
			}
		}
		return true;
	}
}
