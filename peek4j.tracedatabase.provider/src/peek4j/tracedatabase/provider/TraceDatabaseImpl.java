package peek4j.tracedatabase.provider;

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import peek4j.trace.api.Trace;
import peek4j.tracedatabase.api.TraceDatabase;

@Designate(ocd = TraceDatabaseImpl.Config.class, factory = true)
@Component(name = "peek4j.tracedatabase.provider")
public class TraceDatabaseImpl implements TraceDatabase {

	private static final Logger LOGGER = LoggerFactory.getLogger(TraceDatabaseImpl.class);

	@ObjectClassDefinition
	@interface Config {
		String name() default "World";
	}

	private String name;

	@Activate
	void activate(Config config) {
		name = config.name();
		LOGGER.debug("Activated '{}'", name);
	}

	@Deactivate
	void deactivate() {
		LOGGER.debug("Deactivated '{}'", name);
	}

	@Override
	public Trace getTraceById(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger getTraceCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<UUID> getTraceIdSet() {
		// TODO Auto-generated method stub
		return null;
	}
}
