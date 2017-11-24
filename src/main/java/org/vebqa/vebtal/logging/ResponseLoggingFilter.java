package org.vebqa.vebtal.logging;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Logged
@Provider
public class ResponseLoggingFilter implements ContainerResponseFilter {

	private static final Logger logger = LoggerFactory.getLogger(ResponseLoggingFilter.class);
	
	public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
		logger.info("Response status: " + resp.getStatus());
	}
}
