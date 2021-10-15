Kafka Connect SMT to add a search a String and replace all occurrences of that String in the Value field of a message

Example on how to add to your connector:
```
transforms=search.replace
transforms.search.replace.type=com.github.angoothachap.kafka.connect.smt.SearchReplaceField$Value
```


!! IMPORTANT !!
1. This will only work on values that are of String type for a schemaless message and all String fields in a message value with a backing schema
2. Implement reflections to accommodate all Data types