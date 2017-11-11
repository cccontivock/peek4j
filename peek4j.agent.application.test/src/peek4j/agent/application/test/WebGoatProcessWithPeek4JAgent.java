package peek4j.agent.application.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import peek4j.agent.api.AgentArgs;
import peek4j.agent.launcher.framework.AgentFrameworkLauncher;

/**
 * Spawns and manages a JVM to run WebGoat with the Peek4J Agent.
 */
final class WebGoatProcessWithPeek4JAgent implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebGoatProcessWithPeek4JAgent.class);
	private static final Random RANDOM = new Random();
	/**
	 * Text printed by WebGoat (to stdout) when it starts.
	 */
	private static final String WEBGOAT_STARTUP_CANARY = "WebGoat and happy hacking!";

	private final int wgHttpPort;
	private final Process wgProcess;
	private final ByteArrayOutputStream stderrBaos = new ByteArrayOutputStream();
	private final ByteArrayOutputStream stdoutBaos = new ByteArrayOutputStream();

	/**
	 * Uses the given Peek4J Agent arguments and a randomly-generated high port
	 * number to spawn a JVM to run WebGoat with the Peek4J Agent.
	 * 
	 * @param agentArgs
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	WebGoatProcessWithPeek4JAgent(AgentArgs agentArgs) throws URISyntaxException, IOException {
		wgHttpPort = RANDOM.ints(1, 50000, 65536).findFirst().getAsInt();
		final ProcessBuilder wgPb = new ProcessBuilder(buildCommand(agentArgs));
		final File workingDirectory = new File(System.getProperty("java.io.tmpdir"));
		wgPb.directory(workingDirectory);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Starting WebGoat in {} using command line: {}", workingDirectory,
					StringUtils.join(wgPb.command(), " "));
		}
		wgProcess = wgPb.start();
		new StreamGobbler(LOGGER, wgProcess.getErrorStream(), "stderr", stderrBaos).start();
		new StreamGobbler(LOGGER, wgProcess.getInputStream(), "stdout", stdoutBaos).start();
	}

	/**
	 * Waits until the WebGoat process has printed the string that indicates that it
	 * has started.
	 *
	 * @throws UnsupportedEncodingException
	 * @throws InterruptedException
	 */
	void waitForWebGoatToStart() throws UnsupportedEncodingException, InterruptedException {
		final Integer portStr = Integer.valueOf(wgHttpPort);
		LOGGER.debug("Waiting for WebGoat to start on port {}", portStr);
		synchronized (stdoutBaos) {
			while (!stdoutBaos.toString(StandardCharsets.UTF_8.name()).contains(WEBGOAT_STARTUP_CANARY)) {
				stdoutBaos.wait();
			}
		}
		LOGGER.debug("WebGoat started on port {}", portStr);
	}

	/**
	 * Builds the command-line for running a JVM with the Peek4J Agent and WebGoat.
	 *
	 * Depends on external settings (that is, system properties, environment
	 * variables) to determine the URLs of the WebGoat JAR file and the Peek4J Agent
	 * Launcher JAR file.
	 *
	 * @param agentArgs
	 * @return the command-line
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	private List<String> buildCommand(AgentArgs agentArgs) throws MalformedURLException, URISyntaxException {
		final URL wgContainerExecJarUrl = new URI(System.getProperty("webGoatContainerExecJarUri")).toURL();
		String p4jAgentJarUriStr = System.getProperty("peek4jAgentLauncherExportedJarUri");
		if (StringUtils.isBlank(p4jAgentJarUriStr)) {
			p4jAgentJarUriStr = System.getenv("peek4jAgentLauncherExportedJarUri");
		}
		final URL agentJarUrl = new URI(p4jAgentJarUriStr).toURL();
		final List<String> command = new ArrayList<>();
		command.add("java");
		command.add("-javaagent:" + agentJarUrl.getFile() + agentArgs.toCommandLineSuffix());
		command.add("-jar");
		command.add(wgContainerExecJarUrl.getFile());
		command.add("-httpPort=" + wgHttpPort);
		command.add("-resetExtract");
		command.add("--debug");
		return command;
	}

	@Override
	public void close() throws Exception {
		if (wgProcess != null && wgProcess.isAlive()) {
			wgProcess.destroyForcibly();
			wgProcess.waitFor();
		}
		final String str = stdoutBaos.toString(StandardCharsets.UTF_8.name());
		// Assert P4J agent start-up.
		Assert.assertTrue(StringUtils.isNotBlank(AgentFrameworkLauncher.STARTUP_STDERR_CANARY));
		Assert.assertTrue(str.contains(AgentFrameworkLauncher.STARTUP_STDERR_CANARY));
	}
}