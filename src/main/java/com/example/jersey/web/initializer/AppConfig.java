
package com.example.jersey.web.initializer;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;

@ApplicationPath("/*")
public class AppConfig extends ResourceConfig {
    
    public AppConfig() {
        register(HelloWorld.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }
    
    @Path("hello")
    public static class HelloWorld {
        @GET
        public String get() {
            return "Hello ServletContainerProvider";
        }
    }
}
