package com.ibm.question_answering.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/documents")
public class DocumentResource {

    @Inject
    DocumentService documentService;

    @POST
    public Response index(Document document) throws IOException {
        if (document.url == null) {
            document.url = UUID.randomUUID().toString();
        }
        documentService.index(document);
        return Response.created(URI.create("/" + DocumentService.INDEX + "/" + document.url)).build();
    }

    @GET
    @Path("/{id}")
    public Document get(String id) throws IOException {
        return documentService.get(id);
    }

    @GET
    @Path("/search")
    public List<Document> search(@RestQuery String name, @RestQuery String color) throws IOException {
        if (name != null) {
            return documentService.searchByName(name);
        } else if (color != null) {
            return documentService.searchByColor(color);
        } else {
            // TODO
            return null;
        }
    }
}