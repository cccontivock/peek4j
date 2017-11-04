package peek4j.agentlauncher.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import peek4j.agentlauncher.AgentLauncher;

public class WebGoatWithAgentTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebGoatWithAgentTest.class);
	private static final Random RANDOM = new Random();

	/**
	 * Text printed by WebGoat (to stdout) when it starts.
	 */
	private static final String WEBGOAT_STARTUP_CANARY = "WebGoat and happy hacking!";

	@Ignore("Provided by test stub generated via Bndtools, but not needed... yet.")
	@Test
	public void testBundle() throws Exception {
		Assert.assertNotNull(FrameworkUtil.getBundle(this.getClass()).getBundleContext());
	}

	/**
	 * Launches WebGoat in a separate JVM via its "container exec" JAR file, with
	 * {@link AgentLauncher the Peek4J Agent launcher} specified, and asserts that
	 * both WebGoat and that agent start (that is, they both print their start-up
	 * "canaries").
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	@Test(timeout = 30000)
	public void testWebGoatAndAgentStartup() throws IOException, InterruptedException, URISyntaxException {
		final URL wgContainerExecJarUrl = new URI(System.getProperty("webGoatContainerExecJarUri")).toURL();
		final URL agentJarUrl = new URL(
				FrameworkUtil.getBundle(AgentLauncher.class).getLocation().replace("reference:", ""));
		final int httpPort = RANDOM.ints(1, 50000, 65536).findFirst().getAsInt();
		final ProcessBuilder wgPb = new ProcessBuilder("java", "-javaagent:" + agentJarUrl.getFile(), "-jar",
				wgContainerExecJarUrl.getFile(), "-httpPort=" + httpPort);
		wgPb.directory(new File(System.getProperty("java.io.tmpdir")));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Starting WebGoat using command line: {}", StringUtils.join(wgPb.command(), " "));
		}
		final Process wgProcess = wgPb.start();
		try (ByteArrayOutputStream stdoutBaos = new ByteArrayOutputStream(1024 * 1024)) {
			new StreamGobbler(LOGGER, wgProcess.getInputStream(), "stdout", stdoutBaos).start();
			new StreamGobbler(LOGGER, wgProcess.getErrorStream(), "stderr").start();
			while (!stdoutBaos.toString().contains(WEBGOAT_STARTUP_CANARY)) {
				Thread.sleep(500);
			}
			wgProcess.destroyForcibly();
			final int wgExitCode = wgProcess.waitFor();
			Assert.assertEquals(137, wgExitCode);
			final String str = stdoutBaos.toString();
			// Assert P4J agent start-up.
			Assert.assertTrue(StringUtils.isNotBlank(AgentLauncher.STARTUP_STDERR_CANARY));
			Assert.assertTrue(str.contains(AgentLauncher.STARTUP_STDERR_CANARY));
		} finally {
			if (wgProcess.isAlive()) {
				wgProcess.destroyForcibly();
			}
		}
	}
}
