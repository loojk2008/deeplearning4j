package org.arbiter.optimize.report.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import io.dropwizard.views.View;

@Path("/arbiter-ui")
@Produces(MediaType.TEXT_HTML)
public class TestResource2 {

    @GET
    public View get() {
        return new TestView();
    }

}
