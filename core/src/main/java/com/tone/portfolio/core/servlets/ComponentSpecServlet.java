package com.tone.portfolio.core.servlets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tone.portfolio.core.models.ComponentSpec;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GET /content/dam/portfolio/components.specs.json
 * Lists every component fragment in the folder with references resolved.
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.resourceTypes=sling:Folder",
        "sling.servlet.resourceTypes=sling:OrderedFolder",
        "sling.servlet.selectors=specs",
        "sling.servlet.extensions=json",
        "sling.servlet.methods=GET"
})
public class ComponentSpecServlet extends SlingSafeMethodsServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws IOException {
        List<ComponentSpec> specs = new ArrayList<>();
        for (Resource child : request.getResource().getChildren()) {
            ComponentSpec spec = child.adaptTo(ComponentSpec.class);
            if (spec != null && spec.isValid()) {
                specs.add(spec);
            }
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(response.getWriter(), specs);
    }
}
