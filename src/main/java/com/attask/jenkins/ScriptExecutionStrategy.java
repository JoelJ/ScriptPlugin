package com.attask.jenkins;

import hudson.Extension;
import hudson.matrix.*;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 1/7/13
 * Time: 12:39 PM
 */
public class ScriptExecutionStrategy extends DefaultMatrixExecutionStrategyImpl {
	private List<ScriptBuilder> scripts;

	@DataBoundConstructor
	public ScriptExecutionStrategy(List<ScriptBuilder> scripts) {
		super(false, false, null, null, new NoopMatrixConfigurationSorter());
		this.scripts = scripts == null ? Collections.<ScriptBuilder>emptyList() : scripts;
	}

	@Override
	public Result run(MatrixBuild build, List<MatrixAggregator> aggregators, BuildListener listener) throws InterruptedException, IOException {
		boolean result = true;
		for (ScriptBuilder script : scripts) {
			result = result && script.perform(build, null, listener);
		}

		if(!result) {
			throw new FailedScriptException("One or more of the pre-build scripts failed");
		}

		return super.run(build, aggregators, listener);
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
