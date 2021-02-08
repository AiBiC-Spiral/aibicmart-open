package cloud.aibic_spiral.aibicmart.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * /apiにapiをはやす
 * 
 * @author t-kanda
 *
 */
@ApplicationPath("api")
public class MarketApplication extends ResourceConfig {
	public MarketApplication() {
		packages(this.getClass().getPackage().getName(), "io.swagger.v3.jaxrs2.integration.resources");
	}
}
