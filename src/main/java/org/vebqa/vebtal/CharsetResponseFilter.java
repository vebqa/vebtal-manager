package org.vebqa.vebtal;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharsetResponseFilter implements ContainerResponseFilter {

	private static final Logger logger = LoggerFactory.getLogger(CharsetResponseFilter.class);
	
	public CharsetResponseFilter() {
		logger.info("init CharsetResponseFilter");
	}
	
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		
        MediaType contentType = response.getMediaType();
        request.getHeaders().clear();
        request.getHeaders().putSingle("Content-Type", contentType.toString() + ";charset=UTF-8");
        response.getHeaders().clear();
        response.getHeaders().putSingle("Content-Type", contentType.toString() + ";charset=UTF-8");
        logger.info("extend header with utf-8");
	}


}
