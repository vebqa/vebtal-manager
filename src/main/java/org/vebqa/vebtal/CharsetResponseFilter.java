package org.vebqa.vebtal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharsetResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Logger logger = LoggerFactory.getLogger(CharsetResponseFilter.class);

	public CharsetResponseFilter() {
		logger.info("init CharsetResponseFilter");
	}

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		MediaType contentType = response.getMediaType();
		request.getHeaders().clear();
		request.getHeaders().putSingle("Content-Type", contentType.toString() + ";charset=UTF-8");
		response.getHeaders().clear();
		response.getHeaders().putSingle("Content-Type", contentType.toString() + ";charset=UTF-8");
		logger.info("extend header with utf-8");
	}

	@Override
	public void filter(ContainerRequestContext request) throws IOException {
		logger.info("Entering in Resource : /{} ", request.getUriInfo().getPath());
		logQueryParameters(request);
		
        //log entity stream...
        String entity = readEntityStream(request);
        if(null != entity && entity.trim().length() > 0) {
            logger.info("Entity Stream : {}",entity);
        }

	}

	private void logQueryParameters(ContainerRequestContext requestContext) {
        Iterator<String> iterator = requestContext.getUriInfo().getPathParameters().keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            List<String> obj = requestContext.getUriInfo().getPathParameters().get(name);
            String value = null;
            if(null != obj && obj.size() > 0) {
                value = obj.get(0);
            }
            logger.info("Query Parameter Name: {}, Value :{}", name, value);
        }
    }
	
    private String readEntityStream(ContainerRequestContext requestContext)
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final InputStream inputStream = requestContext.getEntityStream();
        final StringBuilder builder = new StringBuilder();
        try
        {
            ReaderWriter.writeTo(inputStream, outStream);
            byte[] requestEntity = outStream.toByteArray();
            if (requestEntity.length == 0) {
                builder.append("");
            } else {
                builder.append(new String(requestEntity));
            }
            requestContext.setEntityStream(new ByteArrayInputStream(requestEntity) );
        } catch (IOException ex) {
            logger.info("----Exception occurred while reading entity stream :{}",ex.getMessage());
        }
        return builder.toString();
    }
}
