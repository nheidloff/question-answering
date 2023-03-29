package com.ibm.question_answering.primeqa;

import java.util.List;

public class Reader {

    public Reader(String reader_id, String provenance, List<Parameter> parameters) {
        this.reader_id = reader_id;
        if (reader_id == null) {
            this.reader_id = READER_ID_PRIME_QA;
        } else if (reader_id.equals("")) {
            this.reader_id = READER_ID_PRIME_QA;
        }
        this.provenance = provenance;
        this.parameters = parameters;
        if (provenance == null) {
            this.provenance = PROVENANCE_PRIME_QA;
        } else if (provenance.equals("")) {
            this.provenance = PROVENANCE_PRIME_QA;
        }
    }
    
    public final static String PROVENANCE_PRIME_QA = "PrimeQA";
    public final static String READER_ID_PRIME_QA = "ExtractiveReader";

    public String reader_id;
    public String provenance;
    public List<Parameter> parameters;
}
