## API

### Rules
Resource | HTTP | Description
--- | --- | ---
/perseo-core/rules | GET | Retrieves all the rules
/perseo-core/rules/{name} | GET | Retrieves the rule with name {name}
/perseo-core/rules | POST | Inserts a new rule (or updates it if it exists)
/perseo-core/rules | PUT | Updates the whole set of rules
/perseo-core/rules/{name} | DELETE | Deletes the rule with name {name}

Each rule is represented as an plain object in JSON with a field `name` with the identifier of the rule inside the engine and a field `text` with the text of the EPL statement of the rule. The field `timeLastStateChange` has a timestamp with the last time when the state of rule changed. In this implementation, that value is the time when the rule was created. The field `state` will be `STARTED` usually, meaning the rule is working. Less frequently it could be `DESTROYED` if the rule has been removed recently and not "garbage-colleted" by the engine *yet*.

#### GET (all)

##### DESCRIPTION
Allows to retrieve all the rules

##### URL STRUCTURE
```
http://[HOST]/perseo-core/rules/
```

##### RETURNS
It returns a 200 response including a body with a JSON representation of the array containing all the rules stored in the engine (in memory). If there are not any rules, it will be an empty array. Each item in the array is the representation of the rule as described at the beginning of this section.

Example:  Two rules in the engine
```JSON
[
   {
      "text":"@Audit select \"blood_3_action\" as iotcepaction,* from ev=iotEvent.win:length(3).stat:uni(cast(cast(BloodPressure?,string),float)) output last every 3 events",
      "timeLastStateChange":1407225969166,
      "name":"blood_3",
      "state":"STARTED"
   },
   {
      "text":"@Audit select *,\"blood_4_action\" as iotcepaction,ev.BloodPressure? as Pression, ev.id? as Meter from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\"BloodMeter\")]",
      "timeLastStateChange":1407225930866,
      "name":"blood_4",
      "state":"STARTED"
   }
]
```
Example: There are not any rules
```JSON
[]
```

##### ERRORS
No errors are returned under normal operation. An empty array is returned if there is no data

##### RAW TRACE
```
> GET /perseo-core/rules HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
>
< HTTP/1.1 200 OK
< X-Powered-By: Servlet/3.1 JSP/2.3 (GlassFish Server Open Source Edition  4.0  Java/Oracle Corporation/1.7)
< Server: GlassFish Server Open Source Edition  4.0
< Content-Type: application/json;charset=ISO-8859-1
< Date: Tue, 05 Aug 2014 08:06:27 GMT
< Content-Length: 525
<
[{"text":"@Audit select \"blood_3_action\" as iotcepaction,* from ev=iotEvent.win:length(3).stat:uni(cast(cast(BloodPressure?,string),float)) output last every 3 events","timeLastStateChange":1407225969166,"name":"blood_3","state":"STARTED"},{"text":"@Audit select *,\"blood_4_action\" as iotcepaction,ev.BloodPressure? as Pression, ev.id? as Meter from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\"BloodMeter\")]","timeLastStateChange":1407225930866,"name":"blood_4","state":"STARTED"}]
```

#### GET

##### DESCRIPTION
Allows to retrieve a rule by name

##### URL STRUCTURE
```
http://[HOST]/perseo-core/rules/{name}
```
* **{name}:** Value identifying the rule

##### RETURNS
It returns a 200 response including a body with a JSON representation of the rule as described in the beginning of the section about rules. If there is not any rule with that `{name}`, a 404 is returned.

Example:  Rule exists
```JSON
{
   "text":"@Audit select \"blood_3_action\" as iotcepaction,* from ev=iotEvent.win:length(3).stat:uni(cast(cast(BloodPressure?,string),float)) output last every 3 events",
   "timeLastStateChange":1407225969166,
   "name":"blood_3",
   "state":"STARTED"
}
```

##### ERRORS
If the rule does not exist, a 404 (Not Found) error is returned with a description of the error in a `error` field inside a JSON.

Example: The rule does not exists
```JSON
{"error":"blood_34 not found"}
```

##### RAW TRACE
```
> GET /perseo-core/rules/blood_3 HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
>
< HTTP/1.1 200 OK
< X-Powered-By: Servlet/3.1 JSP/2.3 (GlassFish Server Open Source Edition  4.0  Java/Oracle Corporation/1.7)
< Server: GlassFish Server Open Source Edition  4.0
< Content-Type: application/json;charset=ISO-8859-1
< Date: Tue, 05 Aug 2014 08:47:11 GMT
< Content-Length: 241
<
{"text":"@Audit select \"blood_3_action\" as iotcepaction,* from ev=iotEvent.win:length(3).stat:uni(cast(cast(BloodPressure?,string),float)) output last every 3 events","timeLastStateChange":1407225969166,"name":"blood_3","state":"STARTED"}
```


#### POST

##### DESCRIPTION
Allows to add a  new rule to the engine

The rule must be provided as a JSON with a field `name` for the name of the rule and an field `text` containing the text of EPL statement of the rule. If a rule with the same name exists, it will be replaced if the text is different.

