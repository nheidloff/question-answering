package com.ibm.question_answering.proxy;

public class Input {
    
    public Input(String bam_api_key, String bam_url, com.ibm.question_answering.maas.Input bam_payload) {
        this.bam_api_key = bam_api_key;
        this.bam_url = bam_url;
        this.bam_payload = bam_payload;
    }

    public String bam_api_key;
    public com.ibm.question_answering.maas.Input bam_payload;
    public String bam_url;
}