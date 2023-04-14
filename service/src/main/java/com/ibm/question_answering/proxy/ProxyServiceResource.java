package com.ibm.question_answering.proxy;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@ApplicationScoped
@RegisterProvider(ProxyExceptionMapper.class)
public class ProxyServiceResource {

    @Inject
    @RestClient
    ProxyService proxyService;

    @POST
    @Path("/bam_access")
    public com.ibm.question_answering.maas.Answer ask(Input input) {
        return proxyService.ask(input);
    }
}
