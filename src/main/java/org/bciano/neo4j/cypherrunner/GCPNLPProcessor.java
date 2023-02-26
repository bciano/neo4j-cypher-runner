package org.bciano.neo4j.cypherrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GCPNLPProcessor {

    static class ParsedContent {
        private Map<String, Object> entities;
        private String text;

        ParsedContent() {

        }
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setEntities(Map<String, Object> entities) {
            this.entities = entities;
        }
    }

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
                ObjectMapper objectMapper = new ObjectMapper();

                GCPNLPExtractor nlpe = new GCPNLPExtractor();
                ParsedContent parsedContent = objectMapper.readValue(map.get("text").toString(), ParsedContent.class);
                map.put("entities", nlpe.extract(parsedContent.getText().trim()));

                String outputFile = resultFolder + "/" + ((File) f).getName() + ".json";

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
