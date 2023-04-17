package com.ibm.question_answering.discovery;

public class Passages {

    final static int DISCOVERY_CHARACTERS = 1000;
    final static boolean DISCOVERY_FIND_ANSWERS = false;
   
    public Passages() {
        this.characters = DISCOVERY_CHARACTERS;
        String envDiscoveryCharacters = System.getenv("EXPERIMENT_DISCOVERY_CHARACTERS");
        if ((envDiscoveryCharacters != null) && (!envDiscoveryCharacters.equals(""))) {
            try {
                this.characters = Integer.parseInt(envDiscoveryCharacters);
            } catch (Exception e) {}
        }
        this.find_answers = DISCOVERY_FIND_ANSWERS;
        String envDiscoveryFindAnswers = System.getenv("EXPERIMENT_DISCOVERY_FIND_ANSWERS");
        if ((envDiscoveryFindAnswers != null) && (!envDiscoveryFindAnswers.equals(""))) {
            if (envDiscoveryFindAnswers.equalsIgnoreCase("true")) {
                this.find_answers = true;
            }
        }
        this.fields = new String[1];
        this.fields[0] = "text";
    }

    public String[] fields;
    public boolean enabled = true;
    public int characters;
    public boolean find_answers;
}