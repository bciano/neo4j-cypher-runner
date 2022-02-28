package org.bciano.neo4j.cypherrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class DatasetLoaderApplication {

    private static Map sequenceConfig;
    private static Map databaseConfig;
    private static final String DEFAULT_SEQUENCE_FILE = "templates/sequence.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetLoaderApplication.class);

    public static void main(String[] args){

        LocalDateTime startTime = LocalDateTime.now();

        String dbConfigFilePath = args[0];
        String sequenceConfigFilePath = args[1];

        log("Start Time: " + new Timestamp(System.currentTimeMillis()));
        log("dbConfigFilePath      : " + dbConfigFilePath);
        log("sequenceConfigFilePath: " + sequenceConfigFilePath);

        try {
            databaseConfig = new ObjectMapper().readValue(new File(dbConfigFilePath), Map.class);
            if (sequenceConfigFilePath == null){
                sequenceConfig = new ConfigHelper().getConfig(DEFAULT_SEQUENCE_FILE);
            }else{
                sequenceConfig = new ObjectMapper().readValue(new File(sequenceConfigFilePath), Map.class);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        if(databaseConfig.containsKey("databaseName")){
            log("databaseName: " + databaseConfig.get("databaseName").toString());
        }

        Map clientConfig = new HashMap();
        if(sequenceConfig.containsKey("clientConfig")){
            clientConfig = (Map)sequenceConfig.get("clientConfig");
        }

        String dataDirectoryPath = (args.length==3?args[2]:null);
        if(dataDirectoryPath != null){
            sequenceConfig.put("datadirectorypath", dataDirectoryPath);
        }

        CypherScriptRunner loader = new CypherScriptRunner(databaseConfig, sequenceConfig, clientConfig);
        loader.process();

        log("End Time: " + new Timestamp(System.currentTimeMillis()));
        log("Total Time: " + Duration.between(startTime, LocalDateTime.now()).toString());
    }

    private static void log(String msg){
        LOGGER.info(msg);
    }

}