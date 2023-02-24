package org.bciano.neo4j.cypherrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GCPNLPProcessor {

    public void process(ArrayList<Map> scriptDef, Map def) {
        System.out.println("Doc Extract processing ..");

        String docFolder = def.getOrDefault("docFolder", "").toString();
        String resultFolder = def.getOrDefault("resultFolder", "").toString();

        Set t = Stream.of(new File(docFolder).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getAbsoluteFile)
                .collect(Collectors.toSet());
        t.forEach(f ->
        {
            System.out.println("File: " +f);

            FileInputStream fis = null;

            try {

                fis = new FileInputStream((File)f);
                Map<String, Object> map = new HashMap<String, Object>();

                String content = new Tika().parseToString((File)f);
                map.put("text", content.replaceAll("\n|\r|\t", " "));
                //System.out.println("TEXT: " + map.get("text"));

                GCPNLPExtractor nlpe = new GCPNLPExtractor();
                Map entities = nlpe.extract((String)map.get("text"));
                map.put("entities", entities);

                String outputFile = resultFolder + "/" + ((File) f).getName() + ".json";
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(new File(outputFile), map);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
