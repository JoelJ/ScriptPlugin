package com.attask.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BatchFile;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
			Map<String, String> varsToInject = injectParameters(parameters);
			build.addAction(new InjectPropertiesAction(varsToInject));

			//If we want to run it on master, do so. But if the job is already running on master, just run it as if the run on master flag isn't set.
			if(this.runOnMaster && !(launcher instanceof Launcher.LocalLauncher)) {
				FilePath workspace = Jenkins.getInstance().getRootPath().createTempDir("Workspace", "Temp");
				try {
					Launcher masterLauncher = new Launcher.RemoteLauncher(listener, workspace.getChannel(), true);
					result = execute(build, masterLauncher, listener, script);
				} finally {
					workspace.deleteRecursive();
				}
			} else {
				result = execute(build, launcher, listener, script);
			}
		} else {
			listener.error("'" + scriptName + "' doesn't exist anymore. Failing.");
			result = false;
		}

		injectProperties(build, listener);

		return result;
	}

	private void injectProperties(AbstractBuild<?, ?> build, BuildListener listener) throws IOException {
		if(getInjectProperties() != null && !getInjectProperties().isEmpty()) {
			PrintStream logger = listener.getLogger();
			logger.println("injecting properties from " + getInjectProperties());
			FilePath filePath = new FilePath(build.getWorkspace(), getInjectProperties());
			Properties injectedProperties = new Properties();
			InputStream read = filePath.read();
			try {
				injectedProperties.load(read);
			} finally {
				read.close();
			}
			Map<String, String> injectedMap = new HashMap<String, String>(injectedProperties.size());
			for (Map.Entry<Object, Object> entry : injectedProperties.entrySet()) {
				logger.println("\t" + entry.getKey() + " => " + entry.getValue());
				injectedMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
			}
			build.addAction(new InjectPropertiesAction(injectedMap));
		}
	}

	private Map<String, String> injectParameters(List<Parameter> parameters) {
		Map<String, String> result = new HashMap<String, String>();
		for (Parameter parameter : parameters) {
			String key = parameter.getParameterKey();
			String value = parameter.getParameterValue();
			result.put(key, value);
		}
		return result;
	}

	private boolean execute(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, Script script) throws IOException, InterruptedException {
		String scriptContents = script.findScriptContents();
		if(launcher.isUnix()) {
			Shell shell = new Shell(scriptContents);
			return shell.perform(build, launcher, listener);
		} else {
			BatchFile batchFile = new BatchFile(scriptContents);
			return batchFile.perform(build, launcher, listener);
		}
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
				String path = script.getFile().getRemote();
				path = path.substring(userContent.getRemote().length()+1);

				items.add(path, script.getFile().getRemote());
			}

			return items;
		}

		public String getGuid() {
			return UUID.randomUUID().toString().replaceAll("-", "");
		}

		private Map<String, Script> findRunnableScripts(FilePath userContent) {
			final List<String> fileTypes = Arrays.asList(this.getFileTypes().split("\\s+"));
			try {
				return userContent.act(new FindScriptsOnMaster(userContent, fileTypes));
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
