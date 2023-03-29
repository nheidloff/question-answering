package com.ibm.question_answering;

import com.ibm.question_answering.discovery.Text;

public class Result {

    public static final String TITLE_ONE_ANSWER = "Answer";

    public Result() {}

    public Result(String documentId, String title, String[] text, String url, DocumentPassage[] documentPassages) {
        this.document_id = documentId;
        this.title = title;
        Text subObject = new Text();
        subObject.text = text;
        this.text = subObject;
        this.url = url;
        this.document_passages = documentPassages;        
    }

    public String document_id;

    public String chunckid;
    
    public String title;

    //public String text[];
    public Text text;

    public String url;

    public DocumentPassage[] document_passages;

    public ResultMetaData result_metadata;
}
