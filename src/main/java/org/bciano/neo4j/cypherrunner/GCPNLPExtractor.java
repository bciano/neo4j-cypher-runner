package org.bciano.neo4j.cypherrunner;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Key;
import com.google.cloud.language.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GCPNLPExtractor {

    class GcpUrl extends GenericUrl {
        GcpUrl(String encodedUrl) {
            super(encodedUrl);
        }
        @Key
        public String key;
    }

    private static final String BASE_URL = "https://language.googleapis.com/v1";
    private static final String ENTITIES_ENDPOINT = "/documents:analyzeEntities";
    // Insert your GCP API Key here
    // To generate API Key run gcloud alpha services api-keys create --display-name=gg-api-key from your CLI
    private static final String API_KEY = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(GCPNLPExtractor.class);

    private String analyzeEntitiesHttp(String text) throws IOException {
        GcpUrl url = new GcpUrl(BASE_URL + ENTITIES_ENDPOINT);
        url.key = API_KEY;

        String payload = String.format("{'encodingType': 'UTF8','document': { 'type': 'PLAIN_TEXT', 'content': '%1$s' } }", text);
        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
        HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString(null, payload));
        HttpHeaders headers = request.getHeaders();
        headers.setContentType("application/json");
        HttpResponse response = request.execute();

        log("Response from GCP API Call: " + response.getStatusCode());
        String result = response.parseAsString();
        log(result);
        return result;
    }

    private List analyzeEntities(String text) throws Exception {
        List<Entity> results = new ArrayList<>();

        try (LanguageServiceClient languageServiceClient = LanguageServiceClient.create()) {
            Document document = Document.newBuilder()
                    .setContent(text)
                    .setType(Document.Type.PLAIN_TEXT)
                    .build();

            AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder()
                    .setDocument(document)
                    .setEncodingType(EncodingType.UTF8)
                    .build();

            AnalyzeEntitiesResponse response = languageServiceClient.analyzeEntities(request);

            // Output results
            for (Entity entity : response.getEntitiesList()) {
                log(String.format("Entity: %s", entity.getName()));
                log(String.format("Salience: %.3f", entity.getSalience()));
                log("Metadata: ");
                for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
                   log(String.format("%s : %s", entry.getKey(), entry.getValue()));
                }
                for (EntityMention mention : entity.getMentionsList()) {
                    log(String.format("Begin offset: %d", mention.getText().getBeginOffset()));
                    log(String.format("Content: %s", mention.getText().getContent()));
                    log(String.format("Type: %s", mention.getType()));
                }
            }
            results.addAll(response.getEntitiesList());
            return results;
        }
    }
    public String extract(String text) throws Exception {
//        Map<String, List> results = new HashMap<>();
//        return analyzeEntities(text);
        return analyzeEntitiesHttp(text);
//        return results;
    }

    private static void log(String msg){
        LOGGER.info(msg);
    }
}
