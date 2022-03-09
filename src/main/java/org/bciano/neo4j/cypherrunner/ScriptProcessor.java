package org.bciano.neo4j.cypherrunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ScriptProcessor {

    private final CypherScriptRunner cypherScriptRunner;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptProcessor.class);

    public ScriptProcessor(CypherScriptRunner cypherScriptRunner) {
        this.cypherScriptRunner = cypherScriptRunner;
    }

    void processScript(ArrayList<Map> scriptDef, Neo4jUtil neo4j, int i, Map def) throws IOException {

        String id = def.get("id").toString();
        String type = def.get("type").toString();
        String dataFileName = def.getOrDefault("dataFileName", "").toString();
        String scriptFileName = def.getOrDefault("scriptFileName", "").toString();
        String script = def.getOrDefault("script", "").toString();
        String batchLog = def.getOrDefault("batchlog", "").toString();
        int batchSize = Integer.parseInt(def.getOrDefault("batchSize", 1).toString());
        int threads = Integer.parseInt(def.getOrDefault("threads", 1).toString());
        //run the script x amount of times, but sleep for x seconds after each execution
        //this lets us say things like run this statement every minute for 5 minutes
        int iteratecount = Integer.parseInt(def.getOrDefault("iteratecount", 1).toString());
        int iteratesleep = Integer.parseInt(def.getOrDefault("iteratesleep", 0).toString());
        String mode = def.getOrDefault("mode", "READ").toString();

        if (!"".equals(scriptFileName)) {
            //script = neo4j.getStatementFromFile(CypherScriptRunner.TEMPLATES + scriptFileName);
            script = neo4j.getStatementFromFile(scriptFileName);
        }

        if (!"".equals(dataFileName) && cypherScriptRunner.getSequenceConfig().containsKey("datadirectorypath")) {
            dataFileName = cypherScriptRunner.getSequenceConfig().get("datadirectorypath") + dataFileName;
        }

        if (iteratecount != -1) {
            for (int x = 0; x < iteratecount; x++) {

                LocalDateTime startTime = LocalDateTime.now();

                if ("iterate".equals(type)) {
                    String iterateScriptFileName = def.getOrDefault("iterateScriptFileName", "").toString();
                    String actionScriptFileName = def.getOrDefault("actionScriptFileName", "").toString();
                    String iterateScript = def.getOrDefault("iterateScript", "").toString();
                    String actionScript = def.getOrDefault("actionScript", "").toString();

                    if (!"".equals(iterateScriptFileName)) {
                        iterateScript = neo4j.getStatementFromFile(CypherScriptRunner.TEMPLATES + iterateScriptFileName);
                    }

                    if (!"".equals(actionScriptFileName)) {
                        actionScript = neo4j.getStatementFromFile(CypherScriptRunner.TEMPLATES + actionScriptFileName);
                    }

                    String scriptOutput = ("".equals(iterateScriptFileName) ? iterateScript : iterateScriptFileName);
                    log("[" + (i + 1) + "/" + scriptDef.size() + " - " + (x + 1) + "] : START    : executeScript : " + id + " : " + type + " : " + dataFileName + " : " + batchSize + " : " + scriptOutput);
                    executeIterateScript(neo4j, iterateScript, actionScript, "WRITE");
                } else {
                    String scriptOutput = ("".equals(scriptFileName) ? script : scriptFileName);
                    log("[" + (i + 1) + "/" + scriptDef.size() + " - " + (x + 1) + "] : START    : executeScript : " + id + " : " + type + " : " + dataFileName + " : " + batchSize + " : " + scriptOutput);
                    executeScript(neo4j, dataFileName, script, batchSize, batchLog, mode);
                }

                log("[" + (i + 1) + "/" + scriptDef.size() + " - " + (x + 1) + "] : COMPLETE : executeScript : " + id + " : " + Duration.between(startTime, LocalDateTime.now()).toString());

                if (iteratesleep > 0 && x < iteratecount - 1) {
                    try {
                        Thread.sleep(iteratesleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }//end iteratecount for
        } else {
            while (true) {

                LocalDateTime startTime = LocalDateTime.now();

                if ("iterate".equals(type)) {
                    String iterateScriptFileName = def.getOrDefault("iterateScriptFileName", "").toString();
                    String actionScriptFileName = def.getOrDefault("actionScriptFileName", "").toString();
                    String iterateScript = def.getOrDefault("iterateScript", "").toString();
                    String actionScript = def.getOrDefault("actionScript", "").toString();

                    if (!"".equals(iterateScriptFileName)) {
                        iterateScript = neo4j.getStatementFromFile(CypherScriptRunner.TEMPLATES + iterateScriptFileName);
                    }

                    if (!"".equals(actionScriptFileName)) {
                        actionScript = neo4j.getStatementFromFile(CypherScriptRunner.TEMPLATES + actionScriptFileName);
                    }

                    String scriptOutput = ("".equals(iterateScriptFileName) ? iterateScript : iterateScriptFileName);
                    log("[" + (i + 1) + "/" + scriptDef.size() + "] : START    : executeScript : " + id + " : " + type + " : " + dataFileName + " : " + batchSize + " : " + scriptOutput);
                    executeIterateScript(neo4j, iterateScript, actionScript, "WRITE");
                } else {
                    String scriptOutput = ("".equals(scriptFileName) ? script : scriptFileName);
                    log("[" + (i + 1) + "/" + scriptDef.size() + "] : START    : executeScript : " + id + " : " + type + " : " + dataFileName + " : " + batchSize + " : " + scriptOutput);
                    executeScript(neo4j, dataFileName, script, batchSize, batchLog, mode);
                }

                log("[" + (i + 1) + "/" + scriptDef.size() + "] : COMPLETE : executeScript : " + id + " : " + Duration.between(startTime, LocalDateTime.now()).toString());

                if (iteratesleep > 0) {
                    try {
                        Thread.sleep(iteratesleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }//end iteratecount for
        }
    }

    private void executeIterateScript(Neo4jUtil neo4j, String iterateScript, String actionScript, String actionMode) throws IOException {

        List<Record> results = neo4j.executeStatementOnlyWithResults(iterateScript, actionMode);
        List<Map> resultList = new ArrayList();

        //build map of results
        results.forEach(record -> resultList.add(record.asMap()));

        log("Iterate Result Size: " + resultList.size());

        for(int i=0;i<resultList.size();i++){
            LocalDateTime startTime = LocalDateTime.now();

            Map rowMap = resultList.get(i);

            StringBuilder logMsg = new StringBuilder();
            //for each row, pass each map key in a separate parameter
            logMsg.append("** [" + (i+1) + "/" + resultList.size() + "] : " + rowMap + ":         ");
            List<Record> actionResults = neo4j.executeStatementOnlyWithParams(actionScript, rowMap);

            if(actionResults.isEmpty()) {
                logMsg.append(" :         NO RESULTS FOUND   ");
            }else if(actionResults.size() == 1) {
                Map row1 = actionResults.get(0).asMap();
                logMsg.append(row1);
            }else {
                logMsg.append("\n");
                actionResults.forEach( row -> System.out.println(row.asMap())); //NOSONAR
            }

            logMsg.append(" :         " + Duration.between(startTime, LocalDateTime.now()));
            log(logMsg.toString());
        }

    }

    void executeScript(Neo4jUtil neo4j, String dataFileName, String script, int batchSize, String batchLog, String mode) {

        if("".equals(dataFileName)){
            List<Record> results = neo4j.executeStatementOnlyWithResults(script, mode);
            results.forEach(record -> {
                try {
                    log(new ObjectMapper().writeValueAsString(record.asMap()));
                } catch (JsonProcessingException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            });
        }else{
            neo4j.executeStatementWithDataFromFile(dataFileName, script, batchSize, batchLog);
        }

    }

    void log(String s) {
        LOGGER.info(s);
    }
}