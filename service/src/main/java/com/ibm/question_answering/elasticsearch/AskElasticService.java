package com.ibm.question_answering.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.Metrics;
import com.ibm.question_answering.api.Result;

@ApplicationScoped
public class AskElasticService {
    public AskElasticService() {}

    final String ELASTIC_SEARCH_URL_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "ELASTIC_SEARCH_URL", defaultValue = ELASTIC_SEARCH_URL_NOT_SET) 
    private String url;
    final static String ERROR_ELASTIC_SEARCH_URL_NOT_SET = ElasticExceptionMapper.ERROR_ELASTIC_PREFIX + "ELASTIC_SEARCH_URL not defined";

    final String ELASTIC_SEARCH_INDEX_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "ELASTIC_SEARCH_INDEX", defaultValue = ELASTIC_SEARCH_INDEX_NOT_SET) 
    private String index;
    final static String ERROR_ELASTIC_SEARCH_INDEX_NOT_SET = ElasticExceptionMapper.ERROR_ELASTIC_PREFIX + "ELASTIC_SEARCH_INDEX not defined";

    final public static String ELASTIC_SEARCH_USER_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "ELASTIC_SEARCH_USER", defaultValue = ELASTIC_SEARCH_USER_NOT_SET) 
    private String user;
    final static String ERROR_ELASTIC_SEARCH_USER_NOT_SET = ElasticExceptionMapper.ERROR_ELASTIC_PREFIX + "ELASTIC_SEARCH_USER not defined";

    final public static String ELASTIC_SEARCH_PASSWORD_NOT_SET = "NOT_SET";   
    @ConfigProperty(name = "ELASTIC_SEARCH_PASSWORD", defaultValue = ELASTIC_SEARCH_PASSWORD_NOT_SET) 
    private String password;
    final static String ERROR_ELASTIC_SEARCH_PASSWORD_NOT_SET = ElasticExceptionMapper.ERROR_ELASTIC_PREFIX + "ELASTIC_SEARCH_PASSWORD not defined";

    String field1;
    String field2;
    String field3;
    String filterName1;
    String filterName2;
    String filterName3;
    String filterValue1;
    String filterValue2;
    String filterValue3;
    String highlightField;
    int maxResults;
    int MAX_RESULTS_DEFAULT = 5;

    private void readAndCheckEnvironmentVariables() {
        if (url.equalsIgnoreCase(ELASTIC_SEARCH_URL_NOT_SET)) {
            System.err.println(ERROR_ELASTIC_SEARCH_URL_NOT_SET);
            throw new RuntimeException(ERROR_ELASTIC_SEARCH_URL_NOT_SET);
        }
        if (url.equalsIgnoreCase(ELASTIC_SEARCH_INDEX_NOT_SET)) {
            System.err.println(ERROR_ELASTIC_SEARCH_INDEX_NOT_SET);
            throw new RuntimeException(ERROR_ELASTIC_SEARCH_INDEX_NOT_SET);
        }
        if (url.equalsIgnoreCase(ELASTIC_SEARCH_USER_NOT_SET)) {
            System.err.println(ERROR_ELASTIC_SEARCH_USER_NOT_SET);
            throw new RuntimeException(ERROR_ELASTIC_SEARCH_USER_NOT_SET);
        }
        if (url.equalsIgnoreCase(ELASTIC_SEARCH_PASSWORD_NOT_SET)) {
            System.err.println(ERROR_ELASTIC_SEARCH_PASSWORD_NOT_SET);
            throw new RuntimeException(ERROR_ELASTIC_SEARCH_PASSWORD_NOT_SET);
        }
        String envVar = System.getenv("ELASTIC_SEARCH_FIELD_1");
        if ((envVar != null) && (!envVar.equals(""))) {
            field1 = envVar;   
        } 
        envVar = System.getenv("ELASTIC_SEARCH_FIELD_2");
        if ((envVar != null) && (!envVar.equals(""))) {
            field2 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FIELD_3");
        if ((envVar != null) && (!envVar.equals(""))) {
            field3 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_NAME_1");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterName1 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_NAME_2");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterName2 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_NAME_3");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterName3 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_VALUE_1");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterValue1 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_VALUE_2");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterValue2 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_FILTER_VALUE_3");
        if ((envVar != null) && (!envVar.equals(""))) {
            filterValue3 = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_HIGHLIGHT_FIELD");
        if ((envVar != null) && (!envVar.equals(""))) {
            highlightField = envVar;   
        }
        envVar = System.getenv("ELASTIC_SEARCH_MAX_OUTPUT_DOCUMENTS");
        if ((envVar != null) && (!envVar.equals(""))) {
            this.maxResults = MAX_RESULTS_DEFAULT;
            try{
                this.maxResults = Integer.parseInt(envVar);
            }
            catch (NumberFormatException ex){
            }
        }
    }

    @Inject
    Metrics metrics;

    @Inject
    ElasticServiceResource elasticResource;
   
    public com.ibm.question_answering.api.Answer search(String query) {
        readAndCheckEnvironmentVariables();

        com.ibm.question_answering.api.Answer output = convertToAnswer(elasticResource.search(createInput(query)));
        return output;
    }

    // The following method 'createInput' needs to create the following input for ElasticSearch
    // Note that the field name 'plainTextContent' is a variable which is why custom serialization needs to be used
    // https://stackoverflow.com/questions/76228719/jackson-serializer-not-invoked-in-quarkus
