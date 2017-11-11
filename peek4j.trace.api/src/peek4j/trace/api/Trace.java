package peek4j.trace.api;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface Trace {
	UUID getId();

	Instant getInstantStarted();

	Optional<Instant> getInstantStopped();

	<T> TypeProfile getTypeProfile(Class<T> typeToProfile);
}
