# Neo4j Cypher Script Runner
This is a POC designed to execute a sequence of cypher statements using a set of different operating parameters.

# Pre-requisites
- project can be built and ran using java 8 or 11

## Client Config Options
| key                   | value                  | default | description |
|-----------------------|------------------------|---------|-------------|
| MaxConnectionLifetime | -=int=-                | 60      | description |  
| uniqueScriptDriver    | -=boolean=-            | true    | description |    

## Script Config Options
| key  | value                   | default | description |
|------|-------------------------|---------|-------------|
| mode | READ, WRITE, AUTOCOMMIT | WRITE   | description |  
| id   | -=string=-              | none    | description |    
| type | query, delete, iterate  | query   | description |   

## Usage example
Assuming jar and configuration json files are all in the same directory:
```
java -cp ./neo4j-cypher-runner-4.4.11.jar org.bciano.neo4j.cypherrunner.DatasetLoaderApplication ./sequence.json
```

## Example sequence.json file
```json
{
  "databaseConfig" : {
    "boltUri": "bolt://localhost:7687",
    "username":"neo4j",
    "password":"neo4jneo4j",
    "databaseName": "neo4j"
  },
  "clientConfig": {
    "driverProperties": {
      "MaxConnectionLifetime": "1"
    }
  },
  "scriptDefinitions": [
    {
      "id": "example",
      "type": "query",
      "batchSize": 1000,
      "script": "MATCH (n) RETURN count(n)",
      "mode": "READ"
    }
  ]
}
```

## Client Config section
clientConfig is optional.
The only supported client config property right now is MaxConnectionLifetime.  This allows the client to specify how long to keep a connection in the connection pool before removing it and replacing it.
A value of 1 = 1second,  3600 = 1 hour.
```json
"clientConfig": 
  {
    "driverProperties": {
      "MaxConnectionLifetime": "1"
    }
  }
```

## Example Sequence Definitions
How to configure a statement to read a CSV file, and pass each batch to the script.
```json
{
 "id": "APP_FROM_SOR_Delete",
 "type": "write", 
 "batchSize": 20000, 
 "dataFileName": "Users.csv",  
 "scriptFileName": "add_users.cql"
}
```

Alternative to apoc.periodic.iterate
```json
{
    "id": "APP_FROM_SOR_Delete", 
    "type": "iterate", 
    "batchLog": true, 
    "iterateScript": "MATCH (m) RETURN id(m) as mid", 
    "actionScript": "MATCH (m) WHERE id(m) = mid RETURN id(m)"
}
```

This example shows how to configure a statement to run in AUTOCOMMIT mode, infinitely, with a 30 second delay after each transaction.
```json
{
  "id": "example",
  "type": "query",
  "batchSize": 1000,
  "script": "MATCH (n) RETURN count(n)",
  "iteratecount": -1,
  "iteratesleep": 30000,
  "mode": "AUTOCOMMIT"
}
```

## Default Data Directory Path location configuration
dataDirectoryPath can also be specified as a second parameter.

example:
```
java -cp ./neo4j-cypher-runner-4.4.2.jar org.bciano.neo4j.cypherrunner.DatasetLoaderApplication ./sequence.json /tmp/data
```

## Unstructured data text extraction using Apache Tika
This component will extract text from office files, pdfs, etc. and generate a json document containing a property value equal to the test string.
```json
{
    "id" : "",
    "type": "doc2json",
    "docFolder": "data/docs",
    "resultFolder" : "data/docs-extracted"
}
```

## Entity Extraction using Stanford-NER
This component <TBD>
```json
{
    "id" : "",
    "type": "ner-extract",
    "docFolder": "./data/docs-extracted",
    "resultFolder" : "./data/ner-extracted"
}
```

## Entity Extraction using GCP NLP ?
This
```json
,
{
"id" : "",
"type": "gcp-nlp-extract",
"docFolder": "data/docs-extracted",
"resultFolder" : "data/gcp-ner-extracted",
"gcpkey" : ""
}
```

## NEW IDEAS

### doc2json (text extractor)
This component would iterate through each of the docs in the docFOlder, use Tika to extract the text, and write a json version to the docs-extracted folder
-   add a config to define the output file json format, or the filename
```json
{ 
    "id" : "",
    "type": "doc2json",
    "docFolder": "./data/docs",
    "resultFolder" : "./data/docs-extracted"
}
```

### ner-extract (entity extractor)
This component would take a text string and send to NEW  which would return a list of entities that would be stored in a new json along with the original text
-   add config to define the json input file property ie.  inputJSONProperty="doc.metadata.description"
```json
{ 
    "id" : "",
    "type": "ner-extract",
    "docFolder": "./data/docs-extracted",
    "resultFolder" : "./data/ner-extracted"
}
```

### add support for parallel jobs
- premise: use java concurrency to have jobs run in parallel
```json
"scriptDefinitions":
  [

    {
      "id" : "",
      "type": "doc2json",
      "docFolder": "data/docs",
      "resultFolder" : "data/docs-extracted"
    },
    {
        "id":"",
        "type": "parallel-jobs"
        "scriptDefinitions":
            [
                {
                  "id" : "",
                  "type": "ner-extract",
                  "docFolder": "./data/docs-extracted",
                  "resultFolder" : "./data/ner-extracted"
                },
                {
                  "id" : "",
                  "type": "gcp-nlp-extract",
                  "docFolder": "data/docs-extracted",
                  "resultFolder" : "data/gcp-ner-extracted",
                  "gcpkey" : ""
                }
            ]
    }
  ]

```


### Change definition of jobs
instead of using a unique name for a job, joba should be defined using their class name, and then instantiated within the code using reflection.  THis will allow for us to dynamically provision new components by adding their classes to the classpath, if they were to have been developed by someone else in a different project.
```json
{
  "id": "",
  "jobClassName": "com.bciano.ps.cypherrunner.jobs.ExampleJob",
  
  "config": {
    "docFolder": "./data/docs-extracted",
    "resultFolder": "./data/ner-extracted"
  }
}
````

## Neo-semantics RDF importer
This will import the rdf file into neo4j using neo-semantics libraries
```json
{
    "id" : "",
    "type": "neosemantics-importer",
    "file": "data/ontology1/salmon.ttl",
    "config":{
    //add the neo-semantics function options here
    }
}
```

Definitely need to get the optional dependencies out of the main project jar (its 580mb, currently)