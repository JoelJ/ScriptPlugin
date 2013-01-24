package com.attask.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
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
public class ScriptPostMatrixBuild extends ScriptPostBuild implements MatrixAggregatable {
	@DataBoundConstructor
	public ScriptPostMatrixBuild(List<ScriptBuilder> scripts) {
		super(scripts);
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		return true;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
		return new ScriptMatrixAggregator(build, launcher, listener, false, true, getScripts());
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return MatrixProject.class.isAssignableFrom(jobType);
		}

		@Override
		public String getDisplayName() {
			return "Execute Post-Matrix scripts";
		}
	}
}
