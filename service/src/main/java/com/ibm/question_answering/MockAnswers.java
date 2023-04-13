package com.ibm.question_answering;

import java.util.ArrayList;
import java.util.UUID;

public class MockAnswers {
    
    public static Answer getConfidentAnswer() {
        ArrayList<Result> results = new ArrayList<Result>();
        results.add(getResultAnswer());
        results.add(getResultDocument1());
        results.add(getResultDocument2());
        return new Answer(true, 2, results);
    }

    public static Answer getNotConfidentAnswer() {
        ArrayList<Result> results = new ArrayList<Result>();
        results.add(getResultDocument1());
        results.add(getResultDocument2());
        return new Answer(false, 2, results);
    }

    public static Answer getEmptyAnswer() {
        ArrayList<Result> results = new ArrayList<Result>();
        results.add(getResultEmpty());
        return new Answer(false, 1, results);
    }

    public static Answer getErrorAnswer() {
        ArrayList<Result> results = new ArrayList<Result>();
        results.add(getErrorResult());
        return new Answer(false, 1, results);
    }

    public static Result getResultDocument1() {
        UUID randomUUID = UUID.randomUUID();  
        DocumentPassage[] documentPassages = new DocumentPassage[1];
        PassageAnswer[] passageAnswers = new PassageAnswer[1];
        passageAnswers[0] = new PassageAnswer("IBM acquires Red Hat", 0.07588528);
        documentPassages[0] = new DocumentPassage("<em>IBM</em> <em>acquires</em> <em>Red</em> <em>Hat</em>", DocumentPassage.FIELD_TEXT, passageAnswers);
        String text[] = new String[1];
        text[0] = "It's official - IBM has acquired Red Hat! The deal was announced in October 2018. IBM Closes Landmark Acquisition of Red Hat.";
        return new Result(randomUUID.toString(), 
            "IBM acquires Red Hat", 
            text, 
            "https://www.ibm.com/support/pages/ibm-acquires-red-hat",
            documentPassages);
    }

    public static Result getResultDocument2() {
        UUID randomUUID = UUID.randomUUID();  
        DocumentPassage[] documentPassages = new DocumentPassage[1];
        PassageAnswer[] passageAnswers = new PassageAnswer[1];
        passageAnswers[0] = new PassageAnswer("$190.00 per share in cash", 0.6790031);
        documentPassages[0] = new DocumentPassage("<em>IBM</em> (NYSE<em>:</em><em>IBM</em>) and <em>Red</em> <em>Hat</em> announced today that they have closed the transaction under which <em>IBM</em> <em>acquired</em> all of the issued and outstanding common shares of <em>Red</em> <em>Hat</em> for $190.00 per share in cash, representing a total equity value of approximately $34 billion.", DocumentPassage.FIELD_TEXT, passageAnswers);
        String text[] = new String[1];
        text[0] = "IBM (NYSE:IBM) and Red Hat announced today that they have closed the transaction under which IBM acquired all of the issued and outstanding common shares of Red Hat for $190.00 per share in cash, representing a total equity value of approximately $34 billion. The acquisition redefines the cloud market for business. Red Hat’s open hybrid cloud technologies are now paired with the unmatched scale and depth of IBM’s innovation and industry expertise, and sales leadership in more than 175 countries. Together, IBM and Red Hat will accelerate innovation by offering a next-generation hybrid multicloud platform. Based on open source technologies, such as Linux and Kubernetes, the platform will allow businesses to securely deploy, run and manage data and applications on-premises and on private and multiple public clouds.";
        return new Result(randomUUID.toString(), 
            "IBM Closes Landmark Acquisition of Red Hat; Defines Open, Hybrid Cloud Future", 
            text, 
            "https://www.redhat.com/en/about/press-releases/ibm-closes-landmark-acquisition-red-hat-34-billion-defines-open-hybrid-cloud-future",
            documentPassages);
    }

    public static Result getResultAnswer() {
        UUID randomUUID = UUID.randomUUID();
        String text[] = new String[1];
        text[0] = "IBM has acquired Red Hat for $34 billion in October 2018.";
        return new Result(randomUUID.toString(), 
            Result.TITLE_ONE_ANSWER, 
            text, 
            null,
            null);
    }

    public static Result getResultEmpty() {
        UUID randomUUID = UUID.randomUUID(); 
        String text[] = new String[1];
        text[0] = "";
        return new Result(randomUUID.toString(), 
            "", 
            text, 
            "",
            null);
    }

    public static Result getErrorResult() {
        UUID randomUUID = UUID.randomUUID(); 
        String text[] = new String[1];
        text[0] = "";
        return new Result(randomUUID.toString(), 
            "Error", 
            text, 
            "",
            null);
    }
}