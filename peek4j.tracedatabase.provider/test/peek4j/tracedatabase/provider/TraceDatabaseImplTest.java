package peek4j.tracedatabase.provider;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TraceDatabaseImplTest {

	@Test
	public void simple() {
		final TraceDatabaseImpl impl = new TraceDatabaseImpl();
		assertNotNull(impl);
	}
}
