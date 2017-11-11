package peek4j.agent.application.test;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import peek4j.agent.api.AgentArgs;
import peek4j.agent.api.DefaultAgentArgsImp;
import peek4j.tracedatabase.api.TraceDatabase;

public class WebGoatWithAgentTest {

	private static final AgentArgs EMPTY = new DefaultAgentArgsImp();

	private BundleContext context;

	@Before
	public void setUp() {
		context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	}

	@Test
	public void testBundle() throws Exception {
		Assert.assertNotNull(context);
	}

	/**
	 * Launches WebGoat in a separate JVM via its "container exec" JAR file, with
	 * {@link peek4j.agent.launcher.AgentLauncher the Peek4J Agent launcher}
	 * specified, and asserts that both WebGoat and that agent start (that is, they
	 * both print their start-up "canaries").
	 *
	 * @throws Exception
	 */
	@Test(timeout = 60000)
	public void testWebGoatAndAgentStartup() throws Exception {
		try (WebGoatProcessWithPeek4JAgent wgWithP4jAgent = new WebGoatProcessWithPeek4JAgent(EMPTY)) {
			wgWithP4jAgent.waitForWebGoatToStart();
			// Getting here is success!
		}
	}

	@Ignore("Disabled until simpler test passes.")
	@Test(timeout = 30000)
	public void testWebGoatAndAgentStartupPlusOSGiFw() throws Exception {
		ServiceTracker<TraceDatabase, TraceDatabase> serviceTracker = null;
		try (WebGoatProcessWithPeek4JAgent wgWithP4jAgent = new WebGoatProcessWithPeek4JAgent(EMPTY)) {
			wgWithP4jAgent.waitForWebGoatToStart();
			final CountDownLatch traceDatabaseAddedLatch = new CountDownLatch(1);
			serviceTracker = new ServiceTracker<TraceDatabase, TraceDatabase>(context, TraceDatabase.class, null) {
				@Override
				public TraceDatabase addingService(ServiceReference<TraceDatabase> reference) {
					// Tell the test thread that the desired service has been added.
					traceDatabaseAddedLatch.countDown();
					return super.addingService(reference);
				}
			};
			// Wait for a (remote) TraceDatabase service to be added.
			traceDatabaseAddedLatch.await();
		} finally {
			if (serviceTracker != null) {
				serviceTracker.close();
				serviceTracker = null;
			}
		}
	}

	@Ignore("Could be a final-ish integration test, but is too far away currently.")
	@Test(timeout = 60000)
	public void testWebGoatLoginPageWithAgent() {
		/*
		 * - Start WG with P4J agent.
		 *   - P4J agent starts an OSGi framework instance, waiting (XXX?) for these local services to appear:
		 *     - Instrumentor, which instruments the appropriate types (e.g., String)
		 *       - using multiple TracerProviders, each of which provides a Tracer
		 *         - each of which provides code to extract information from the handling of single request,
		 *           and makes the information available to the *local* TraceParameterizer
		 *     - TraceDatabase, which records/caches parameters by Trace (DTO), and avails them
		 * - Local OSGi framework instance creates services:
		 *   - TraceParameterClient (TODO: rename?), which consumes data from TraceParameterizer, and checks them
		 *     as part of this test.
		 */
		Assert.fail("Unimplemented.");
	}
}
