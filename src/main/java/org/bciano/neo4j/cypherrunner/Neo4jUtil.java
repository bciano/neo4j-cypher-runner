package org.bciano.neo4j.cypherrunner;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.neo4j.driver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Hex;


public class Neo4jUtil {

    private Driver driver = null;
    private static final String UTF8_BOM = "\uFEFF";
    private final Map config;
    private final Map clientConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jUtil.class);

    public Neo4jUtil(Map config, Map clientConfig) {
        this.config = config;
        this.clientConfig = clientConfig;
        createDriver();
    }

    private void createDriver() {
        closeSession();

        Config driverConfig = buildDriverConfig(clientConfig);

        driver = GraphDatabase.driver(
                config.get("boltUri").toString(),
                AuthTokens.basic(
                        config.get("username").toString(),
                        config.get("password").toString()
                ),
                driverConfig
        );
    }

    private Config buildDriverConfig(Map<String,Object> clientConfig){

        //todo:  ugly, fix this
        Map driverProperties = new HashMap();
        long maxConnectionLifetime = 0;
        if(clientConfig.containsKey("driverProperties")){
            driverProperties = (Map) clientConfig.get("driverProperties");
            maxConnectionLifetime = Long.parseLong(driverProperties.getOrDefault("MaxConnectionLifetime", "60").toString());
        }else {
            maxConnectionLifetime = 60;
        }

        log("Driver Settings : MaxConnectionLifetime: " + maxConnectionLifetime + " minute(s)");

        return
            Config.builder()
            .withMaxConnectionLifetime(maxConnectionLifetime, TimeUnit.MINUTES) //default is 1h
            .build();
    }

    public void closeSession(){
        if(driver != null) {
            driver.close();
        }
    }

    private SessionConfig getSessionConfig() {

        SessionConfig sessionConfig;

        if(config.containsKey("databaseName")){
            String databaseName = config.get("databaseName").toString();
            sessionConfig = SessionConfig.forDatabase(databaseName);
        }else{
            sessionConfig = SessionConfig.defaultConfig();
        }
        return sessionConfig;
    }

    public List<Record> executeStatementOnlyWithResults(String stmt, String mode){

        //build a new driver
        //createDriver(); /dont need to if we arent using .run() auto-commit which doesnt do retries

        try(Session session = driver.session(getSessionConfig())) {

            if(mode.equals("READ")) {
                return session.readTransaction(tx ->
                {
                    Result result = tx.run(stmt);
                    return result.list();
                });
            }

            if(mode.equals("WRITE")) {
                return session.writeTransaction( tx ->
                {
                    Result result = tx.run(stmt);
                    return result.list();
                } );
            }

            if(mode.equals("AUTOCOMMIT")) {
                Result result = session.run(stmt);
                return result.list();
            }

        }catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

    private boolean isContainBOM(String path) {

        boolean result = false;

        byte[] bom = new byte[3];
        try(InputStream is = new FileInputStream(path)){

            // read first 3 bytes of a file.
            is.read(bom);

            // BOM encoded as ef bb bf
            String content = new String(Hex.encodeHex(bom));
            if ("efbbbf".equalsIgnoreCase(content)) {
                result = true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }



    public void executeStatementWithDataFromFile(String dataFilePath, char delimiter, boolean withoutQuoteChar, String stmt, int batchSize, String batchLog)  {

        //build a new driver
        createDriver();
        Session session = driver.session(getSessionConfig());

        if(isContainBOM(dataFilePath)){
            log("Found BOM!");
            System.out.println("Found BOM!");
        }else{
            log("No BOM.");
            System.out.println("No BOM.");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new UnicodeBOMInputStream(new FileInputStream(dataFilePath)).skipBOM(), "UTF-8")) ) {

            CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();

            System.out.println("Delimiter: " + delimiter);
            csvSchema = csvSchema.withColumnSeparator(delimiter);

            System.out.println("withoutQuoteChar: " + withoutQuoteChar);
            if(withoutQuoteChar){
                csvSchema = csvSchema.withoutQuoteChar();
            }

            MappingIterator<Map<String, String>> currIter =
                    new CsvMapper().readerWithSchemaFor(Map.class).with(csvSchema).readValues(br);

            List<Map<String, String>> rows = new ArrayList<>();
            LocalDateTime startTime = LocalDateTime.now();
            int batchNo = 0;

            while(currIter.hasNext()){

                //BUILD BATCH
                batchNo++;
                int count = 0;
                while (++count <= batchSize && currIter.hasNext()) {
                    rows.add(currIter.nextValue());
                }

                //GET NEW SESSION BEFORE LDAP CACHE EXPIRES
                if(isSessionExpired(startTime, 9)) {
                    log("\n" + new Timestamp(System.currentTimeMillis()) +
                            " : ***************** Session exceeded time limit.  Creating new session/driver. *************************");
                    startTime = LocalDateTime.now(); //reset start time
                    session.close();
                    createDriver(); //build new driver
                    session = driver.session(getSessionConfig());
                }

                //RUN SCRIPT
                Map<String, Object> params = new HashMap();
                params.put("data", rows);
                LocalDateTime batchStartTime = LocalDateTime.now();
                List<Record> results = execute(session, stmt, params);

                results.forEach(r->{
                    System.out.println( r.asMap().toString() );
                });

                if(!"".equals(batchLog) && "true".equalsIgnoreCase(batchLog) ){
                    LocalDateTime now = LocalDateTime.now();
                    Duration elapsedTime = Duration.between(batchStartTime, now);
                    int from = (((batchNo - 1) * batchSize) + 1);
                    int to = ((from + rows.size()) - 1);
                    log(new Timestamp(System.currentTimeMillis()) + " : Batch " + batchNo +" : [" + from +" - " + to + "] : " + elapsedTime.toString());
                }else{
                    System.out.print("."); //NOSONAR
                }

                rows = new ArrayList<>();
            }
            System.out.println("");
            log("");
            log("Batches: " + batchNo);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }finally{
            session.close();
        }
    }

    private List<Record> execute(Session session, String stmt, Map params){
        return execute(session, stmt, params, 0);
    }

    private List<Record> execute(Session session, String stmt, Map params, int executionCount){
        try {
            if(params != null) {
                return session.run(stmt, params).list();
            }else{
                return session.run(stmt).list();
            }
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
            if(executionCount < 5){
                executionCount++;
                log("\n" + new Timestamp(System.currentTimeMillis()) + " : *** ERROR ENCOUNTERED - RETRYING BATCH : COUNT [" + executionCount + "] IN 10 secs  ****");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException interruptedException) {
                    LOGGER.error(interruptedException.getMessage(), interruptedException);
                }
                return execute(session, stmt, params, executionCount);
            }else{
                throw e;
            }
        }
    }

    private boolean isSessionExpired(LocalDateTime startTime, int sessionExpireLength) {
        LocalDateTime now = LocalDateTime.now();
        Duration elapsedTime = Duration.between(startTime, now);
        return (elapsedTime.toMinutes() > sessionExpireLength);
    }

    public String getStatementFromFile(String statementFilePath) {

        InputStream is = null;
        try {
            is = new FileInputStream(statementFilePath);
            //is = this.getClass().getClassLoader().getResourceAsStream(statementFilePath);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        StringBuilder stmt = new StringBuilder();

        if (is != null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            String val;
            try {
                while ((val = r.readLine()) != null) {
                    stmt.append(" \n").append(val);
                }
                r.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return removeUTF8BOM(stmt.toString());
    }

    private static String removeUTF8BOM(String s) {
        if (s.contains(UTF8_BOM)) {
            System.out.println("*** REMOVING UTF8BOM FROM STATEMENT ***");
            s = s.replaceAll(UTF8_BOM, "");
        }
        return s;
    }

    private void log(String s) {
        LOGGER.info(s);
    }

    public List<Record> executeStatementOnlyWithParams(String stmt, Map params) {

        try (Session session = driver.session(getSessionConfig())) {
            return execute(session, stmt, params);
        }catch (Exception ex){
            LOGGER.error(ex.getMessage(), ex);
        }
        return new ArrayList();
    }
}