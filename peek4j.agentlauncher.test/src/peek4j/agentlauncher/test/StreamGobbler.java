package peek4j.agentlauncher.test;

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
	private final String type;
	private final Optional<OutputStream> redirect;

	public StreamGobbler(Logger logger, InputStream is, String type) {
		this(logger, is, type, null);
	}

	public StreamGobbler(Logger logger, InputStream is, String type, OutputStream redirect) {
		this.logger = logger;
		this.is = is;
		this.type = type;
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
				if (logger.isTraceEnabled()) {
					logger.trace("{}>{}", type, lineTmp);
				}
			}
			pw.ifPresent(pwTmp -> pwTmp.flush());
		} catch (final IOException ex) {
			logger.error("Error reading/writing stream.", ex);
		} finally {
			if (logger.isTraceEnabled()) {
				logger.trace("{} for type {} exiting.", getClass(), type);
			}
		}
	}
}
