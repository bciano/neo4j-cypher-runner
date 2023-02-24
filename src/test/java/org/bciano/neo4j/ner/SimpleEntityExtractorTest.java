package org.bciano.neo4j.ner;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimpleEntityExtractorTest {

    @Test
    void extract() {

        /**
         {
         "config":
            {
                "entityDefinitions":[]
            }
         }
        **/


        String text = "My ip address is 23.45.65.123 and my phone number is 432-456-3454, I have an SSN of 117-34-2545 and my email address is bob@someplace.com which can be found at this website: http://bobsplace.com or this one www.friends.gov";

        Map expectedResults = new HashMap();
        expectedResults.put("ipaddress", new String[]{"23.45.65.123"});
        expectedResults.put("phone", new String[]{"432-456-3454"});
        expectedResults.put("ssn", new String[]{"117-34-2545"});
        expectedResults.put("url", new String[]{"bob@someplace.com"});
        expectedResults.put("email", new String[]{"http://bobsplace.com", "www.friends.gov"});

        Map config = new HashMap();

        Map defs = new HashMap();

        config.put("entityDefinitions", defs);

        SimpleEntityExtractor extractor = new SimpleEntityExtractor();

    }
}