package peek4j.agent.api;

/**
 * Constants related to Peek4J Agent start-up.
 */
public final class Constants {
	/**
	 * The key with which
	 * {@link peek4j.agent.launcher.framework.AgentFrameworkLauncher the Agent
	 * Framework Launcher} stores an instance of {@link AgentArgs} for retrieval
	 * from the service keys of the {@link java.lang.Instrumentation} instance
	 * within the Agent's OSGi framework.
	 */
	@SuppressWarnings("javadoc")
	public static final String LAUNCHER_KEY__AGENT_ARGS = "launcher.peek4j.agentArgs";

	private Constants() {
		// NOP
	}
}
