package org.vebqa.vebtal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vebqa.vebtal.model.Response;

/**
 * Selenese REST API Service
 *
 */
public class RestServer {

	private static final Logger logger = LoggerFactory.getLogger(RestServer.class);

	private Server apiServer;

	public boolean startServer() {
		RoboManager.writeToArea("Default charset: " + Charset.defaultCharset());

		ResourceConfig config = new ResourceConfig();

		// Plugins (Adapter) laden und ausfuehren
		Iterator<TestAdaptionPlugin> plugins = ServiceLoader.load(TestAdaptionPlugin.class).iterator();
		while (plugins.hasNext()) {
			final TestAdaptionPlugin robo = plugins.next();
			// register old-style adapter
			if ((robo.getType() == TestAdaptionType.ADAPTER) && (robo.getImplementation() != null)) {
				logger.info("register old style: " + robo.getName());
				try {
					config.register(robo.getImplementation());
				} catch (Exception e) {
					logger.error("Error while starting plugin: " + robo.getName(), e);
				} catch (NoSuchMethodError e) {
					logger.error("Error while starting plugin: " + robo.getName(), e);
				}
			} else if ((robo.getType() == TestAdaptionType.ADAPTER) && (robo.getImplementation() == null)) {
				logger.info("register new style: " + robo.getName());
				// register new-style adapter
				final Resource.Builder resourceBuilder = Resource.builder();
		        // path is always the adaptionID
				resourceBuilder.path(robo.getAdaptionID());
		        
				// method is always "post"
		        final ResourceMethod.Builder methodBuilder = resourceBuilder.addMethod("POST");
		        
		        methodBuilder.consumes(MediaType.APPLICATION_JSON).produces(MediaType.APPLICATION_JSON).handledBy(new Inflector<ContainerRequestContext, Response>() {
		        	@Override
		        	public Response apply(ContainerRequestContext containerRequestContext) {
		        		// we will call the specifiy resource, build with the adaptionId
		        		String tClassname = robo.getAdaptionID().toLowerCase().trim();
		        		// erster Buchstabe gross
		        		String tFirst = tClassname.substring(0, 1).toUpperCase(); 
		        		String tRest = tClassname.substring(1);
		        		tClassname = tFirst + tRest;
		        		String tClass = "org.vebqa.vebtal."+robo.getAdaptionID() + "restserver." + tClassname + "Resource";
		        		Response result = null;
		        		
		        		try {
		        			Class<?> cmdClass = Class.forName(tClass);
		        			Constructor<?> cons = cmdClass.getConstructor();
		        			Object cmdObj = cons.newInstance();
		        			
		        			Class[] argTypes = new Class[] { String.class, String.class, String.class };
		        			Method m = cmdClass.getDeclaredMethod("execute", argTypes);
		        			
		        			MultivaluedMap<String, String> pathparam = containerRequestContext.getUriInfo().getPathParameters();
		        			
		        			result = (Response)m.invoke(cmdObj); //, pathparam.getFirst("cmd"), pathparam.getFirst("target"), pathparam.getFirst("value"));
		        			
		        		} catch (ClassNotFoundException e) {
		        			logger.error("Keyword class not found.", e);
		        		} catch (NoSuchMethodException e) {
		        			logger.error("Method not found!", e);
						} catch (SecurityException e) {
							logger.error("Security exception!", e);
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        		
		        		return result;
		        	}
				});
		        
		        final Resource resource = resourceBuilder.build();
		        config.register(resource);
			}
		} // register adaption plugins

		config.register(org.glassfish.jersey.moxy.json.MoxyJsonFeature.class);
		config.register(CharsetResponseFilter.class);

		ServletHolder servlet = new ServletHolder(new ServletContainer(config));

		apiServer = new Server(84);
		ServletContextHandler context = new ServletContextHandler(apiServer, "/*");
		context.addServlet(servlet, "/*");

		try {
			apiServer.start();
			apiServer.join();
		} catch (Exception e) {
			logger.error("Error starting server.", e);
		} finally {
			// apiServer.destroy();
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
}
