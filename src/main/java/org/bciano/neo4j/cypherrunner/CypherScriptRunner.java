package org.bciano.neo4j.cypherrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class CypherScriptRunner {

    private final Map sequenceConfig;
    private final Map databaseConfig;
    private final Map clientConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(CypherScriptRunner.class);
    static final String TEMPLATES = "templates/";
    private final ScriptProcessor scriptProcessor = new ScriptProcessor(this);

    public CypherScriptRunner(Map databaseConfig, Map sequenceConfig, Map clientConfig) {
        this.sequenceConfig = sequenceConfig;
        this.databaseConfig = databaseConfig;
        this.clientConfig = clientConfig;
    }

    public void process() {
        try {
            ArrayList<Map> scriptDef = (ArrayList<Map>) sequenceConfig.get("scriptDefinitions");

            log("script definition count: " + scriptDef.size());

            Neo4jUtil neo4j = new Neo4jUtil(databaseConfig, clientConfig);

            try {
                for (int i = 0; i < scriptDef.size(); i++) {
                    Map def = scriptDef.get(i);
                    scriptProcessor.processScript(scriptDef, neo4j, i, def);
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