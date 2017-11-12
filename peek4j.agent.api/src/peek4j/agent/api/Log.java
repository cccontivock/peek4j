package peek4j.agent.api;

import java.io.PrintStream;

/**
 * TODO: Replace usage of this method with logging framework calls, once those
 * can be invoked without impeding the target application.
 */
public final class Log {

	/**
	 * @param cls
	 *            the invoking class
	 * @param message
	 *            to be logged via {@link System#err}
	 */
	public static void logErr(Class<?> cls, String message) {
		log(System.err, cls, message);
	}

	/**
	 * @param cls
	 *            the invoking class
	 * @param message
	 *            to be logged via {@link System#out}
	 */
	public static void logOut(Class<?> cls, String message) {
		log(System.out, cls, message);
	}

	/**
	 * @param printStream
	 *            to which to print
	 * @param cls
	 *            the invoking class
	 * @param message
	 *            to be logged via {@link System#out}
	 */
	private static void log(PrintStream printStream, Class<?> cls, String message) {
		printStream.println(cls.getName() + ": " + message);
	}

	private Log() {
	}
}
