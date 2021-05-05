
Register Jersey as a Servlet Filter with no web.xml
===================================================

See Stackoverflow http://stackoverflow.com/q/40042871/2587435

To run:

1. Clone this project
2. From the root of the project `mvn jetty:run`. (wait for "Jetty Server Started")
3. Go to `http://localhost:8080/hello`
3. Go to `http://localhost:8080/index.jsp`

Normally the JSP should be not be available because Jersey is configured with
the mapping `/*`. But with the `ServletContainerProvider` implementation,
we register Jersey as a filter, and disable the registration of the servlet.
We then set the Jersey property to forward on 404, so the index.jsp is now
accessible.