##### URL STRUCTURE
```
http://[HOST]/perseo-core/rules/
```

##### RETURNS
It returns a 200 response including a body with a JSON representation of the created rule as described in the beginning of the section about rules. In case of any error, it returns an 400 (Bad Request)

Example:  New rules added
```JSON
{
   "text":"@Audit select \"blood_3_action\" as iotcepaction,* from ev=iotEvent.win:length(3).stat:uni(cast(cast(BloodPressure?,string),float)) output last every 3 events",
   "timeLastStateChange":1407225969166,
   "name":"blood_3",
   "state":"STARTED"
}
```

##### ERRORS
If there is a problem processing the rule, a 400 (Bad Request) error is returned with a description of the error in a `error` field inside a JSON. Possible errors are

* missing name
* missing text for EPL statement
* incorrect JSON format, non parseable
* incorrect syntax or semantic of the EPL statement

Example: Incorrect EPL statement
```JSON
{
   "error":"Failed to resolve event type: Event type or class named 'iotEvent2' was not found [@Audit select *,\"blood_4_action\" as iotcepaction,ev.BloodPressure? as Pression, ev.id? as Meter from pattern [every ev=iotEvent2(cast(cast(BloodPressure?,String),float)>1.5 and type=\"BloodMeter\")]]"
}
```


##### RAW TRACE
```
> POST /perseo-core/rules HTTP/1.1
> host: localhost:8080
> accept: application/json
> content-type: application/json
> content-length: 391
> Connection: keep-alive
>
> {"name":"blood_4","text":"@Audit select *,\"blood_4_action\" as iotcepaction,ev.BloodPressure? as Pression, ev.id? as Meter from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\"BloodMeter\")]"}

< HTTP/1.1 200 OK
< X-Powered-By: Servlet/3.1 JSP/2.3 (GlassFish Server Open Source Edition  4.0  Java/Oracle Corporation/1.7)
< Server: GlassFish Server Open Source Edition  4.0
< Content-Type: application/json;charset=ISO-8859-1
< Date: Tue, 05 Aug 2014 10:16:55 GMT
< Content-Length: 282
<
< {"text":"@Audit select *,\"blood_4_action\" as iotcepaction,ev.BloodPressure? as Pression, ev.id? as Meter from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\"BloodMeter\")]","timeLastStateChange":1407225930866,"name":"blood_4","state":"STARTED"}
```

#### DELETE
##### DESCRIPTION
Allows to delete a rule by name

##### URL STRUCTURE
```
http://[HOST]/perseo-core/rules/{name}
```
* **{name}:** Value identifying the rule

##### RETURNS
It returns a 200 response including a body with a JSON representation of the deleted rule as described in the beginning of the section about rules. If there is not any rule with that `{name}`, a 200 is returned with an empty object in JSON. The state of the rule will be always `DESTROYED`

Example:  Rule exists
```JSON
{
   "text":"@Audit select \"blood_3_action\" as iotcepaction,* from ev=iotEvent.win:length(3).stat:uni(cast(cast(BloodPressure?,string),float)) output last every 3 events",
   "timeLastStateChange":1407225969166,
   "name":"blood_3",
   "state":"DESTROYED"
}
```
Example: The rule does not exist
```JSON
{}
```

##### ERRORS
No errors are returned under normal operation. An empty object is returned if there is no rule with the provided name.


##### RAW TRACE
```
> DELETE /perseo-core/rules/blood_2 HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
>
< HTTP/1.1 200 OK
< X-Powered-By: Servlet/3.1 JSP/2.3 (GlassFish Server Open Source Edition  4.0  Java/Oracle Corporation/1.7)
< Server: GlassFish Server Open Source Edition  4.0
< Content-Type: application/json;charset=ISO-8859-1
< Date: Tue, 05 Aug 2014 11:43:03 GMT
< Content-Length: 284
<
{"text":"@Audit select *,\"blood_2_action\" as iotcepaction,ev.BloodPressure? as Pression, ev.id? as Meter from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)>1.5 and type=\"BloodMeter\")]","timeLastStateChange":1407238983966,"name":"blood_2","state":"DESTROYED"}
```

#### PUT

##### DESCRIPTION
Allows to *replace* the set of rules of the engine

The rule set must be provided as a JSON of an **array** in which every item represents a rule and has a field `name` for the name of the rule and an field `text` containing the text of the EPL statement of the rule.

##### URL STRUCTURE
```
http://[HOST]/perseo-core/rules/
```

##### RETURNS
It returns a 200 response with a JSON of an empty object, if there was not errors. In case of any error, it returns an 400 (Bad Request)

Example: All the rules were processed correctly
```JSON
{}
```

##### ERRORS
If there is a problem processing any of the rules in the array, a 400 (Bad Request) error is returned with a description of the error in a `error` field inside a JSON. Possible errors are

* missing name
* missing text for EPL statement
* incorrect JSON format, non parseable
* incorrect syntax or semantic of the EPL statement

Example: Incorrect EPL statement
```JSON
{
   "error":"missing name"
}
```


