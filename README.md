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
Assuming jar and configuration json filea are all in the same directory:
```
java -cp ./neo4j-cypher-runner-4.4.2.jar org.bciano.neo4j.cypherrunner.DatasetLoaderApplication ./databaseconfig.json ./sequence.json
```

## Example databaseconfig.json file
```
{
  "boltUri": "bolt://localhost:7687",
  "username":"neo4j",
  "password":"neo4j"
}
```

## Example sequence.json file
```
{
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
```
"clientConfig": 
  {
    "driverProperties": {
      "MaxConnectionLifetime": "1"
    }
  }
```

## Example Sequence Definitions
How to configure a statement to read a CSV file, and pass each batch to the script.
```
{
 "id": "APP_FROM_SOR_Delete",
 "type": "write", 
 "batchSize": 20000, 
 "dataFileName": "Users.csv",  
 "scriptFileName": "add_users.cql"
}
```

Alternative to apoc.periodic.iterate
```
{
    "id": "APP_FROM_SOR_Delete", 
    "type": "iterate", 
    "batchLog": true, 
    "iterateScript": "MATCH (m) RETURN id(m) as mid", 
    "actionScript": "MATCH (m) WHERE id(m) = mid RETURN id(m)"
}
```

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

