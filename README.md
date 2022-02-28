# Neo4j Cypher Script Runner

This is a POC designed to execute a sequence of cypher statements using a set of different operating parameters.

## Options

| key  | value  | default  | description
|---|---|---|---|
| mode  |  READ, WRITE, AUTOCOMMIT | WRITE | description  |  
| id | -=string=-  | none | description  |    
| type  | query, delete, iterate  | query |description  |   

## Example Sequence Definitions

This example shows how to configure a statement to run in AUTOCOMMIT mode, infinitely, with a 30 second delay after each transaction.
```
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

## Usage example

```
java -cp ./target/neo4j-cypher-runner-4.4.2.jar org.bciano.neo4j.cypherrunner.DatasetLoaderApplication databaseconfig.json sequence.json
```