package com.attask.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 8/29/12
 * Time: 5:23 PM
 */
@ExportedBean
public class ScriptBuilder extends Builder {
	private final String scriptName; //will be an absolute path
	private final List<Parameter> parameters;
	private final boolean abortOnFailure;
	private final ErrorMode errorMode;
	private final String errorRange;
	private final ErrorMode unstableMode;
	private final String unstableRange;
	private final String injectProperties;
	private final boolean runOnMaster;

	@DataBoundConstructor
	public ScriptBuilder(String scriptName, List<Parameter> parameters, boolean abortOnFailure, ErrorMode errorMode, String errorRange, ErrorMode unstableMode, String unstableRange, String injectProperties, boolean runOnMaster) {
		this.scriptName = scriptName;
		if(parameters == null) {
			this.parameters = Collections.emptyList();
		} else {
			this.parameters = Collections.unmodifiableList(new ArrayList<Parameter>(parameters));
		}

		this.abortOnFailure = abortOnFailure;
		this.errorMode = errorMode;
		this.errorRange = errorRange;
		this.unstableMode = unstableMode;
		this.unstableRange = unstableRange;

		this.injectProperties = injectProperties;

		this.runOnMaster = runOnMaster;
	}

	@Exported
	public String getScriptName() {
		return scriptName;
	}

	@Exported
	public List<Parameter> getParameters() {
		return parameters;
	}

	@Exported
	public boolean getAbortOnFailure() {
		return abortOnFailure;
	}

	@Exported
	public ErrorMode getErrorMode() {
		return errorMode;
	}

	@Exported
	public String getErrorRange() {
		return errorRange;
	}

	@Exported
	public ErrorMode getUnstableMode() {
		return unstableMode;
	}

	@Exported
	public String getUnstableRange() {
		return unstableRange;
	}

	@Exported
	public String getInjectProperties() {
		return injectProperties;
	}

	/**
	 * If true the script runs on the master node in a temporary directory rather than on the machine the build is running on.
	 */
	@Exported
	public boolean getRunOnMaster() {
		return runOnMaster;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	public Map<String, Script> findRunnableScripts() throws IOException, InterruptedException {
		FilePath rootPath = Jenkins.getInstance().getRootPath();
		FilePath userContent = new FilePath(rootPath, "userContent");

		DescriptorImpl descriptor = getDescriptor();
		return descriptor.findRunnableScripts(userContent);
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
		boolean result;

		final Map<String, Script> runnableScripts = findRunnableScripts();
		Script script = runnableScripts.get(scriptName);
		if(script != null) {
			if(this.runOnMaster) {
				FilePath workspace = Jenkins.getInstance().getRootPath().createTempDir("Workspace", "Temp");
				try {
					result = execute(build, listener, script, workspace);
				} finally {
					workspace.deleteRecursive();
				}
			} else {
				FilePath workspace = build.getWorkspace();
				result = execute(build, listener, script, workspace);
			}
		} else {
			listener.error("'" + scriptName + "' doesn't exist anymore. Failing.");
			result = false;
		}

		return result;
	}

	private boolean execute(AbstractBuild<?, ?> build, BuildListener listener, Script script, FilePath workspace) throws IOException, InterruptedException {
		EnvVars environment = build.getEnvironment(listener);

		ExecuteScriptCallable fileScriptCallable = new ExecuteScriptCallable(script, getParameters(), environment, workspace.getRemote(), listener);
		final int exitCode = workspace.act(fileScriptCallable);

		Result result = ExitCodeParser.findResult(exitCode, errorMode, errorRange, unstableMode, unstableRange);
		build.setResult(result);
		if(result.isWorseThan(Result.SUCCESS)) {
			listener.error("Exit code " + exitCode + " evaluated to " + result);
		}

		if(injectProperties != null && !injectProperties.isEmpty()) {
			InjectPropertiesCallable injectPropertiesCallable = new InjectPropertiesCallable(workspace.getRemote(), this.injectProperties);
			Map<String, String> injectedPropertiesMap = workspace.act(injectPropertiesCallable);
			build.addAction(new InjectPropertiesAction(injectedPropertiesMap));
		}

		return !(abortOnFailure && result.isWorseOrEqualTo(Result.FAILURE));
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public String fileTypes;

		@Override
		public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
			fileTypes = formData.getString("fileTypes");
			save();
			return super.configure(request, formData);
		}

		public String getFileTypes() {
			load();
			if(fileTypes == null || fileTypes.isEmpty()) {
				return ".*";
			}
			return fileTypes;
		}

		@Exported
		public ListBoxModel doFillScriptNameItems() {
			FilePath rootPath = Jenkins.getInstance().getRootPath();
			FilePath userContent = new FilePath(rootPath, "userContent");

			ListBoxModel items = new ListBoxModel();
			for (Script script : findRunnableScripts(userContent).values()) {
				//Pretty up the name
				String path = script.getFile().getAbsolutePath();
				path = path.substring(userContent.getRemote().length()+1);

				items.add(path, script.getFile().getAbsolutePath());
			}

			return items;
		}

		public String getGuid() {
			return UUID.randomUUID().toString().replaceAll("-", "");
		}

		private Map<String, Script> findRunnableScripts(FilePath userContent) {
			final List<String> fileTypes = Arrays.asList(this.getFileTypes().split("\\s+"));
			try {
				return userContent.act(new FindScriptsOnMaster(fileTypes));
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		public ListBoxModel doFillErrorModeItems() {
			ListBoxModel items = new ListBoxModel();
			for (ErrorMode errorMode : ErrorMode.values()) {
				items.add(errorMode.getHumanReadable(), errorMode.toString());
			}
			return items;
		}

		@Exported
		public ListBoxModel doFillUnstableModeItems() {
			return doFillErrorModeItems();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Execute UserContent Script";
		}
	}
}
