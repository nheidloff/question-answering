package com.ibm.question_answering.primeqa;

public class Answer {

    public Answer(){
        this.text = "";
        this.confidence_score = 0;
        this.start_char_offset = 0;
        this.end_char_offset = 0;
        this.context_index = 0;
    }
    
    public String text;
    public float confidence_score;
    public int start_char_offset;
    public int end_char_offset;
    public int context_index;
}
