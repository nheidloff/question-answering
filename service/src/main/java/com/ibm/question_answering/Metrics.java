package com.ibm.question_answering;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.question_answering.discovery.RelevantOutput;
import com.ibm.question_answering.reranker.DocumentScore;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Metrics {

    @ConfigProperty(name = "EXPERIMENT_METRICS_SESSION") 
    Optional<String> sessionOptionalString;

    String session = null;
    boolean enabled = false;

    @ConfigProperty(name = "EXPERIMENT_METRICS_DIRECTORY") 
    Optional<String> directoryOptionalString;

    public Metrics() {
    }

    final String[] headersMetadata = {
        "METRICS_SESSION", 
        "ENDPOINT",
        "LLM_NAME",
        "LLM_MIN_NEW_TOKENS",
        "LLM_MAX_NEW_TOKENS",
        "LLM_MAX_INPUT_DOCUMENTS",
        "RERANKER_MAX_INPUT_DOCUMENTS",
        "RERANKER_MODEL",
        "RERANKER_ID",
        "URL",
        "DISCOVERY_MAX_OUTPUT_DOCUMENTS",
        "PROMPT_TEMPLATE"
    };

    final String[] headersRuns = {
        "METRICS_SESSION", 
        "ENDPOINT",
        "QUERY",
        "ANSWER",
        "RESULT_DISCOVERY_PASSAGE1",
        "RESULT_DISCOVERY_PASSAGE1_ID",
        "RESULT_DISCOVERY_PASSAGE2",
        "RESULT_DISCOVERY_PASSAGE2_ID",
        "RESULT_DISCOVERY_PASSAGE3",
        "RESULT_DISCOVERY_PASSAGE3_ID",
        "RESULT_DISCOVERY_PASSAGE4",
        "RESULT_DISCOVERY_PASSAGE4_ID",
        "RESULT_DISCOVERY_PASSAGE5",
        "RESULT_DISCOVERY_PASSAGE5_ID",
        "RESULT_DISCOVERY_PASSAGE6",
        "RESULT_DISCOVERY_PASSAGE6_ID",
        "RESULT_DISCOVERY_PASSAGE7",
        "RESULT_DISCOVERY_PASSAGE7_ID",
        "RESULT_DISCOVERY_PASSAGE8",
        "RESULT_DISCOVERY_PASSAGE8_ID",
        "RESULT_DISCOVERY_PASSAGE9",
        "RESULT_DISCOVERY_PASSAGE9_ID",
        "RESULT_DISCOVERY_PASSAGE10",
        "RESULT_DISCOVERY_PASSAGE10_ID",
        "RESULT_RERANKER_PASSAGE1",
        "RESULT_RERANKER_PASSAGE1_ID",
        "RESULT_RERANKER_PASSAGE2",
        "RESULT_RERANKER_PASSAGE2_ID",
        "RESULT_RERANKER_PASSAGE3",
        "RESULT_RERANKER_PASSAGE3_ID",
        "RESULT_RERANKER_PASSAGE4",
        "RESULT_RERANKER_PASSAGE4_ID",
        "RESULT_RERANKER_PASSAGE5",
        "RESULT_RERANKER_PASSAGE5_ID",
        "RESULT_RERANKER_PASSAGE6",
        "RESULT_RERANKER_PASSAGE6_ID",
        "RESULT_RERANKER_PASSAGE7",
        "RESULT_RERANKER_PASSAGE7_ID",
        "RESULT_RERANKER_PASSAGE8",
        "RESULT_RERANKER_PASSAGE8_ID",
        "RESULT_RERANKER_PASSAGE9",
        "RESULT_RERANKER_PASSAGE9_ID",
        "RESULT_RERANKER_PASSAGE10",
        "RESULT_RERANKER_PASSAGE10_ID",
        "TIMESTAMP_START",
        "TIMESTAMP_END",
        "TIMESTAMP_DISCOVERY_START",
        "TIMESTAMP_DISCOVERY_END",
        "TIMESTAMP_RERANKER_START",
        "TIMESTAMP_RERANKER_END",
        "TIMESTAMP_MAAS_START",
        "TIMESTAMP_MAAS_END",
        "SIZE_DISCOVERY_RESULTS",
        "SIZE_RERANKER_INPUTS",
        "SIZE_RERANKER_RESULTS"
    };

    String endpoint;
    String answer;
    String[] resultDiscovery;
    String[] resultDiscoveryChunkIds;
    String[] resultReRanker;
    String[] resultReRankerChunkIds;
    String query;
    String prompt;
    String llmName;
    String llmMinNewTokens;
    String llmMaxNewTokens;
    String llmMaxInputDocuments;
    String rrAmountInputDocuments;
    String rrModel;
    String rrId;
    String url;
    String tsRRStart;
    String tsRREnd;
    String tsStart;
    String tsEnd;
    String tsDiscoveryStart;
    String tsDiscoveryEnd;
    String tsMaaSStart;
    String tsMaaSEnd;
    String sizeDiscoveryResults;
    String sizeDiscoverySentResults;
    String sizeReRankerInputs;
    String sizeReRankerResults;
    String discoveryMaxDocuments;
    String promptTemplate;

    final String fileNameMetadata = "Metadata.csv";
    final String fileNameRuns = "Runs.csv";
    final String fileNameLastRun = "Last-Run.md";
    String directory = "/deployments/metrics";
    String directoryAndFileNameMetadata;
    String directoryAndfileNameRuns;
    String directoryAndfileNameLastRun;
    boolean reRankerUsed = false;

    public void end() {
        this.tsEnd = getTimestamp();
        this.writeMetadata();
        this.writeRun();
        this.writeLastRunReadable();
    }

    public void start(javax.ws.rs.core.UriInfo uriInfo, String query) {
        this.tsStart = getTimestamp();
        try {
            if (sessionOptionalString.isPresent()) {
                this.session = sessionOptionalString.get();
                if ((this.session != null) && (!this.session.equals(""))) {
                    this.enabled = true;
                }
            }
            if (directoryOptionalString.isPresent()) {
                directory = directoryOptionalString.get();
            }
        } catch (Exception e) {}
        directoryAndFileNameMetadata = directory + '/' + this.session + '-' + fileNameMetadata;
        directoryAndfileNameRuns = directory + '/' + this.session + '-' + fileNameRuns;
        directoryAndfileNameLastRun = directory + '/' + fileNameLastRun;

        this.endpoint = uriInfo.getPath();
        this.url = uriInfo.getRequestUri().toString();
        this.query = query;   

        this.resultReRanker = new String[10];
        this.resultReRankerChunkIds = new String[10];
    }

    void writeRun() {
        if (this.enabled == true) {
            boolean setSkipHeaderRecord = true;
            Path path = Paths.get(this.directoryAndfileNameRuns);
            if (Files.exists(path) == false) {
                setSkipHeaderRecord = false;
            }
            try {
                BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(this.directoryAndfileNameRuns), 
                    StandardOpenOption.APPEND, 
                    StandardOpenOption.CREATE);
                CSVFormat formatRuns = CSVFormat.DEFAULT.builder()
                    .setHeader(headersRuns)
                    .setSkipHeaderRecord(setSkipHeaderRecord)
                    .build();
                CSVPrinter csvPrinterRuns = new CSVPrinter(writer, formatRuns);
    
                List<String> data = new ArrayList<String>();
                data.add(this.session);
                data.add(this.endpoint);
                data.add(this.query);
                data.add(this.answer);
                for (int index = 0; index < 10; index++) {
                    data.add(this.resultDiscovery[index]);
                    data.add(this.resultDiscoveryChunkIds[index]);
                }
                for (int index = 0; index < 10; index++) {
                    data.add(this.resultReRanker[index]);
                    data.add(this.resultReRankerChunkIds[index]);
                }
                data.add(this.tsStart);
                data.add(this.tsEnd);
                data.add(this.tsDiscoveryStart);
                data.add(this.tsDiscoveryEnd);
                data.add(this.tsRRStart);
                data.add(this.tsRREnd);
                data.add(this.tsMaaSEnd);
                data.add(this.tsMaaSEnd);
                data.add(this.sizeDiscoveryResults);
                data.add(this.sizeReRankerInputs);
                data.add(this.sizeReRankerResults);
                csvPrinterRuns.printRecord(data);
    
                csvPrinterRuns.flush();
                csvPrinterRuns.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void writeMetadata() {
        if (this.enabled == true) {
            try {
                FileWriter fileWriteMetadata = new FileWriter(this.directoryAndFileNameMetadata);
                PrintWriter printWriterMetadata = new PrintWriter(fileWriteMetadata);
                CSVFormat formatMetadata = CSVFormat.DEFAULT.builder()
                    .setHeader(headersMetadata)
                    .setSkipHeaderRecord(false)
                    .build();
                CSVPrinter csvPrinterMetadata = new CSVPrinter(printWriterMetadata, formatMetadata);

                List<String> data = new ArrayList<String>();
                data.add(this.session);
                data.add(this.endpoint);
                data.add(this.llmName);
                data.add(this.llmMinNewTokens);
                data.add(this.llmMaxNewTokens);
                data.add(this.llmMaxInputDocuments);
                data.add(this.rrAmountInputDocuments);
                data.add(this.rrModel);
                data.add(this.rrId);
                data.add(this.url);
                data.add(this.discoveryMaxDocuments);
                data.add(this.promptTemplate);

                csvPrinterMetadata.printRecord(data);
                csvPrinterMetadata.flush();
                csvPrinterMetadata.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reRankerStarted(String id, String model) {
        this.rrId = id;
        this.rrModel = model;
        this.tsRRStart = getTimestamp();
        this.reRankerUsed = true;
    }

    public void reRankerStopped(DocumentScore[][] result) {
        this.tsRREnd = getTimestamp();
        if ((result != null) && (result.length > 0)) {
            this.sizeReRankerResults = String.valueOf(result[0].length);
            
            for (int index = 0; index < result[0].length; index++) {
                if (index < 10) {
                    String resultString = "";
                    String resultStringChunkIds = "";
                    if (index <= result[0].length) {
                        resultString = result[0][index].document.title + ". " + result[0][index].document.text;
                        resultStringChunkIds = result[0][index].document.document_id;
                    }    
                    this.resultReRanker[index] = resultString;
                    this.resultReRankerChunkIds[index] = resultStringChunkIds;
                }
            }
        }
    }

    public void discoveryStarted(int maxDocuments) {
        this.tsDiscoveryStart = getTimestamp();
        this.discoveryMaxDocuments = String.valueOf(maxDocuments);
    }

    public void discoveryStopped(com.ibm.question_answering.api.Answer answer) {
        this.tsDiscoveryEnd = getTimestamp();
        if (answer != null) {
            this.sizeDiscoveryResults = String.valueOf(answer.matching_results);
            this.sizeDiscoverySentResults = String.valueOf(answer.results.size());
            this.resultDiscovery = new String[10];
            this.resultDiscoveryChunkIds = new String[10];

            int amountResults = answer.results.size();
            if (amountResults > 10) {
                amountResults = 10;
            }
            
            for (int index = 0; index < amountResults; index++) {
                String resultString = "";
                String resultStringChuckId = "";
                if (index <= answer.results.size()) {
                    resultString = RelevantOutput.getDiscoveryResultAsText(answer, index);
                    resultStringChuckId = answer.results.get(index).chunckid;
                }
                this.resultDiscovery[index] = resultString;
                this.resultDiscoveryChunkIds[index] = resultStringChuckId;
            }
        }
    }

    public void maaSStarted(int minTokens, int maxTokens, String model, String prompt) {
        this.tsMaaSStart = getTimestamp();
        this.llmMinNewTokens = String.valueOf(minTokens);
        this.llmMaxNewTokens = String.valueOf(maxTokens);
        this.llmName = model;
        this.prompt = prompt;
    }

    public void maaSStopped(com.ibm.question_answering.api.Answer result) {
        this.tsMaaSEnd = getTimestamp();
        if (result != null) {
            if ((result.results != null) && (result.results.size() > 0) && (result.results.get(0).text.text.length > 0)) {
                this.answer = result.results.get(0).text.text[0];
            }
        }
    }

    public void setRRAmountInputDocuments(int amountDocumentsActual, int amountDocumentsMax) {
        this.sizeReRankerInputs = String.valueOf(amountDocumentsActual);
        this.rrAmountInputDocuments = String.valueOf(amountDocumentsMax);
    }

    public void setMaaSMaxAmountDocuments(int llmMaxInputDocuments) {
        this.llmMaxInputDocuments = String.valueOf(llmMaxInputDocuments);
    }

    private String getTimestamp() {
        long time = new java.util.Date().getTime();
        return String.valueOf(time);
    }

    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate.replaceAll(System.getProperty("line.separator"), "\\\\n");
    }

    void writeLastRunReadable() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(directoryAndfileNameLastRun));
            
            writer.write("## Last Run\n");
            writer.write("\n");
            writer.write("*URL:* " + this.url + "\n");
            writer.write("\n");
            writer.write("*Query:* " + this.query + "\n");
            writer.write("\n");
            writer.write("*Answer:* " + this.answer + "\n");
            writer.write("\n");
            writer.write("*Duration in Milliseconds:* " + getDuration(this.tsStart, this.tsEnd) + "\n");
            writer.write("\n");
            writer.write("\n");
            writer.write("### Watson Discovery" + "\n");
            writer.write("\n");
            writer.write("*Results (matching):* " + this.sizeDiscoveryResults + "\n");
            writer.write("\n");
            writer.write("*Results (returned):* " + this.sizeDiscoverySentResults + "\n");
            writer.write("\n");
            writer.write("*Results (max):* " + this.discoveryMaxDocuments + "\n");
            writer.write("\n");            
            writer.write("*Duration in Milliseconds:* " + getDuration(this.tsDiscoveryStart, this.tsDiscoveryEnd) + "\n");
            writer.write("\n");
            writer.write("*Result 1 chunckid or document_id:* " + this.resultDiscoveryChunkIds[0] + "\n");
            writer.write("\n");
            writer.write("<details><summary>Result 1</summary>" + this.resultDiscovery[0] + "</details>\n\n");
            writer.write("*Result 2 chunckid or document_id:* " + this.resultDiscoveryChunkIds[1] + "\n");
            writer.write("\n");
            writer.write("<details><summary>Result 2</summary>" + this.resultDiscovery[1] + "</details>\n\n");
            writer.write("*Result 3 chunckid or document_id:* " + this.resultDiscoveryChunkIds[3] + "\n");
            writer.write("\n");
            writer.write("<details><summary>Result 3</summary>" + this.resultDiscovery[2] + "</details>\n\n");
            writer.write("\n");
            if (this.reRankerUsed == true) {
                writer.write("### Re-Ranker" + "\n");
                writer.write("\n");
                writer.write("*ID:* " + this.rrId + "\n");
                writer.write("\n");
                writer.write("*Model:* " + this.rrModel + "\n");
                writer.write("\n");
                writer.write("*Duration in Milliseconds:* " + getDuration(this.tsRRStart, this.tsRREnd) + "\n");
                writer.write("\n");
                writer.write("*Input Documents Max:* " + this.rrAmountInputDocuments + "\n");
                writer.write("\n");
                writer.write("*Input Documents Actual:* " + this.sizeReRankerInputs + "\n");
                writer.write("\n");
                writer.write("*Ouput Documents Actual:* " + this.sizeReRankerResults + "\n");
                writer.write("\n");
                writer.write("*Result 1 chunckid or document_id:* " + this.resultReRankerChunkIds[0] + "\n");
                writer.write("\n");
                writer.write("<details><summary>Result 1</summary>" + this.resultReRanker[0] + "</details>\n\n");
                writer.write("*Result 2 chunckid or document_id:* " + this.resultReRankerChunkIds[1] + "\n");
                writer.write("\n");
                writer.write("<details><summary>Result 2</summary>" + this.resultReRanker[1] + "</details>\n\n");
                writer.write("*Result 3 chunckid or document_id:* " + this.resultReRankerChunkIds[2] + "\n");
                writer.write("\n");
                writer.write("<details><summary>Result 3</summary>" + this.resultReRanker[2] + "</details>\n\n");
                writer.write("\n");
            }
            writer.write("### Model as a Service" + "\n");
            writer.write("\n");
            writer.write("*MaaS Model:* " + this.llmName + "\n");
            writer.write("\n");
            writer.write("*Duration in Milliseconds:* " + getDuration(this.tsMaaSStart, this.tsMaaSEnd) + "\n");
            writer.write("\n");
            writer.write("*Min Tokens:* " + this.llmMinNewTokens + "\n");
            writer.write("\n");
            writer.write("*Max Tokens:* " + this.llmMaxNewTokens + "\n");
            writer.write("\n");
            writer.write("*Prompt:* " + this.prompt + "\n");

            writer.close();
        }
        catch (Exception e) {
        }
    }

    long getDuration(String ts1, String ts2) {
        long output = 0;
        try {
            long l1 = Long.parseLong(ts1);
            long l2 = Long.parseLong(ts2); 
            output = l2 - l1;
        } catch (Exception e) {
        }
        return output;
    }
}