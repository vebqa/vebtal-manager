package org.vebqa.vebtal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vebqa.vebtal.logging.RequestLoggingFilter;
import org.vebqa.vebtal.model.Command;
import org.vebqa.vebtal.model.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Selenese REST API Service
 *
 */
public class RestServer {

	private static final Logger logger = LoggerFactory.getLogger(RestServer.class);

	private static final String LOCALHOST = "127.0.0.1";
	
	private int port = 84;
	
	private Server apiServer;

	public void setPort(int aPort) {
		this.port = aPort;
	}
	
	public boolean startServer() {
		GuiManager.getinstance().writeLog("Default charset: " + Charset.defaultCharset());
		
		ResourceConfig config = new ResourceConfig();

		// Plugins (Adapter) laden und ausfuehren
		Iterator<TestAdaptionPlugin> plugins = ServiceLoader.load(TestAdaptionPlugin.class).iterator();
		while (plugins.hasNext()) {
			final TestAdaptionPlugin robo = plugins.next();
			// register old-style adapter
			if (robo.getType() == TestAdaptionType.ADAPTER && robo.getImplementation() != null) {
				logger.info("register old style: " + robo.getName());
				try {
					config.register(robo.getImplementation());
				} catch (Exception e) {
					logger.error("Error while starting plugin: " + robo.getName(), e);
				} catch (NoSuchMethodError e) {
					logger.error("Error while starting plugin: " + robo.getName(), e);
				}
			} else if (robo.getType() == TestAdaptionType.ADAPTER && robo.getImplementation() == null) {
				logger.info("register new style: " + robo.getName());

				// register new-style adapter
				final Resource.Builder resourceBuilder = Resource.builder();
				resourceBuilder.path(robo.getAdaptionID());

				resourceBuilder.addChildResource("execute").addMethod("POST").produces(MediaType.APPLICATION_JSON)
						.consumes(MediaType.APPLICATION_JSON)
						.handledBy(new Inflector<ContainerRequestContext, Response>() {

							@Override
							public Response apply(ContainerRequestContext request) {

								logger.info("Media Type: " + request.getMediaType().toString());
								String entity = getEntityBody(request);
								logger.info("Entity: " + entity);
								
								// convert entity data to command object model
								ObjectMapper mapper = new ObjectMapper();
								Command cmd = null;
								try {
									cmd = mapper.readValue(entity, Command.class);
								} catch (JsonParseException e) {
									logger.error("Error while parsing entity stream to object!", e);
								} catch (JsonMappingException e) {
									logger.error("Error while parsing entity stream to object!", e);
								} catch (IOException e) {
									logger.error("Error while parsing entity stream to object!", e);
								}
								
								// we will call the specifiy resource, build with the adaptionId String
								String tClassname = robo.getAdaptionID().toLowerCase().trim(); // erster Buchstabe
								String tFirst = tClassname.substring(0, 1).toUpperCase();
								String tRest = tClassname.substring(1);
								tClassname = tFirst + tRest;
								
								String tAdapterRoot = GuiManager.getinstance().getConfig().getString("adapter." + robo.getAdaptionID() + ".root");
								String tAdapterClass = tAdapterRoot + "." + tClassname + "Resource";
								
								Class<?> cmdClass = null;
								try {
									cmdClass = Class.forName(tAdapterClass);
								} catch (ClassNotFoundException e1) {
									// default class not found, use legacy
									tAdapterClass = "org.vebqa.vebtal." + robo.getAdaptionID() + "restserver." + tClassname + "Resource";
									try {
										cmdClass = Class.forName(tAdapterClass);
									} catch (ClassNotFoundException e2) {
										logger.error("Adapter class not found: " + tAdapterClass);
									}
								}
								
								logger.info("call handler class: " + tAdapterClass);
								Response result = null;
								try {
									TestAdaptionResource cmdObj = (TestAdaptionResource)cmdClass.newInstance();

									Class[] argTypes = new Class[] { Command.class };
									Method m = cmdClass.getDeclaredMethod("execute", argTypes);

									result = (Response) m.invoke(cmdObj, cmd);

								} catch (NoSuchMethodException e) {
									logger.error("Method not found!", e);
								} catch (SecurityException e) {
									logger.error("Security exception!", e);
								} catch (InstantiationException e) {
									logger.error("Instantiation error!", e);
								} catch (IllegalAccessException e) {
									logger.error("IllegalAccessException!", e);
								} catch (IllegalArgumentException e) {
									logger.error("IllegalArgumentException!", e);
								} catch (InvocationTargetException e) {
									logger.error("There is an exception in the invoked object: ", e.getCause());
								} catch (NullPointerException e) {
									logger.error("I am struggling of nullpointer!", e);
								}

								return result;
							}
						});

				final Resource resource = resourceBuilder.build();
				config.registerResources(resource);
			}
		} // register adaption plugins

		config.register(org.glassfish.jersey.moxy.json.MoxyJsonFeature.class);
		config.register(RequestLoggingFilter.class);
		config.register(CharsetResponseFilter.class);

		ServletHolder servlet = new ServletHolder(new ServletContainer(config));

		apiServer = new Server();
		ServerConnector connector = new ServerConnector(apiServer);
		connector.setPort(this.port);
		connector.setHost(GuiManager.getinstance().getConfig().getString("server.host", LOCALHOST));
		apiServer.addConnector(connector);
		
		ServletContextHandler context = new ServletContextHandler(apiServer, "/*");
		context.addServlet(servlet, "/*");
		context.setErrorHandler(new ErrorHandler());

		try {
			apiServer.start();
			apiServer.join();
		} catch (Exception e) {
			logger.error("Error starting server.", e);
		}

		return true;
	}

