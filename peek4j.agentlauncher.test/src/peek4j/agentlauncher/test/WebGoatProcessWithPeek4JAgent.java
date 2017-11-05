package peek4j.agentlauncher.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import peek4j.agent.api.AgentArgs;
import peek4j.agentlauncher.AgentLauncher;

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

	WebGoatProcessWithPeek4JAgent(AgentArgs agentArgs) throws URISyntaxException, IOException, InterruptedException {
		wgHttpPort = RANDOM.ints(1, 50000, 65536).findFirst().getAsInt();
		final ProcessBuilder wgPb = new ProcessBuilder(buildCommand(agentArgs));
		wgPb.directory(new File(System.getProperty("java.io.tmpdir")));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Starting WebGoat using command line: {}", StringUtils.join(wgPb.command(), " "));
		}
		wgProcess = wgPb.start();
		new StreamGobbler(LOGGER, wgProcess.getErrorStream(), "stderr", stderrBaos).start();
		new StreamGobbler(LOGGER, wgProcess.getInputStream(), "stdout", stdoutBaos).start();
		synchronized (stdoutBaos) {
			while (!stdoutBaos.toString().contains(WEBGOAT_STARTUP_CANARY)) {
				stdoutBaos.wait();
			}
		}
	}

	private List<String> buildCommand(AgentArgs agentArgs) throws MalformedURLException, URISyntaxException {
		final URL wgContainerExecJarUrl = new URI(System.getProperty("webGoatContainerExecJarUri")).toURL();
		final URL agentJarUrl = new URL(
				FrameworkUtil.getBundle(AgentLauncher.class).getLocation().replace("reference:", ""));
		final List<String> command = new ArrayList<>();
		command.add("java");
		command.add("-javaagent:" + agentJarUrl.getFile() + agentArgs.toCommandLineSuffix());
		command.add("-jar");
		command.add(wgContainerExecJarUrl.getFile());
		command.add("-httpPort=" + wgHttpPort);
		return command;
	}

	@Override
	public void close() throws Exception {
		if (wgProcess.isAlive()) {
			wgProcess.destroyForcibly();
		}
		final String str = stdoutBaos.toString();
		// Assert P4J agent start-up.
		Assert.assertTrue(StringUtils.isNotBlank(AgentLauncher.STARTUP_STDERR_CANARY));
		Assert.assertTrue(str.contains(AgentLauncher.STARTUP_STDERR_CANARY));
	}
}