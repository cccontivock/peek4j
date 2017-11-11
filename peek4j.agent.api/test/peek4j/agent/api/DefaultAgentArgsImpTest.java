package peek4j.agent.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import peek4j.agent.api.AgentArgs;
import peek4j.agent.api.DefaultAgentArgsImp;

public class DefaultAgentArgsImpTest {

	private AgentArgs testee;

	@Before
	public void setUp() {
		testee = new DefaultAgentArgsImp();
	}

	@Test
	public void testToCommandLineSuffix() {
		Assert.assertTrue(testee.isEmpty());
		final String keyA = "ALL YOUR";
		final String valA = "BELONG";
		testee.put(keyA, valA);
		final String keyB = "BASE ARE";
		final String valB = "TO US.";
		testee.put(keyB, valB);
		Assert.assertEquals("=" + keyA + "=" + valA + "," + keyB + "=" + valB, testee.toCommandLineSuffix());
	}
}
