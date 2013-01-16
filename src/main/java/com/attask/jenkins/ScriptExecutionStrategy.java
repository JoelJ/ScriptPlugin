package com.attask.jenkins;

import hudson.Extension;
import hudson.matrix.*;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Joel Johnson
 * Date: 1/7/13
 * Time: 12:39 PM
 */
public class ScriptExecutionStrategy extends DefaultMatrixExecutionStrategyImpl {
	private static final Logger logger = Logger.getLogger("ScriptPlugin");
	private List<ScriptBuilder> scripts;

	@DataBoundConstructor
	public ScriptExecutionStrategy(List<ScriptBuilder> scripts) {
		super(false, false, null, null, new NoopMatrixConfigurationSorter());
		this.scripts = scripts == null ? Collections.<ScriptBuilder>emptyList() : scripts;
	}

	@Override
	public Result run(MatrixBuild.MatrixBuildExecution execution) throws InterruptedException, IOException {
		BuildListener listener = execution.getListener();
		Run build = execution.getBuild();
		if(!(build instanceof AbstractBuild)) {
			listener.error("Couldn't execute scripts because the build wasn't an instance of AbstractBuild.");
			return Result.FAILURE;
		}

		logger.info(build.getFullDisplayName() + " running " + scripts.size() + " scripts before Matrix");

		boolean result = true;
		for (ScriptBuilder script : scripts) {
			result = result && script.perform((AbstractBuild)build, null, listener);
		}

		if(!result) {
			throw new FailedScriptException("One or more of the pre-build scripts failed");
		}

		logger.info(build.getFullDisplayName() + " done running " + scripts.size() + " scripts. Moving on to the matrix jobs.");
		return super.run(execution);
	}

	public List<ScriptBuilder> getScripts() {
		return scripts;
	}

	public void setScripts(List<ScriptBuilder> scripts) {
		this.scripts = scripts;
	}

	@Extension
	public static class DescriptorImpl extends MatrixExecutionStrategyDescriptor {
		@Override
		public String getDisplayName() {
			return "Script Execution Strategy";
		}
	}
}
