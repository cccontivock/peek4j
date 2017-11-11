package peek4j.agent.api;

import java.util.SortedMap;

/**
 * A map of arguments to be used by the Peek4J Agent.
 */
public interface AgentArgs extends SortedMap<String, String> {

	/**
	 * @return a string suitable for direct concatenation with the agent JAR file
	 *         name in a {@code -javaagent:} command-line argument to a JVM; will be
	 *         either {@link String#isEmpty()} or will have '=' as its first
	 *         character
	 */
	default String toCommandLineSuffix() {
		if (isEmpty()) {
			return "";
		}

		final StringBuilder sb = new StringBuilder().append('=');
		final int baseLength = sb.length();
		entrySet().stream().sequential().forEach(entry -> {
			if (sb.length() > baseLength) {
				sb.append(',');
			}
			sb.append(entry.getKey().trim()).append('=').append(entry.getValue());
		});
		return sb.toString();
	}
}
