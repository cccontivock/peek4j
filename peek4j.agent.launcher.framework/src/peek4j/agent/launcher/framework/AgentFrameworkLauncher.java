package peek4j.agent.launcher.framework;

import static aQute.launcher.constants.LauncherConstants.DEFAULT_LAUNCHER_PROPERTIES;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

import org.osgi.framework.launch.Framework;

import com.fasterxml.jackson.databind.ObjectMapper;

import aQute.launcher.Launcher;
import peek4j.agent.api.AgentArgs;
import peek4j.agent.api.Constants;
import peek4j.agent.api.DefaultAgentArgsImp;

/**
 * Actually launches the OSGi framework in which the Peek4J Agent runs.
 */
@SuppressWarnings("restriction")
public final class AgentFrameworkLauncher {
	public static final CharSequence STARTUP_STDERR_CANARY = AgentFrameworkLauncher.class.getName() + " started.";
	private static final Random RANDOM = new Random();
	@SuppressWarnings("unused")
	private static final String SYS_PROP_KEY__LOG4J_CONFIGURATION = "log4j.configuration";
	private static final String SYS_PROP_KEY__OSGI_ENROUTE_HTTP_PORT = "org.osgi.service.http.port";

	/**
	 * Launches the Peek4J Agent by launching an OSGi container via bnd's launcher,
	 * making the given instrumentation available as an OSGi service with the given
	 * agent arguments in its service properties.
	 *
	 * @param agentArgsStr
	 * @param instrumentation
	 * @see aQute.launcher.Launcher
	 */
	public static void launchAgentFramework(String agentArgsStr, Instrumentation instrumentation) {
		System.out.println(STARTUP_STDERR_CANARY);
		// Do the necessary (for our purposes) parts of Launcher.main() and Launcher.run().
		final Properties properties = new Properties();
		try (InputStreamReader propsReader = new InputStreamReader(
				Launcher.class.getClassLoader().getResourceAsStream(DEFAULT_LAUNCHER_PROPERTIES),
				StandardCharsets.UTF_8)) {
			properties.load(propsReader);
		} catch (final IOException ex) {
			System.err.println("Erred while processing properties file.");
			ex.printStackTrace();
		}
		// Choose a random high port number and set it for use by the HTTP server.
		final int httpPort = RANDOM.ints(1, 50000, 65536).findFirst().getAsInt();
		final String httpPortStr = Integer.toString(httpPort);
		final String httpPortStrOrig = System.setProperty(SYS_PROP_KEY__OSGI_ENROUTE_HTTP_PORT, httpPortStr);
		System.out.println("Peek4J Agent HTTP server will use port " + httpPortStr);
		// TODO: Determine why configuring Log4j causes WebGoat not to start correctly. O_o
		// TODO: Perhaps force usage of Log4j *2* and use https://logging.apache.org/log4j/2.0/manual/logsep.html?
//		final String log4jCfgUrlStr = AgentFrameworkLauncher.class.getResource("/log4j.properties").toString();
//		final String log4jCfgUrlStrOrig = System.setProperty(SYS_PROP_KEY__LOG4J_CONFIGURATION, log4jCfgUrlStr);
//		System.out.println("Peek4J Agent will use log4j configuration " + log4jCfgUrlStr);
		try {
			final Launcher bndFwLauncher = new Launcher(properties, null);
			bndFwLauncher.activate();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					System.out.println("Peek4J Agent deactivating OSGi framework.");
					bndFwLauncher.deactivate();
				} catch (@SuppressWarnings("unused") final Exception ex) {
					// Don't care, because runtime is exiting.
				}
			}));
			final Framework framework = extractFrameworkFrom(bndFwLauncher);
			System.out.println("Peek4J Agent launched OSGi framework.");

			// Deserialize agent arguments from the given string, assuming it's JSON.
			final AgentArgs agentArgs = agentArgsStr == null ? new DefaultAgentArgsImp()
					: new ObjectMapper().readValue(agentArgsStr, DefaultAgentArgsImp.class);
			System.out.println("Peek4J Agent using args: " + agentArgs);

			// Register the instrumentation as an OSGi service, with agentArgs in its service props.
			final Hashtable<String, Object> instrumentationProps = new Hashtable<>();
			instrumentationProps.put(Constants.LAUNCHER_KEY__AGENT_ARGS, agentArgs);
			framework.getBundleContext().registerService(Instrumentation.class.getName(), instrumentation,
					instrumentationProps);
			System.out.println("Peek4J Agent registered Instrumentation instance as a service.");
		} catch (final Exception ex) {
			System.err.println("Erred while launching OSGi framework.");
			ex.printStackTrace();
		} finally {
			if (httpPortStrOrig != null) {
				System.setProperty(SYS_PROP_KEY__OSGI_ENROUTE_HTTP_PORT, httpPortStrOrig);
			}
//			if (log4jCfgUrlStrOrig != null) {
//				System.setProperty(SYS_PROP_KEY__LOG4J_CONFIGURATION, log4jCfgUrlStrOrig);
//			}
		}
	}

	private static Framework extractFrameworkFrom(Launcher launcher)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		final Field field = Launcher.class.getDeclaredField("systemBundle");
		field.setAccessible(true);
		return (Framework) field.get(launcher);
	}
}
