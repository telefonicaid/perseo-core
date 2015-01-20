
## Configuration

 The configuration file is /etc/perseo-core.propeties. It a Java properties file. In this version there is not way of using other path.
```
# This file should be copied by deployment process
# into /etc/perseo-core.properties, with the appropiate permissions

# URL for invoking actions when a rule is fired
action.url = http://127.0.0.1:9090/actions/do

# Time in milliseconds (long) to "expire" a "dangling" rule
rule.max_age= 60000
```

The `action.url` field is the URL used to make a POST in the case of a rule being fired by an incoming event. The content POSTed is the JSON representation of the derived  ("complex") event.

The `rule.max_age` field is the value in milliseconds used as a threshold for the age of a rule to deleted safely in case of a PUT request over the rules resource does not contain the rule. All the rules older than rule.max_age will be deleted if they are not in the new set. If a rule is younger than `rule.max_age` it will be kept despite not being in the set sent by the PUT request.

