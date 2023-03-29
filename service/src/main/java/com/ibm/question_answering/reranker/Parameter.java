package com.ibm.question_answering.reranker;

public class Parameter {

    public final static String MODEL_DRDECT = "/store/checkpoints/drdecr/DrDecr.dnn";

    public Parameter(String model) {
        this.value = model;
        this.parameter_id = "model";
    }

    public String parameter_id;
    public String value;
}
