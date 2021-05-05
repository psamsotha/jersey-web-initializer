
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

/**
 * Jersey uses this hook while initializing the application. This class taps into
 * this and registers the Jersey application as a Servlet Filter instead the
 * default, which is as a Servlet.
 */
public class AsFilterServletContainerProvider implements ServletContainerProvider {

    private static final Logger LOGGER = Logger.getLogger(AsFilterServletContainerProvider.class.getName());

    @Override
    public void preInit(ServletContext context, Set<Class<?>> classes) throws ServletException {
        final Class<? extends Application> applicationCls = getApplicationClass(classes);
        if (applicationCls != null) {
            final ApplicationPath appPath = applicationCls.getAnnotation(ApplicationPath.class);
            final String mapping = createMappingPath(appPath);
            registerFilter(context, applicationCls, classes, mapping);

            // Stop Jersey servlet initializer from registering another servlet
            classes.remove(applicationCls);
        } else {
            LOGGER.warning("No Application class annotated with @ApplicationPath found.");
        }
    }

    private static void registerFilter(ServletContext context, Class<? extends Application> cls,
                                       Set<Class<?>> classes, String mapping) {
        final ResourceConfig resourceConfig = ResourceConfig.forApplicationClass(cls, classes);
        final ServletContainer jerseyFilter = new ServletContainer(resourceConfig);
        final FilterRegistration.Dynamic registration = context.addFilter(cls.getName(), jerseyFilter);
        registration.addMappingForUrlPatterns(null, true, mapping);
        registration.setAsyncSupported(true);
    }

    /**
     * Searches for a single Application class that is used for the Jersey configuration.
     * This can be a JAX-RS {@code javax.ws.rs.core.Application} or a Jersey
     * {@code org.glassfish.jersey.server.ResourceConfig}.
     *
     * @param classes the set of classes discovered by the Servlet container.
     * @return an Application class annotated with {@code @ApplicationPath}, or null of one is not found.
     */
    private static Class<? extends Application> getApplicationClass(Set<Class<?>> classes) {
        for (Class<?> cls : classes) {
            if (Application.class.isAssignableFrom(cls) && cls.isAnnotationPresent(ApplicationPath.class)) {
                return cls.asSubclass(Application.class);
            }
        }
        return null;
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