	public boolean restartServer() {
		if (!apiServer.isRunning()) {
			logger.info("RestServer not running!");
		}
		try {
			logger.info("Stopping rest server...");
			apiServer.stop();
			startServer();
			logger.info("Rest server started.");
		} catch (Exception e) {
			logger.error("Error while restarting server.", e);
		}

		return true;
	}

	public boolean shutdownServer() {
		if (apiServer == null) {
			logger.info("No rest server available!");
			return true;
		}
		if (apiServer.isStopped()) {
			logger.info("rest server already stopped.");
			return true;
		}
		try {
			apiServer.stop();
		} catch (Exception e) {
			logger.error("Error while stopping server.", e);
		} finally {
			apiServer.destroy();
		}
		logger.info("rest server shutdown successfully.");
		return true;
	}

	public boolean isStarted() {
		return apiServer.isStarted();
	}

	private String getEntityBody(ContainerRequestContext requestContext)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = requestContext.getEntityStream();
         
        final StringBuilder b = new StringBuilder();
        try
        {
            ReaderWriter.writeTo(in, out);
 
            byte[] requestEntity = out.toByteArray();
            if (requestEntity.length == 0)
            {
                b.append("").append("\n");
            }
            else
            {
                b.append(new String(requestEntity)).append("\n");
            }
            requestContext.setEntityStream( new ByteArrayInputStream(requestEntity) );
 
        } catch (IOException e) {
            logger.error("IOException while reading entity from stream!", e);
        }
        return b.toString();
    }	
	
	static class ErrorHandler extends ErrorPageErrorHandler {
		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
				throws IOException {
			logger.error("HTTP ERROR: " + String.valueOf(response.getStatus()));

			Response error = new Response();
			error.setCode(String.valueOf(response.getStatus()));
			error.setMessage("Error while processing reequest!");
			JsonObject jsonError = Json.createObjectBuilder().add("code", String.valueOf(response.getStatus()))
					.add("content", "Error while processing request!").build();
			response.getWriter().append(jsonError.toString());
		}
	}
	
}
