package peek4j.agentlauncher.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

import peek4j.agent.api.AgentArgs;
import peek4j.agentlauncher.AgentLauncher;
import peek4j.agentlauncher.DefaultAgentArgsImp;

public class WebGoatWithAgentTest {

	private static final AgentArgs EMPTY = new DefaultAgentArgsImp();

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
	 * @throws Exception
	 */
	@Test(timeout = 30000)
	public void testWebGoatAndAgentStartup() throws Exception {
		try (WebGoatProcessWithPeek4JAgent wgProcess = new WebGoatProcessWithPeek4JAgent(EMPTY)) {
			// Getting here is success!
		}
	}
}
