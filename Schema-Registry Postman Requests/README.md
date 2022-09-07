1. I've created this collection to get an understanding of contexts in Schema Registry
2. The aim is to be able to register the same schema across different contexts within a Schema Registry(SR) cluster. In Confluent Cloud terms, this SR has an environment scope
3. Replace the Basic auth credentials  `{{SR_basic_auth_key}}` and `{{SR_basic_auth_key_value}}` with your SR Key/Value credentials
4. Replace {{SR_host}} with your SR cluster URL - Don't include the `https://`, your URL should looks like `https://{{SR_host}}/`
5. We will register the below schema across default,emea and us-east contexts. The schema ID is consistent across all the contexts if this is the only JSON created
```
{
	"test1": {
		"type": "integer",
		"description": "The integer type is used for integral numbers."
	},
	"test2": {
		"type": "number",
		"description": "The number type is used for any numeric type, either integers or floating point numbers."
	},
	"test3": {
		"type": "string",
		"description": "The string type is used for strings of text."
	}
}
```
 
