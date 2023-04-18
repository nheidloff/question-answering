# MaaS

Run with Docker: 

```
cd maas-mock
docker build -f src/main/docker/Dockerfile.jvm -t maas-mock .
docker run -i --rm -p 8082:8082 maas-mock
```

Run with Java and Maven:

```
cd maas-mock
mvn package
mvn quarkus:dev
```

REST API:

```
QUERY="Complete the next sentence: Together we"
QUERY="When and for how much did IBM acquire Red Hat?"
QUERY="Can Watson NLP run as containers on multi cloud environments?"
curl -v POST http://localhost:8082/v1/generate \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer 123456789' \
  -d '{ 
    "model_id": "google/flan-ul2", 
    "inputs": [ 
        "'$QUERY'"
    ], 
    "parameters": { 
      "temperature": 0, 
      "min_new_tokens": 1, 
      "max_new_tokens": 5 
    } 
  }' | jq '.'
```

http://localhost:8082/q/swagger-ui/


