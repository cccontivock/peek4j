package peek4j.agentlauncher;

import java.lang.instrument.Instrumentation;

public final class AgentLauncher {
	public static final CharSequence STARTUP_STDERR_CANARY = AgentLauncher.class.getName() + " started.";

	public static void agentmain(String agentArgs, Instrumentation instrumentation) {
		launchAgent(agentArgs, instrumentation);
	}

	private static void launchAgent(String agentArgs, Instrumentation instrumentation) {
		// TODO: Start OSGi container, making instrumentation available to its bundles?
		System.out.println(STARTUP_STDERR_CANARY);
	}

	public static void premain(String agentArgs, Instrumentation instrumentation) {
		launchAgent(agentArgs, instrumentation);
	}
}
