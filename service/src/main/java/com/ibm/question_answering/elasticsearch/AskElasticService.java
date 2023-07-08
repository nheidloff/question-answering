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

    String vectorSearch = System.getenv("ELASTIC_SEARCH_USE_VECTOR");
    private boolean useVectorSearch = false;

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
    String resultUrl;
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
        envVar = System.getenv("ELASTIC_SEARCH_RESULT_URL");
        if ((envVar != null) && (!envVar.equals(""))) {
            resultUrl = envVar;   
        } 
        if ((vectorSearch != null) && (!vectorSearch.equals(""))) {
            if (vectorSearch.equalsIgnoreCase("true")) {
                useVectorSearch = true;
            }
        }
    }

    @Inject
    Metrics metrics;

    @Inject
    ElasticServiceResource elasticResource;
   
    public com.ibm.question_answering.api.Answer search(String query) {
        readAndCheckEnvironmentVariables();
        metrics.elasticStarted(this.maxResults);
        com.ibm.question_answering.api.Answer output = convertToAnswer(elasticResource.search(createInput(query)));
        metrics.elasticStopped(output);
        return output;
    }

    public com.ibm.question_answering.elasticsearch.Input createInput(String query) {
        com.ibm.question_answering.elasticsearch.Input output = new com.ibm.question_answering.elasticsearch.Input();
        output.size = this.maxResults;

        if (useVectorSearch == false) {
            output.highlight = new Highlight();        
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
            bool.filter = new Filter();
            Query queryInput = new Query();
            queryInput.bool = bool;
            output.query = queryInput;
        }
        else {
            Query queryInput = new Query();
            queryInput.text_expansion = new TextExpansion();
            queryInput.text_expansion.query = query;
            output.query = queryInput;
        }
        return output;
    }

    public com.ibm.question_answering.api.Answer convertToAnswer(Response response) {
        List<Hit> hits;
        com.ibm.question_answering.api.Answer output = new com.ibm.question_answering.api.Answer(false, 0, null);
        com.ibm.question_answering.api.Result result;
        ArrayList<Result> results = new ArrayList<Result>();
        if (response != null) {
            if (response.hits != null) {
                if (response.hits.hits != null) {
                    hits = new ArrayList<>();
                    for (int indexHits = 0; indexHits < response.hits.hits.length; indexHits++) {
                        hits.add(response.hits.hits[indexHits]);
                    }
                
                    if (hits != null) {
                        if (hits.size() > 0) {
                            output.matching_results = hits.size();
                            for (int index = 0; index < hits.size(); index++) {
                                result = new com.ibm.question_answering.api.Result();
                                result.document_id = hits.get(index)._source.id;
                                result.url = buildUrl(hits.get(index)._source);
                                result.title = hits.get(index)._source.title;
                                // TODO
                                result.text = new com.ibm.question_answering.discovery.Text();
                                result.text.text = new String[1];
                                result.text.text[0] = hits.get(index)._source.text;  
                                /* 
                                if (hits.get(index).highlight != null) {
                                    result.text.text = hits.get(index).highlight.text;
                                    if (result.text.text != null) {
                                        for (int indexText = 0; indexText < result.text.text.length; indexText++) {
                                            String oneLine = result.text.text[indexText];
                                            oneLine = oneLine.replaceAll("<em>", "");
                                            oneLine = oneLine.replaceAll("</em>", "");
                                            result.text.text[indexText] = oneLine;
                                        }
                                    }
                                    //result = addFirstWords(result, hits.get(index));
                                }
                                */
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

    private Result addFirstWords(Result result, Hit hit) {
        int amountWords = 0;
        int maxAmountWords = 100;
        if (result != null) {
            if (result.text != null) {
                if (result.text.text != null) {
                    for (int indexText = 0; indexText < result.text.text.length; indexText++) {
                        String oneLine = result.text.text[indexText];
                        amountWords = amountWords + getAmountWords(oneLine);
                    }
                } 
            } 
            // TODO
            //String fullText = getFullText(hit._source.text);
            String fullText = hit._source.text;
            String firstWords = getFirstWords(amountWords, maxAmountWords, fullText);
            String[] allText = new String[result.text.text.length + 1];
            for (int index = 0; index < result.text.text.length; index++) {
                allText[index] = result.text.text[index];
            }
            allText[result.text.text.length] = firstWords;
            result.text.text = allText;
        }
        return result;
    }

    private String getFirstWords(int amountWords, int maxAmountWords, String fullText) {
        String output = "";
        if (fullText != null ) {
            boolean more = true;
            while (more) {
                if (amountWords >= maxAmountWords) {
                    more = false;
                }
                else {
                    int indexOfSpace = fullText.indexOf(" ");
                    if (indexOfSpace == -1) {
                        more = false;
                    }
                    else {
                        output = output + " " + fullText.substring(0, indexOfSpace);
                        amountWords++;
                        fullText = fullText.substring(indexOfSpace, fullText.length()).trim();
                    }
                }
            }
        }
        return output.trim();
    }

    private int getAmountWords(String text) {
        long count = text.chars().filter(character -> character == ' ').count();
        return (int)count +  1;
    }

    private String getFullText(String[] fullTextArray) {
        String output = "";
        for (int indexText = 0; indexText < fullTextArray.length; indexText++) {
            output = output + " " + fullTextArray[indexText];
        }
        return output.trim();
    }

    private String buildUrl(Document result) {
        String output = this.resultUrl;
        String urlPart1 = System.getenv("ELASTIC_SEARCH_FIELD_RESULT_SINGLE_1");
        String urlPart2 = System.getenv("ELASTIC_SEARCH_FIELD_RESULT_SINGLE_2");
        String urlPlaceholderPart1 = "ELASTIC_SEARCH_FIELD_RESULT_SINGLE_1";
        String urlPlaceholderPart2 = "ELASTIC_SEARCH_FIELD_RESULT_SINGLE_2";
        if (this.resultUrl != null) {
            if ((urlPart1 != null) && (!urlPart1.equals("") && (urlPlaceholderPart1 != null) && (result.urlField1 != null))) {
                output = output.replaceAll(urlPlaceholderPart1, result.urlField1);
            }
            if ((urlPart2 != null) && (!urlPart2.equals("") && (urlPlaceholderPart2 != null) && (result.urlField2 != null))) {
                output = output.replaceAll(urlPlaceholderPart2, result.urlField2);
            }
        }
        return output;
    }
}