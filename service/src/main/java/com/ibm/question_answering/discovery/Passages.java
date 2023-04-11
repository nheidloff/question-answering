package com.ibm.question_answering.discovery;

public class Passages {
    public Passages() {
        this.fields = new String[1];
        this.fields[0] = "text";
    }
    public String[] fields;
    public boolean enabled = true;
    public int characters = 250;
    public boolean find_answers = true;
}