package com.ibm.question_answering.maas;

public class Parameters {

    // MaaS Problem
    // public final static double DEFAULT_TEMPERATURE = 0.7;
    public final static double DEFAULT_TEMPERATURE = 0;
    public final static int DEFAULT_MIN_NEW_TOKENS = 10;
    public final static int DEFAULT_MAX_NEW_TOKENS = 25;
 
    public Parameters() {
        this.max_new_tokens = DEFAULT_MAX_NEW_TOKENS;
        this.min_new_tokens = DEFAULT_MIN_NEW_TOKENS;

        // MaaS problem
        // this.temperature = 0.7;
        this.temperature = DEFAULT_TEMPERATURE;
    }

    public Parameters(double temperature, int max_new_tokens, int min_new_tokens) {
        this.max_new_tokens = max_new_tokens;
        this.min_new_tokens = min_new_tokens;
        // MaaS problem        
        // this.temperature = 0.7;       
        this.temperature = temperature;
        this.stream = false;
    }
    
    public double temperature;
    public int max_new_tokens;
    public int min_new_tokens;
    public boolean stream;
}
