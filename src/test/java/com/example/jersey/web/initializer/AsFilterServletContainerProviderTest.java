package com.example.jersey.web.initializer;

import java.lang.reflect.Field;
import java.util.Random;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jersey.repackaged.com.google.common.collect.Sets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AsFilterServletContainerProviderTest {

    @Mock
    private ServletContext context;

    @Mock
    private FilterRegistration.Dynamic registration;

    @Captor
    private ArgumentCaptor<ServletContainer> filterCaptor;

    @Captor
    private ArgumentCaptor<String> filterNameCaptor;

    private AsFilterServletContainerProvider provider;

    @Before
    public void setUp() {
        provider = new AsFilterServletContainerProvider();
        when(context.addFilter(anyString(), any(Filter.class)))
                .thenReturn(registration);
    }

    @Test
    public void testJerseyFilterRegistered() throws ServletException  {
        assertFilterRegisteredUsingClasses(Random.class, TestConfigGood.class);
    }

    @Test
    public void testJerseyFilterRegisteredWithMultipleApplications() throws ServletException {
        assertFilterRegisteredUsingClasses(TestConfigNoAnnotation.class, TestConfigGood.class);
    }

    private void assertFilterRegisteredUsingClasses(Class<?>... classes) throws ServletException {
        provider.preInit(context, Sets.newHashSet(classes));
        verify(context).addFilter(filterNameCaptor.capture(), filterCaptor.capture());

        ServletContainer jerseyFilter = filterCaptor.getValue();
        ResourceConfig config = getResourceConfig(jerseyFilter);
        assertThat(config).isNotNull();

        String filterName = filterNameCaptor.getValue();
        assertThat(filterName).isEqualTo(TestConfigGood.class.getName());
    }

    @Test
    public void testNoClassRegistered() throws ServletException {
        provider.preInit(context, Sets.newHashSet(TestConfigNoAnnotation.class, Random.class));
        verify(context, never()).addFilter(anyString(), any(Filter.class));
    }

    @Test
    public void testCorrectUrlMapping() throws ServletException {
        provider.preInit(context, Sets.newHashSet(TestConfigGood.class, Random.class));
        verify(registration).addMappingForUrlPatterns(null, true, "/api/*");
    }

    /* ServletContainer#getConfiguration() doesn't return our actual instance */
    private static ResourceConfig getResourceConfig(ServletContainer container) {
        ResourceConfig config = null;
        try {
            Field rcField = container.getClass().getDeclaredField("resourceConfig");
            rcField.setAccessible(true);
            Object obj = rcField.get(container);
            if (obj instanceof ResourceConfig) {
                config = (ResourceConfig) obj;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return config;
    }


    @ApplicationPath("/api")
    private static class TestConfigGood extends ResourceConfig {}

    private static class TestConfigNoAnnotation extends ResourceConfig {}
}
