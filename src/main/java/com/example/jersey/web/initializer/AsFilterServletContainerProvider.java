
package com.example.jersey.web.initializer;

import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.internal.spi.ServletContainerProvider;

public class AsFilterServletContainerProvider implements ServletContainerProvider {

    private static final Logger LOGGER = Logger.getLogger(AsFilterServletContainerProvider.class.getName());

    @Override
    public void preInit(ServletContext context, Set<Class<?>> classes) throws ServletException {
        final Class<? extends Application> applicationCls = getApplicationClass(classes);
        if (applicationCls != null) {
            final ApplicationPath appPath = applicationCls.getAnnotation(ApplicationPath.class);
            if (appPath == null) {
                LOGGER.warning("Application class is not annotated with ApplicationPath");
                return;
            }
            final String mapping = createMappingPath(appPath);
            addFilter(context, applicationCls, classes, mapping);
            // to stop Jersey servlet initializer from trying to register another servlet
            classes.remove(applicationCls);
        }
    }
    
    private static void addFilter(ServletContext context, Class<? extends Application> cls,
                                  Set<Class<?>> classes, String mapping) {
        final ResourceConfig resourceConfig = ResourceConfig.forApplicationClass(cls, classes);
        final ServletContainer filter = new ServletContainer(resourceConfig);
        final FilterRegistration.Dynamic registration = context.addFilter(cls.getName(), filter);
        registration.addMappingForUrlPatterns(null, true, mapping);
        registration.setAsyncSupported(true);
    }

    private static Class<? extends Application> getApplicationClass(Set<Class<?>> classes) {
        Class<? extends Application> applicationCls = null;
        for (Class<?> cls : classes) {
            if (Application.class.isAssignableFrom(cls)) {
                applicationCls = cls.asSubclass(Application.class);
                break;
            }
        }
        if (applicationCls == null) {
            LOGGER.warning("No Application Class Found");
        }
        return applicationCls;
    }

    private static String createMappingPath(final ApplicationPath ap) {
        String path = ap.value();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (!path.endsWith("/*")) {
            if (path.endsWith("/")) {
                path += "*";
            } else {
                path += "/*";
            }
        }

        return path;
    }

    @Override
    public void postInit(ServletContext context, Set<Class<?>> classes, Set<String> names)
            throws ServletException {}

    @Override
    public void onRegister(ServletContext context, Set<String> set) throws ServletException {}

    @Override
    public void configure(ResourceConfig config) throws ServletException {}
}
