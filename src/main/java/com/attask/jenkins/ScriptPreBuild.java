package com.attask.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 11/29/12
 * Time: 3:51 PM
 */
@ExportedBean
public class ScriptPreBuild extends BuildWrapper {
	private List<ScriptBuilder> scripts;

	@DataBoundConstructor
	public ScriptPreBuild(List<ScriptBuilder> scripts) {
		this.scripts = scripts;
	}

	@Exported
	public List<ScriptBuilder> getScripts() {
		return scripts;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		listener.getLogger().println("Executing pre-build scripts");
		boolean result = true;
		for (ScriptBuilder script : scripts) {
			result = result && script.perform(build, launcher, listener);
		}

		if(!result) {
			throw new FailedScriptException("One or more of the pre-build scripts failed");
		}

		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				return true;
			}
		};
	}

	@Extension(ordinal = 100)
	public static class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public String getDisplayName() {
			return "Execute Pre-Build Scripts";
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}
	}
}
