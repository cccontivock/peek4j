package peek4j.trace.provider;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import peek4j.trace.provider.TraceImpl;

/*
 * Example JUNit test case
 *
 */

public class ProviderImplTest {

	/*
	 * Example test method
	 */

	@Test
	public void simple() {
		TraceImpl impl = new TraceImpl();
		assertNotNull(impl);
	}

}
