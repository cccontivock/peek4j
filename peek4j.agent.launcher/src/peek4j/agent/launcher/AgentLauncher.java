package peek4j.agent.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

/**
 * The primordial Java agent launcher for Peek4J. Except within the scope of the
 * {@link URLClassLoader} that it creates and initializes, this class <b>must
 * not</b> directly refer to any types except those available from the JRE.
 *
 * This class uses an approach borrowed from bnd's embedded launcher to
 * bootstrap a secondary launcher; see
 * {@link peek4j.agent.launcher.framework.AgentFrameworkLauncher}.
 */
public final class AgentLauncher {

	private static final int BUFFER_SIZE = 16 * 1024;
	public static final String EMBEDDED_RUNPATH = "Embedded-Runpath";
	private static byte[] BUFFER = new byte[BUFFER_SIZE];

	public static void premain(String agentArgs, Instrumentation instrumentation) throws Throwable {
		agentmain(agentArgs, instrumentation);
	}

	public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Throwable {
		log("Starting Peek4J Agent.");
		final ClassLoader cl = AgentLauncher.class.getClassLoader();
		final Enumeration<URL> manifests = cl.getResources("META-INF/MANIFEST.MF");
		while (manifests.hasMoreElements()) {

			final Manifest m = new Manifest(manifests.nextElement().openStream());
			final String runpath = m.getMainAttributes().getValue(EMBEDDED_RUNPATH);
			log("Found embedded runpath: " + runpath);
			if (runpath == null || runpath.isEmpty()) {
				continue;
			}

			final List<URL> classpath = new ArrayList<>();
			for (final String path : runpath.split("\\s*,\\s*")) {
				final URL embeddedRunPathJarUrl = cl.getResource(path);
				final URL url = toFileURL(embeddedRunPathJarUrl);
				log("Adding " + embeddedRunPathJarUrl + " to classpath as " + url);
				classpath.add(url);
			}

			AccessController.doPrivileged((PrivilegedAction<Throwable>) () -> {
				try (final URLClassLoader urlc = new URLClassLoader(classpath.toArray(new URL[0]))) {
					log("Getting framework-launcher class.");
					final Class<?> embeddedLauncher = urlc
							.loadClass("peek4j.agent.launcher.framework.AgentFrameworkLauncher");
					log("Getting framework-launcher method.");
					final Method method = embeddedLauncher.getMethod("launchAgentFramework",
							new Class<?>[] { String.class, Instrumentation.class });
					log("Invoking framework-launcher method.");
					method.invoke(null, new Object[] { agentArgs, instrumentation });
					return null;
				} catch (final Exception ex) {
					throw new RuntimeException("Failed to launch Peek4J Agent OSGi framework.", ex);
				}
			});
			log("Started Peek4J Agent.");
		}
	}

	/**
	 * TODO: Replace usage of this method with logging framework calls, once those
	 * can be invoked without impeding the target application.
	 *
	 * NOTE: Not using {@link peek4j.agent.api.Log} here because this needs no
	 * dependencies.
	 *
	 * @param message
	 */
	@SuppressWarnings("javadoc")
	private static void log(String message) {
		System.out.println(AgentLauncher.class.getName() + ": " + message);
	}

	private static URL toFileURL(URL resource) throws IOException {
		final File resourceJarFile = File.createTempFile("resource", ".jar");
		final File tmpFileParentDir = resourceJarFile.getParentFile();
		final boolean madeDirs = tmpFileParentDir.mkdirs();
		if (!madeDirs && !tmpFileParentDir.isDirectory()) {
			throw new IOException("Failed to create temporary directory.");
		}
		try (final InputStream in = resource.openStream();
				final OutputStream out = new FileOutputStream(resourceJarFile)) {
			int size = in.read(BUFFER);
			while (size > 0) {
				out.write(BUFFER, 0, size);
				size = in.read(BUFFER);
			}
		}
		resourceJarFile.deleteOnExit();
		return resourceJarFile.toURI().toURL();
	}
}
