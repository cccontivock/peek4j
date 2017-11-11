package peek4j.tracedatabase.api;

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

import org.osgi.annotation.versioning.ProviderType;

import peek4j.trace.api.Trace;

/**
 * The interface for a database of {@link Trace} instances.
 */
@ProviderType
public interface TraceDatabase {
	Trace getTraceById(UUID id);

	BigInteger getTraceCount();

	Set<UUID> getTraceIdSet();
}
