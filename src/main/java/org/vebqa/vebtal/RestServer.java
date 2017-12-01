package org.vebqa.vebtal;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			TestAdaptionPlugin robo = plugins.next();
			if (robo.getType() == TestAdaptionType.ADAPTER) {
				try {
					config.register(robo.getImplementation());
				} catch (Exception e) {
					logger.error("Error while starting plugin: " + robo.getName(), e);
				}
			}
		}

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
