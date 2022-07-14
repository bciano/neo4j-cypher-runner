package org.bciano.neo4j.cypherrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CypherScriptRunner {

    private final Map sequenceConfig;
    private Map databaseConfig;
    private Map clientConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(CypherScriptRunner.class);
    static final String TEMPLATES = "templates/";
    private final ScriptProcessor scriptProcessor = new ScriptProcessor(this);

    public CypherScriptRunner(Map sequenceConfig) {
        this.sequenceConfig = sequenceConfig;

        //set the default db config (applied to all script definitions)
        Map dbCfgMap = (Map)sequenceConfig.get("databaseConfig");
        if(dbCfgMap != null) {
            this.databaseConfig = dbCfgMap;
        }

        //set the default client config (applied to all script definitions)
        Map clientCfgMap = (Map)sequenceConfig.get("clientConfig");
        if(clientCfgMap != null){
            this.clientConfig = clientCfgMap;
        }
    }

    public void process() {
        try {
            ArrayList<Map> scriptDef = (ArrayList<Map>) sequenceConfig.get("scriptDefinitions");

            log("script definition count: " + scriptDef.size());

            Neo4jUtil neo4j = new Neo4jUtil(databaseConfig, clientConfig);

            try {
                for (int i = 0; i < scriptDef.size(); i++) {
                    Map def = scriptDef.get(i);

                    //override driver with new one if there is a script definition specific db config defined
                    if(def.containsKey("databaseConfig")){
                        Map dbCfgMap = (Map)def.get("databaseConfig");
                        if(dbCfgMap.containsKey("databaseName")){
                            log("databaseName: " + dbCfgMap.get("databaseName").toString());
                        }
                        Neo4jUtil scriptDefNeo4j = new Neo4jUtil(dbCfgMap, clientConfig);

                        try {
                            scriptProcessor.processScript(scriptDef, scriptDefNeo4j, i, def);
                        }finally {
                            scriptDefNeo4j.closeSession();
                        }
                    }else{
                        //default functionality
                        scriptProcessor.processScript(scriptDef, neo4j, i, def);
                    }
                }
            } finally {
                neo4j.closeSession();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    void log(String s) {
        LOGGER.info(s);
    }

    public Map<Object, Object> getSequenceConfig() {
        return sequenceConfig;
    }
}