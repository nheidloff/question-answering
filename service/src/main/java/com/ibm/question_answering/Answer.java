package com.ibm.question_answering;

import java.util.ArrayList;

import com.ibm.question_answering.Result;
import com.ibm.question_answering.RetrievalDetails;

public class Answer {

    public Answer(boolean highConfidence, int amount, ArrayList<Result> results) {
        matching_results = amount;
        retrievalDetails = new RetrievalDetails(highConfidence);
        this.results = results;        
    }

    public int matching_results;

    public RetrievalDetails retrievalDetails;

    public ArrayList<Result> results;    
}