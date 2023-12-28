package org.bciano.neo4j.cypherrunner.jobs;

import n10s.CommonProcedures;
import n10s.graphconfig.GraphConfig;
import n10s.graphconfig.RDFParserConfig;
import n10s.rdf.RDFProcedures;
import n10s.rdf.load.DirectStatementLoader;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class NeoSemanticsProcessor extends CommonProcedures{
//    public void process(ArrayList<Map> scriptDef, Map def) {
//
//        System.out.println("NeoSemantics Importer processing ..");
//        String url = def.getOrDefault("file", "").toString();
//        Map props = (Map)def.get("config");
//        String format = "";
//        boolean reuseCurrentTx = false;
//        String rdfFragment = "";
//
//        DirectStatementLoader statementLoader = null;
//        RDFParserConfig conf = null;
//        RDFFormat rdfFormat = null;
//
//        RDFProcedures.ImportResults importResults = new RDFProcedures.ImportResults();
//        try {
//
//            checkConstraintExist();
//
//            if(!props.containsKey("singleTx")){
//                props.put("singleTx", reuseCurrentTx);
//            }
//
//            conf = new RDFParserConfig(props, new GraphConfig(tx));
//            rdfFormat = getFormat(format);
//            statementLoader = new DirectStatementLoader(db, tx, conf, log);
//
//        } catch (CommonProcedures.RDFImportPreRequisitesNotMet e) {
//            importResults.setTerminationKO(e.getMessage());
//        } catch (GraphConfig.GraphConfigNotFound e) {
//            importResults.setTerminationKO("A Graph Config is required for RDF importing procedures to run");
//        } catch (CommonProcedures.RDFImportBadParams e) {
//            importResults.setTerminationKO(e.getMessage());
//        }
//
//        if (statementLoader != null) {
//            try {
//                parseRDFPayloadOrFromUrl(rdfFormat, url, rdfFragment, props, statementLoader);
//                importResults.setTriplesLoaded(statementLoader.totalTriplesMapped);
//                importResults.setTriplesParsed(statementLoader.totalTriplesParsed);
//                importResults.setNamespaces(statementLoader.getNamespaces());
//                importResults.setConfigSummary(props);
//                importResults.setExtraInfo(statementLoader.getWarnings());
//
//            } catch (IOException | RDFHandlerException | QueryExecutionException | RDFParseException e) {
//                //importResults.setTerminationKO(e.getMessage());
//                importResults.setTriplesLoaded(statementLoader.totalTriplesMapped);
//                importResults.setTriplesParsed(statementLoader.totalTriplesParsed);
//                importResults.setConfigSummary(props);
//            }
//        }
//
//
//    }
//
//
//
//    protected boolean isConstraintOnResourceUriPresent() {
//
////        Iterator<ConstraintDefinition> constraintIterator = tx.schema().getConstraints().iterator();
////
////        while (constraintIterator.hasNext()) {
////            ConstraintDefinition constraintDef = constraintIterator.next();
////            if (constraintDef.isConstraintType(ConstraintType.UNIQUENESS) &&
////                    constraintDef.getLabel().equals(Label.label("Resource")) &&
////                    sizeOneAndNameUri(constraintDef.getPropertyKeys().iterator())) {
////                return true;
////            }
////        }
////        return false;
//        return true;
//    }
}
