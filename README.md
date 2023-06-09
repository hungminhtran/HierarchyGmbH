# Instruction
start the system: ```./run_hierarchy_gmbh.sh clean```

# network information
By default, a new network interface 192.168.195.1/25 will be created. 
Change the subnet to others if you have conflict problem.

# Security
use the init api token (with quotes) : "token123" or "token456"
To add your token, add a row into api_token_api table

# Api example

## add employee - supervisor relationship

### request
```
curl --location 'localhost:8080/add-employee-relationship' \
--header 'X-API-KEY: token456' \
--header 'Content-Type: application/json' \
--data '{
"Pete": "Nick",
"Barbara": "Nick",
"Nick": "Sophie",
"Sophie": "Jonas",
}'
```
### response
```
{
    "Jonas": {
        "Sophie": {
            "Nick": {
                "Pete": {},
                "Barbara": {}
            }
        }
    }
}
```

## get supervisor of supervisor
### request
```
curl --location 'localhost:8080/employee-supervisor-of-supervisor?employee=Barbara' \
--header 'X-API-KEY: token123'
```
### response
```
"Sophie"
```