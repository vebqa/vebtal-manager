package org.vebqa.vebtal.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Logged
@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

	public RequestLoggingFilter() {
		logger.info("init RequestLogginFilter");
	}
	
	public void filter(ContainerRequestContext req) throws IOException {
		logger.info("{} -> {}", req.getMethod(), req.getMediaType());
		Map<String, String> headers = prepareParameters(req.getHeaders());
		Set<String> keys = headers.keySet();
		for (String aKey : keys) {
			logger.info("{} -> {}", aKey, headers.get(aKey));
		}
	}

	private Map<String, String> prepareParameters(MultivaluedMap<String, String> queryParameters) {

		Map<String, String> parameters = new HashMap<String, String>();

		Iterator<String> it = queryParameters.keySet().iterator();

		while (it.hasNext()) {
			String theKey = (String) it.next();
			parameters.put(theKey, queryParameters.getFirst(theKey));
		}

		return parameters;

	}
}
