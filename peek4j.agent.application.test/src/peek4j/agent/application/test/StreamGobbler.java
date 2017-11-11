package peek4j.agent.application.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;

/**
 * @see <a href=
 *      "https://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2">When
 *      Runtime.exec() won't</a>
 */
@SuppressWarnings("javadoc")
public final class StreamGobbler extends Thread {
	private final Logger logger;
	private final InputStream is;
	private final String tag;
	private final Optional<OutputStream> redirect;

	public StreamGobbler(Logger logger, InputStream is, String type) {
		this(logger, is, type, null);
	}

	/**
	 * @param logger
	 *            to which to log messages
	 * @param is
	 *            from which to read lines
	 * @param tag
	 *            to prepend to each read line when printed to the logger
	 * @param redirect
	 *            to which to print lines, unmodified, and on which to call
	 *            {@link #notifyAll()} safely after printing each line
	 */
	public StreamGobbler(Logger logger, InputStream is, String tag, OutputStream redirect) {
		this.logger = logger;
		this.is = is;
		this.tag = tag;
		this.redirect = Optional.ofNullable(redirect);
	}

	@Override
	public void run() {
		final Optional<PrintWriter> pw = redirect.map(os -> new PrintWriter(os));
		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8),
					1024 * 1024);
			String line = null;
			while ((line = br.readLine()) != null) {
				final String lineTmp = line;
				pw.ifPresent(pwTmp -> {
					pwTmp.println(lineTmp);
					pwTmp.flush();
				});
				redirect.ifPresent(os -> {
					synchronized (os) {
						os.notifyAll();
					}
				});
				if (logger.isTraceEnabled()) {
					logger.trace("{}>{}", tag, lineTmp);
				}
			}
			pw.ifPresent(pwTmp -> pwTmp.flush());
		} catch (final IOException ex) {
			logger.error("Error reading/writing stream.", ex);
		} finally {
			if (logger.isTraceEnabled()) {
				logger.trace("{} for type {} exiting.", getClass(), tag);
			}
		}
	}
}
