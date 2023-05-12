package com.ibm.question_answering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestStreamElementType;
import com.ibm.question_answering.api.Answer;
import io.smallrye.mutiny.Multi;

@ApplicationScoped
@Path("")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "apikey", 
        type = SecuritySchemeType.HTTP,
        scheme = "Basic")}
)
@OpenAPIDefinition(info = @Info(title = "Question Answering Service", version = "0.0.1", description = "Question Answering APIs"))
public class AnswerResource {

    @Inject
    Sse sse; 
    
    @Inject
    QueryReRankerMaaS queryReRankerMaaS;

    @Inject
    AnswerResourceUtilities utilities;

    @Inject
    QueryDiscoveryMaaS queryDiscoveryMaaS;

    @Inject
    Metrics metrics;

    @Inject
    QueryPrimeAndMaaS queryPrimeAndMaaS;

    @Inject
    QueryDiscoveryReRankerMaaS queryDiscoveryReRankerMaaS;

    @Inject
    QueryElasticMaaS queryElasticMaaS;

    @Inject
    QueryMaaS queryMaaS;

    @Inject
    QueryDiscovery queryDiscovery;

    @Inject
    QueryReranker queryReranker;

    @Inject
    QueryPrimeQA queryPrimeQA;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query")
    @SecurityRequirement(name = "apikey")
    @APIResponses(value = {
        @APIResponse(
            responseCode = "401",
            description = "Not authorized"
        ),
        @APIResponse(
            responseCode = "200",
            description = "Answer successfully returned",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Answer.class)
            )
        )
    })
    @Operation(
            summary = "Reads documents from Discovery, re-ranks results and uses MaaS to return answer",
            description = "Reads documents from Discovery, re-ranks results and uses MaaS to return answer"
    )
    public Answer query(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, 
        @Parameter(description = "query", 
            required = true,
            example = "{\"query\": \"text:When and for how much did IBM acquire Red Hat?\"}") Data data) {
        
        //System.out.println(uriInfo.getRequestUri().toString());
        //System.out.println(correctAPIKey);
        //System.out.println("apikey: " + getAPIKey(apikey));
        //System.out.println("query: " + getQuery(data));

        metrics.start(uriInfo, utilities.getQuery(data));
        utilities.checkAuthorization(apikey);
        Answer output;
        output = queryDiscoveryReRankerMaaS.query(utilities.getQuery(data));
        output = utilities.removeRedundantDocuments(output);
        metrics.end();
        return output; 
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-discovery-reranker-maas")
    @SecurityRequirement(name = "apikey")
    @Operation(
            summary = "Reads documents from Discovery, re-ranks results and uses MaaS to return answer",
            description = "Reads documents from Discovery, re-ranks results and uses MaaS to return answer"
    )
    public Answer queryDiscoveryAndReRankerAndMaaS(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        metrics.start(uriInfo, utilities.getQuery(data));
        utilities.checkAuthorization(apikey);
        Answer output;
        output = queryDiscoveryReRankerMaaS.query(utilities.getQuery(data));
        output = utilities.removeRedundantDocuments(output);
        metrics.end();
        return output;       
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-reranker-maas")
    @SecurityRequirement(name = "apikey")
    @Operation(
        summary = "Returns answer from ReRanker and MaaS",
        description = "Returns answer from ReRanker and MaaS"
    )
    public Answer queryReRankerAndMaaS(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        metrics.start(uriInfo, utilities.getQuery(data));
        utilities.checkAuthorization(apikey);
        Answer output;
        output = queryReRankerMaaS.query(utilities.getQuery(data));
        output = utilities.removeRedundantDocuments(output);
        metrics.end();
        return output;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-discovery-maas")
    @SecurityRequirement(name = "apikey")
    @Operation(
        summary = "Returns answer from Discovery and MaaS",
        description = "Returns answer from Discovery and MaaS"
    )
    public Answer queryDiscoveryAndMaaS(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        metrics.start(uriInfo, utilities.getQuery(data));
        utilities.checkAuthorization(apikey);
        Answer output;
        output = queryDiscoveryMaaS.query(utilities.getQuery(data));
        output = utilities.removeRedundantDocuments(output);
        metrics.end();
        return output;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-elastic-maas")
    @SecurityRequirement(name = "apikey")
    @Operation(
        summary = "Returns answer from ElasticSearch and MaaS",
        description = "Returns answer from ElasticSearch and MaaS"
    )
    public Answer queryElasticAndMaaS(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        metrics.start(uriInfo, utilities.getQuery(data));
        utilities.checkAuthorization(apikey);
        Answer output;
        output = queryElasticMaaS.query(utilities.getQuery(data));
        //TODO output = utilities.removeRedundantDocuments(output);
        //TODOmetrics.end();
        return output;
    }

    @POST
    @Path("/query-discovery-maas-as-stream")
    @SecurityRequirement(name = "apikey")
    @RestStreamElementType(MediaType.APPLICATION_JSON)    
    @Operation(
        summary = "Returns answer from Discovery and MaaS as stream",
        description = "Returns answer from Discovery and MaaS as stream"
    )
    public Multi<OutboundSseEvent> queryDiscoveryAndMaaSAsStream(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        metrics.start(uriInfo, utilities.getQuery(data));
        utilities.checkAuthorization(apikey);
        
        return queryDiscoveryMaaS.queryAsStream(utilities.getQuery(data))
            .onFailure().recoverWithItem(t -> {
                // TODO figure out how to throw an exception
                com.ibm.question_answering.maas.Answer answerWithFailure = new com.ibm.question_answering.maas.Answer();
                com.ibm.question_answering.maas.Result result = new com.ibm.question_answering.maas.Result();
                result.generated_text = "Error";
                com.ibm.question_answering.maas.Result[] results = new com.ibm.question_answering.maas.Result[1];
                results[0] = result;
                answerWithFailure.results = results;
                return answerWithFailure;                                
            })
            .map(item -> sse.newEventBuilder() 
                .data(item) 
                .build());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-primeqa-maas")
    @SecurityRequirement(name = "apikey")
    @Operation(
        summary = "Returns answer from PrimeQA (connected to Discovery) and MaaS",
        description = "Returns answer from PrimeQA (connected to Discovery) and MaaS"
    )
    public Answer queryPrimeAndMaaSProxy(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        utilities.checkAuthorization(apikey);
        Answer output;
        output = queryPrimeAndMaaS.query(utilities.getQuery(data));
        output = utilities.removeRedundantDocuments(output);
        return output;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-maas")
    @SecurityRequirement(name = "apikey")
    @Operation(
            summary = "Get an answer from MaaS",
            description = "Get an answer from MaaS"
    )
    public Answer queryMaaS(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        utilities.checkAuthorization(apikey);
        return queryMaaS.query(utilities.getQuery(data));
    }

    @POST
    @Path("/query-maas-as-stream")
    @SecurityRequirement(name = "apikey")
    @RestStreamElementType(MediaType.APPLICATION_JSON)    
    @Operation(
            summary = "Get an answer from MaaS as stream",
            description = "Get an answer from MaaS as stream"
    )
    public Multi<OutboundSseEvent> queryMaaSAsStream(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        utilities.checkAuthorization(apikey);   
        // TODO figure out how to throw an exception     
        return queryMaaS.queryAsStream(utilities.getQuery(data))
            .map(item -> sse.newEventBuilder() 
                .data(item) 
                .build());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-discovery")
    @SecurityRequirement(name = "apikey")
    @Operation(
            summary = "Get an answer from Watson Discovery",
            description = "Get an answer from Watson Discovery"
    )
    public Answer queryDiscovery(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        utilities.checkAuthorization(apikey);
        metrics.start(uriInfo, utilities.getQuery(data));
        Answer output = queryDiscovery.query(utilities.getQuery(data));
        metrics.end();
        return output;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-reranker")
    @SecurityRequirement(name = "apikey")
    @Operation(
            summary = "Returns answer from the reranker on PrimeQA with mock documents",
            description = "Returns answer from the reranker on PrimeQA mock documents"
    )
    public Answer queryReranker(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        utilities.checkAuthorization(apikey);
        Answer output = queryReranker.query(utilities.getQuery(data));
        return output;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-primeqa")
    @SecurityRequirement(name = "apikey")
    @Operation(
            summary = "Get an answer from PrimeQA",
            description = "Get an answer from PrimeQA"
    )
    public Answer queryPrimeQA(@Context UriInfo uriInfo, @RestHeader("Authorization") String apikey, Data data) {
        utilities.checkAuthorization(apikey);
        Answer output = queryPrimeQA.query(utilities.getQuery(data)); 
        return output;  
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-mock-confident")
    @SecurityRequirement(name = "apikey")
    @Operation(
            summary = "Returns hardcoded data for a confident answer",
            description = "Returns hardcoded data for a confident answer"
    )
    public Answer queryConfident(@RestHeader("Authorization") String apikey, Data data) {
        utilities.checkAuthorization(apikey);

        return MockAnswers.getConfidentAnswer();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query-mock-not-confident")
    @SecurityRequirement(name = "apikey")
    @Operation(
            summary = "Returns hardcoded data for a non confident answer",
            description = "Returns hardcoded data for a non confident answer"
    )
    public Answer queryNotConfident(@RestHeader("Authorization") String apikey, Data data) {
        utilities.checkAuthorization(apikey);

        return MockAnswers.getNotConfidentAnswer();
    }
}