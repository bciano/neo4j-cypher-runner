package org.bciano.neo4j.cypherrunner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.*;

public class StanfordNLPExtractor {
    public Map extract(String text) {

        StanfordCoreNLP pipeline = Pipeline.getPipeline();

        CoreDocument coreDocument = new CoreDocument(text);
        pipeline.annotate(coreDocument);

        List<CoreLabel> coreLabels = coreDocument.tokens();

        Map<String,List> results = new HashMap();

        for(CoreLabel coreLabel : coreLabels) {
            String ner = coreLabel.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            //System.out.println(coreLabel.originalText() + " = "+ ner);

            List entity = results.get(ner);
            if(entity == null){
                entity = new ArrayList();
            }
            entity.add(coreLabel.originalText());
            results.put(ner, entity);

        }
        return results;
    }
}
