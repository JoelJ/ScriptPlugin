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
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 1/24/13
 * Time: 8:46 AM
 */
public class ScriptPreMatrixBuild extends ScriptPreBuild implements MatrixAggregatable {
	@DataBoundConstructor
	public ScriptPreMatrixBuild(List<ScriptBuilder> scripts) {
		super(scripts);
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				return true;
			}
		};
	}

	public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
		return new ScriptMatrixAggregator(build, launcher, listener, true, false, getScripts());
	}

	@Extension
	public static class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public String getDisplayName() {
			return "Execute Pre-Matrix Scripts";
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return item instanceof MatrixProject;
		}
	}
}
