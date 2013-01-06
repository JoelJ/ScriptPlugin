package com.attask.jenkins;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import org.apache.log4j.lf5.util.StreamUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a file hosted on another machine to be executed on the current node.
 * <p/>
 * User: Joel Johnson
 * Date: 8/29/12
 * Time: 5:38 PM
 */
public class Script implements Serializable {
	private final File file;
	private final FilePath remoteRootPath;

	/**
	 * Creates a new script
	 *
	 * @param remoteRootPath The root path on the remote machine where the file exists. Typically this is <code>node.getRootPath()</code> or <code>Jenkins.getInstance().getRootPath()</code>.
	 * @param file           The file on the remote machine. Should be an absolute path or relative to the base path of the first argument.
	 */
	public Script(FilePath remoteRootPath, File file) {
		this.remoteRootPath = remoteRootPath;
		this.file = file;
	}

	/**
	 * @return The root of the remote machine.
	 */
	public FilePath getRemoteRootPath() {
		return remoteRootPath;
	}

	/**
	 * @return The file on the remote machine
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Copies the script to the current machine, marks it as executable, and executes it.
	 *
	 * @param parameters The arguments to be passed into the script when executed. Each entry in the list will be treated as only one argument, whether or not it has whitespace in it.
	 * @param listener   The listener for the build. Used for logging.
	 * @return The exit code of the script.
	 * @throws IOException          Thrown if there's an exception raised when attempting to execute the script.
	 * @throws InterruptedException Thrown when the file is being copied <em>or</em> when the script is being executed if an interruption is caused (such as a build being canceled).
	 */
	public int execute(List<String> parameters, Map<String, String> environment, String workspacePath, BuildListener listener) throws IOException, InterruptedException {
		File localFile = copyFile(listener);
		try {
			List<String> cmdBuilder = new ArrayList<String>(parameters.size() + 1);
			cmdBuilder.add(localFile.getAbsolutePath());
			cmdBuilder.addAll(parameters);

			File workspaceDirectory = new File(workspacePath);

			ProcessBuilder processBuilder = new ProcessBuilder(cmdBuilder);
			processBuilder.directory(workspaceDirectory);
			processBuilder.environment().putAll(environment);
			processBuilder.redirectErrorStream(true);

			//Flatten the args, so it's easy to debug the script by just copying and pasting exactly what is being run
			StringBuilder flatArgsBuilder = new StringBuilder();
			for (String parameter : parameters) {
				flatArgsBuilder.append('"').append(parameter).append('"').append(' ');
			}
			String flatArgs = flatArgsBuilder.toString().trim().replace("\"", "\\\"");
			String filePrettyName = file.getAbsolutePath().substring(remoteRootPath.getRemote().length() + 1);

			PrintStream logger = listener.getLogger();
			logger.println("Executing `" + filePrettyName + " " + flatArgs + "`");
			long startTime = System.currentTimeMillis();

			Process exec = processBuilder.start();

			logger.println("Output: ");
			InputStream input = exec.getInputStream();
			dumpInputStreamToLogger(logger, input);

			int errorCode = exec.waitFor();

			if (errorCode != 0) {
				listener.error(filePrettyName + " exited with status " + errorCode);
			}

			long runTime = System.currentTimeMillis() - startTime;
			logger.println("Runtime: " + (runTime / 1000) + " seconds");

			return errorCode;
		} finally {
			deleteFile(localFile, listener);
		}
	}

	private void dumpInputStreamToLogger(PrintStream logger, InputStream input) throws IOException {
		byte[] buf = new byte[512];
		int bytesRead = input.read(buf);
		while (bytesRead != -1) {
			logger.write(buf, 0, bytesRead);
			logger.flush();
			bytesRead = input.read(buf);
		}
	}

	/**
	 * Attempts to delete the given file, logging an error to the given BuildListener if anything goes wrong.
	 *
	 * @param tempFile File to delete
	 * @param listener Where to log to if an error occurs.
	 */
	private void deleteFile(File tempFile, BuildListener listener) {
		if (!tempFile.delete()) {
			listener.error("Failed to delete: " + tempFile.getAbsolutePath());
		}
	}

	/**
	 * Copies the remote file to the local temporary directory and marks it as executable.
	 * If anything goes wrong, the file is deleted.
	 *
	 * @param listener Used for logging.
	 * @return The path to the file on the local machine.
	 * @throws IOException          Thrown if anything goes wrong with copying the file or creating the file locally.
	 * @throws InterruptedException Thrown if the thread is interrupted while the file is copying.
	 */
	private File copyFile(BuildListener listener) throws IOException, InterruptedException {
		byte[] script = remoteRootPath.act(new FilePath.FileCallable<byte[]>() {
			public byte[] invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
				FileInputStream fileInputStream = new FileInputStream(file);
				try {
					byte[] bytes = StreamUtils.getBytes(fileInputStream);
					return bytes;
				} finally {
					fileInputStream.close();
				}
			}
		});
		String name = file.getName();
		String prefix;
		String suffix = "";
		int i = name.indexOf(".");
		if (i >= 0) {
			prefix = name.substring(0, i);
			suffix = name.substring(i);
		} else {
			prefix = name;
		}
		if (prefix.length() <= 3) {
			prefix = "tmp" + prefix;
		}

		File tempFile = File.createTempFile(prefix, suffix);
		try {
			if (!tempFile.setExecutable(true)) {
				listener.error("Unable to set " + tempFile.getAbsolutePath() + " as executable. Attempting to proceed.");
			}
			FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
			try {
				fileOutputStream.write(script);
				fileOutputStream.flush();
			} finally {
				fileOutputStream.close();
			}

			return tempFile;
		} catch (IOException e) {
			deleteFile(tempFile, listener);
			throw e;
		}
	}
}
