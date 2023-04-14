package com.ibm.question_answering.maas;

public class Input {
    
    public Input(String model_id, String[] inputs, Parameters parameters) {
        this.model_id = model_id;
        this.inputs = inputs;
        this.parameters = parameters;
    }

    public String model_id;
    public String[] inputs;
    public Parameters parameters;
}
