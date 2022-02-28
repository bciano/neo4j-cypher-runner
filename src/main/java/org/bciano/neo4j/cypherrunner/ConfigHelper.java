package org.bciano.neo4j.cypherrunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class ConfigHelper {

    public Map getConfig(String s) throws URISyntaxException, IOException {
        return new ObjectMapper().readValue(
                new File(this.getClass().getClassLoader().getResource("templates/sequence.json").toURI()), Map.class);
    }

}