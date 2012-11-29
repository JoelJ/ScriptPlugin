package com.attask.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 11/29/12
 * Time: 2:26 PM
 */
@ExportedBean
public class ScriptPostBuild extends Recorder {
	private List<ScriptBuilder> scripts;

	@DataBoundConstructor
	public ScriptPostBuild(List<ScriptBuilder> scripts) {
		this.scripts = scripts;
	}

	@Exported
	public List<ScriptBuilder> getScripts() {
		return scripts;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		boolean result = true;
		for (ScriptBuilder script : scripts) {
			result = result && script.perform(build, launcher, listener);
		}
		return result;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Extension
	public static class ResultsOverrideRecorderDescriptor extends BuildStepDescriptor<Publisher> {
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Execute Post-Build scripts";
		}
	}
}
