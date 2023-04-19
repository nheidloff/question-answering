package com.ibm.question_answering.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ibm.question_answering.reranker.DocumentScore;

@ApplicationScoped
public class DocumentsReader {

    public final static String DOCUMENTS_FILE_NAME = "test-small.json";
    public final static int MINIMAL_AMOUNT_WORDS_PER_TEXT_ENTRY = 3;
 
    public List<Document> read() {
        List<Document> output = new ArrayList<Document>();
        try {
            File file = new File(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource(DOCUMENTS_FILE_NAME)).getFile()
            );
            
            ObjectMapper objectMapper = new ObjectMapper();           
            SimpleModule module = new SimpleModule();
            module.addDeserializer(com.ibm.question_answering.file.Document.class, 
                new com.ibm.question_answering.file.DocumentDeserializer());
            objectMapper.registerModule(module);
                      
            List<com.ibm.question_answering.file.Document> documents = 
                Arrays.asList(objectMapper.readValue(file, com.ibm.question_answering.file.Document[].class));
            
            output = documents;
        } catch (Exception e) {
            System.out.println(e);
        }
        return output;
    }

    public DocumentScore[] getReRankerInput(List<Document> documents) {
        DocumentScore[] output = null;
        int totalFilteredDocuments = 0;
        List<Document> filteredDocuments = new ArrayList<Document>();
        for (int index = 0; index < documents.size(); index++) {
            Document originalDocument = documents.get(index);
            Document newDocument = new Document();
            newDocument.title = originalDocument.title;
            newDocument.url = originalDocument.url;
            String[] strings = null;
            if (originalDocument.text != null) {
                ArrayList<String> filteredTexts = new ArrayList<String>();
                for (int indexTextEntries = 0; indexTextEntries < originalDocument.text.length; indexTextEntries++) {
                    if (getAmountWords(originalDocument.text[indexTextEntries]) >= MINIMAL_AMOUNT_WORDS_PER_TEXT_ENTRY) {
                        filteredTexts.add(originalDocument.text[indexTextEntries]);
                        totalFilteredDocuments++;
                    }
                }
                strings = filteredTexts.stream().toArray(String[]::new);
            }
            newDocument.text = strings;
            filteredDocuments.add(newDocument);
        }
        System.out.println(totalFilteredDocuments);
        output = new DocumentScore[totalFilteredDocuments];
        int id = 0;
        for (int index = 0; index < filteredDocuments.size(); index++) {
            for (int indexTextEntries = 0; indexTextEntries < filteredDocuments.get(index).text.length; indexTextEntries++) {
                com.ibm.question_answering.reranker.Document document = new com.ibm.question_answering.reranker.Document();
                document.document_id = String.valueOf(id);
                document.title = filteredDocuments.get(index).title;
                document.text = filteredDocuments.get(index).text[indexTextEntries].trim();
                DocumentScore documentScore = new DocumentScore(document, 0);
                output[id] = documentScore;
                id++;
            }
        }
        return output;
    }

    private int getAmountWords(String text) {      
        text = text.trim(); 
        long count = text.chars().filter(ch -> ch == ' ').count();
        return (int)count + 1;
    }
}
