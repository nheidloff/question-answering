package com.ibm.question_answering.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class DocumentService {

    public static final String INDEX = "products";

    @Inject
    RestClient restClient; 

    public void index(Document document) throws IOException {
        Request request = new Request(
                "PUT",
                "/" + INDEX + "/_doc/" + document.url); 
        request.setJsonEntity(JsonObject.mapFrom(document).toString()); 
        restClient.performRequest(request); 
    }

    public Document get(String id) throws IOException {
        Request request = new Request(
                "GET",
                "/" + INDEX + "_doc/" + id);
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject json = new JsonObject(responseBody); 
        return json.getJsonObject("_source").mapTo(Document.class);
    }

    public List<Document> searchByColor(String color) throws IOException {
        return search("color", color);
    }

    public List<Document> searchByName(String name) throws IOException {
        return search("name", name);
    }

    private List<Document> search(String term, String match) throws IOException {
        Request request = new Request(
                "GET",
                "/" + INDEX + "/_search");        
        JsonObject termJson = new JsonObject().put(term, match);
        JsonObject matchJson = new JsonObject().put("match", termJson);
        JsonObject matchAllJson = new JsonObject().put("match_all", "{}");
        JsonObject queryJson = new JsonObject().put("query", matchJson);
        JsonObject queryAllJson = new JsonObject().put("query", matchAllJson);
        request.setJsonEntity(queryAllJson.encode());
        //request.setJsonEntity(queryJson.encode());
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        JsonObject json = new JsonObject(responseBody);
        JsonArray hits = json.getJsonObject("hits").getJsonArray("hits");
        List<Document> results = new ArrayList<>(hits.size());
        for (int i = 0; i < hits.size(); i++) {
            JsonObject hit = hits.getJsonObject(i);
            Document fruit = hit.getJsonObject("_source").mapTo(Document.class);
            results.add(fruit);
        }
        return results;
    }
}