##### RAW TRACE
```
> PUT /perseo-core/rules HTTP/1.1
> Host: localhost:8080
> User-Agent: Go 1.1 package http
> Content-Length: 821
> Accept-Encoding: gzip
>
> [{"name":"blood_2","text":"@Audit select *,\"blood_2_action\" as iotcepaction,ev.BloodPressure? as Pression, ev.id? as Meter from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)\u003e1.5 and type=\"BloodMeter\")]"},{"name":"blood_1","text":"@Audit select *,\"blood_1_action\" as iotcepaction,ev.BloodPressure? as Pression, ev.id? as Meter from pattern [every ev=iotEvent(cast(cast(BloodPressure?,String),float)\u003e1.5 and type=\"BloodMeter\")]"}]

< HTTP/1.1 200 OK
< X-Powered-By: Servlet/3.1 JSP/2.3 (GlassFish Server Open Source Edition  4.0  Java/Oracle Corporation/1.7)
< Server: GlassFish Server Open Source Edition  4.0
< Content-Type: application/json;charset=ISO-8859-1
< Date: Tue, 05 Aug 2014 11:16:38 GMT
< Content-Length: 3
<
< {}
```


### Events
Resource | HTTP | Description
--- | --- | ---
/perseo-core/events | POST | Process a new event

#### POST

##### DESCRIPTION
Send an event to the rule engine

The event is provided as a JSON representing an object. It will be sent to the `iotEvent` stream. Only a valid JSON is required. It can have any field with any name and value. The content of the object should be coherent with the content of the rules, but it can not be enforced by the system. It is responsability of the API user.

##### URL STRUCTURE
```
http://[HOST]/perseo-core/events/
```

##### RETURNS
It returns a 200 response without body. In case of any error, it returns an 400 (Bad Request)

Example:  Event sent to engine
```JSON
{
   "noticeId":"21f12c40-1ca6-11e4-a992-f1e158aa3052",
   "received":"2014-08-05T13:40:58.756Z",
   "id":"bloodm1",
   "type":"BloodMeter",
   "isPattern":"false",
   "BloodPressure":"2",
   "BloodPressure__type":"centigrade",
   "TimeInstant":"2014-04-29T13:18:05Z",
   "TimeInstant__type":"urn:x-ogc:def:trs:IDAS:1.0:ISO8601",
}
```

##### ERRORS
If the body is not a valid JSON, a 400 (Bad Request) error is returned with a description of the error in a `error` field inside a JSON.

Example: Incorrect JSON
```JSON
{"error":"Unterminated string at 9 [character 10 line 1]"}
```


##### RAW TRACE
```
> POST /perseo-core/events HTTP/1.1
> host: localhost:8080
> accept: application/json
> content-type: application/json
> content-length: 511
> Connection: keep-alive
>
> {"noticeId":"21f12c40-1ca6-11e4-a992-f1e158aa3052","received":"2014-08-05T13:40:58.756Z","id":"bloodm1","type":"BloodMeter","isPattern":"false","BloodPressure":"2","BloodPressure__type":"centigrade","numero":"4","numero__type":"centigrade","complejo":{"saludo":"....Orion el Grande!!","subcomplejo":{"x":23}},"complejo__type":"saludo ceremonial","TimeInstant":"2014-04-29T13:18:05Z","TimeInstant__type":"urn:x-ogc:def:trs:IDAS:1.0:ISO8601","falso__anidado":"jamaica.futbol","falso__anidado__type":"desconocido"}

< HTTP/1.1 200 OK
< X-Powered-By: Servlet/3.1 JSP/2.3 (GlassFish Server Open Source Edition  4.0  Java/Oracle Corporation/1.7)
< Server: GlassFish Server Open Source Edition  4.0
< Content-Type: application/json;charset=ISO-8859-1
< Date: Tue, 05 Aug 2014 13:40:58 GMT
< Content-Length: 0

```

### Log level
Resource | HTTP | Description
--- | --- | ---
/perseo-core/admin/log?level={level} | PUT | Changes log level

#### PUT

##### DESCRIPTION
Changes the log level at run time.

Level can be `DEBUG`, `INFO`, `WARN`, `WARNING`, `ERROR`, `FATAL`. Any other value will return a 400 error


##### URL STRUCTURE
```
http://[HOST]/perseo-core/admin/log?level={level}
```

##### RETURNS
It returns a 200 response without body. In case of any error, it returns an 400 (Bad Request)

Example:  Change level to INFO

```
curl -i localhost:8080/perseo-core/admin/log?level=INFO -X PUT
```

##### ERRORS
If level is not a valid value, a 400 (Bad Request) error is returned with a description of the error in a `error` field inside a JSON.

Example: Invalid log level
```JSON
{"errorMessage":"invalid log level"}
```


##### RAW TRACE
```
> PUT /perseo-core/admin/log?level=INFO HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.43.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Server: Apache-Coyote/1.1
< Content-Length: 0
< Date: Wed, 23 Mar 2016 06:57:32 GMT
<

```
