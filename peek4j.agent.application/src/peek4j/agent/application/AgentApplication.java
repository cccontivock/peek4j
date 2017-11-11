package peek4j.agent.application;

import java.lang.instrument.Instrumentation;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osgi.enroute.configurer.api.RequireConfigurerExtender;
import osgi.enroute.rest.api.REST;
import peek4j.agent.api.AgentArgs;
import peek4j.agent.api.Constants;
import peek4j.tracedatabase.api.TraceDatabase;

//@RequireAngularWebResource(resource = { "angular.js", "angular-resource.js", "angular-route.js" }, priority = 1000)
//@RequireBootstrapWebResource(resource = "css/bootstrap.css")
//@RequireWebServerExtender
@RequireConfigurerExtender
@Component(name = "peek4j.agent.application")
public class AgentApplication implements REST {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentApplication.class);

	private AgentArgs agentArgs;
	private Instrumentation instrumentation;
	@Reference
	private TraceDatabase traceDatabase;

	@Activate
	void activate() {
		LOGGER.debug("Activated.");
	}

	@Activate
	void deactivate() {
		LOGGER.debug("Deactivated.");
	}

	@Reference(target = "(" + Constants.LAUNCHER_KEY__AGENT_ARGS + "=*)")
	void args(Object object, Map<String, Object> map) {
		agentArgs = (AgentArgs) map.get(Constants.LAUNCHER_KEY__AGENT_ARGS);
		instrumentation = (Instrumentation) object;
	}
}
