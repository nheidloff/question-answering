package com.ibm.question_answering.file;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Document {
    public Document() {}
    
    public String title;
    public String url;
    public String[] text;
}