/* 
curl -X POST \
-u $ELASTIC_SEARCH_USER:$ELASTIC_SEARCH_PASSWORD \
"$ELASTIC_SEARCH_URL$ELASTIC_SEARCH_INDEX/_search" \
-H "Content-Type: application/json" \
-d '
{
    "size" : 10,
    "highlight": {
        "fields": [
        {
            "plainTextContent": {
                "type": "unified",
                "require_field_match": "true",
                "fragment_size": 300,
                "number_of_fragments":2
            }
        }
        ]
    },
    "query": {
        "bool": {
            "must": {
                "multi_match": {
                    "query": "some search query",
                    "fields": [
                        "fileTitle",
                        "plainTextContent"
                    ],
                    "type": "cross_fields"
                }
            }
        }
    }
}
'
*/
    public com.ibm.question_answering.elasticsearch.Input createInput(String query) {
        com.ibm.question_answering.elasticsearch.Input output = new com.ibm.question_answering.elasticsearch.Input();
        output.highlight = new Highlight();
        output.size = this.maxResults;
        ArrayList<String> fields = new ArrayList<String>();
        if (field1 != null) {
            fields.add(field1);
        }
        if (field2 != null) {
            fields.add(field2);
        }
        if (field3 != null) {
            fields.add(field3);
        }
        String[] fieldsAsString = fields.toArray(new String[fields.size()]);
        MultiMatch multiMatch = new MultiMatch();
        multiMatch.fields = fieldsAsString;
        multiMatch.type = "cross_fields";
        multiMatch.query = query;
        Must must = new Must();
        must.multi_match = multiMatch;
        Bool bool = new Bool();
        bool.must = must;
        Query queryInput = new Query();
        queryInput.bool = bool;
        output.query = queryInput;
        return output;
    }

    public com.ibm.question_answering.api.Answer convertToAnswer(Response response) {
        List<Document> documents;
        com.ibm.question_answering.api.Answer output = new com.ibm.question_answering.api.Answer(false, 0, null);
        com.ibm.question_answering.api.Result result;
        ArrayList<Result> results = new ArrayList<Result>();
        if (response != null) {
            if (response.hits != null) {
                if (response.hits.hits != null) {
                    documents = new ArrayList<>();
                    for (int indexHits = 0; indexHits < response.hits.hits.length; indexHits++) {
                        documents.add(response.hits.hits[indexHits]._source);
                    }
                
                    if (documents != null) {
                        if (documents.size() > 0) {
                            output.matching_results = documents.size();
                            for (int index = 0; index < documents.size(); index++) {
                                result = new com.ibm.question_answering.api.Result();
                                result.document_id = documents.get(index).fileId;
                                result.title = documents.get(index).fileTitle;
                                result.text = new com.ibm.question_answering.discovery.Text();
                                result.text.text = documents.get(index).plainTextContent;
                                results.add(result);
                            }
                            output.results = results;
                        }
                    }
                }
            }
        }
        return output;
    }
}