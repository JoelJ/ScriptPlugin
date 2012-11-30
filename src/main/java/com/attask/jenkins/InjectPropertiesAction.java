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

	public InjectPropertiesAction(File file) throws IOException {
		FileInputStream fileStream = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileStream);

		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			result.put((String)entry.getKey(), (String)entry.getValue());
		}
		this.injectedVariables = result;
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
