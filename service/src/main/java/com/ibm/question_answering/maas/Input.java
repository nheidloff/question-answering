package com.ibm.question_answering.maas;

public class Input {
    
    public final static String MODEL_ID_FLAN_T5 = "google/flan-t5-xxl";
    public final static String MODEL_ID_UL2 = "google/ul2";

    public final static String MODEL_DEFAULT = MODEL_ID_UL2;

    public Input(String model_id, String[] inputs, Parameters parameters) {
        this.model_id = model_id;
        this.inputs = inputs;
        this.parameters = parameters;
    }

    public String model_id;
    public String[] inputs;
    public Parameters parameters;
}
