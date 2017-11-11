package peek4j.trace.provider;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import peek4j.trace.api.Trace;
import peek4j.trace.api.TypeProfile;

public class TraceImpl implements Trace {

	private UUID id = UUID.randomUUID();
	private Instant instantStarted = null;
	private Optional<Instant> instantStopped = Optional.empty();

	@Override
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public Instant getInstantStarted() {
		return instantStarted;
	}

	public void setInstantStarted(Instant instantStarted) {
		this.instantStarted = instantStarted;
	}

	@Override
	public Optional<Instant> getInstantStopped() {
		return instantStopped;
	}

	public void setInstantStopped(Instant instantStopped) {
		this.instantStopped = Optional.ofNullable(instantStopped);
	}

	@Override
	public <T> TypeProfile getTypeProfile(Class<T> typeToProfile) {
		// TODO: Implement.
		throw new NoSuchMethodError("Unimplemented.");
	}
}
