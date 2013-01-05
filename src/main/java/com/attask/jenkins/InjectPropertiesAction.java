package com.attask.jenkins;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: Joel Johnson
 * Date: 11/29/12
 * Time: 4:48 PM
 */
@ExportedBean
public class InjectPropertiesAction implements EnvironmentContributingAction {
	private final Map<String, String> injectedVariables;

	public InjectPropertiesAction(Map<String, String> injectedVariables) throws IOException {
		this.injectedVariables = injectedVariables;
	}

	@Exported
	public Map<String, String> getInjectedVariables() {
		return injectedVariables;
	}

	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
		env.putAll(this.injectedVariables);
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return null;
	}
}
