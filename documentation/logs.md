# Logs

Logs have levels `FATAL`, `ERROR`, `INFO` and `DEBUG`. The log level must be set in the configuration file `log4j.xml`

```xml
		<priority value="info" />
```

Each log line contains several fields of the form *name*`=` *value*, separated by `|`
* `time` time of the log
* `lvl` log level
* `corr` correlator from the incoming request that caused the current transaction. It allows trace a global operation through several systems. If not present as field header in the incoming request, the internal transaction id will be used.
* `trans` internal transaction id
* `op`  description of the operation being done (`doPost`, `doPut`, `updateAll`, ... ).
* `msg` message

```
time=2015-01-15T11:53:37.293CET | lvl=INFO | corr=164f024d-0478-4c23-8c16-b70e6ada3eb9 | trans=164f024d-0478-4c23-8c16-b70e6ada3eb9 | op=updateAll | comp=perseo-core | msg=es.tid.fiware.perseo.RulesManager[205] : identical statement: blood_rule_email@/unknowns
```

Logs for errors can show additional info in the message, giving a hint of the root cause of the problem.

 In order to have logs that can enable alarms being raised and ceased, `INFO` level should be set in the configuration file.
With `ERROR` level, alarms could be raised but not ceased.

The log level can be changed at run-time, with an HTTP PUT request

```
 curl --request PUT <host>:<port>/perseo-core/admin/log?level=<FATAL|ERROR|WARNING|WARN|INFO|DEBUG>
 ```

# Alarms

Alarm levels

* **Critical** - The system is not working
* **Major** - The system has a problem that degrades the service and must be addressed
* **Warning** - It is happening something that must be notified

Alarms will be inferred from logs typically. For each alarm, a 'detection strategy' and a 'stop condition' is provided (note that the stop condition is not shown in the next table, but it is included in the detailed description for each alarm below). The conditions are use for detecting logs that should raise the alarm and cease it respectively. Level (`lvl`), operation  (`op`) and message (`msg`) are considerated to evaluate the condition. The message in a condition is considerated to be a prefix of the possible message in the log. It is  recommended to  eliminate starting spaces in each field, to avoid missing a log that should meet the condition in other case.

## Alarm conditions

Alarm ID | Severity | Description | Action
---|---|---|---|
[ACTION](#action)|Major|Asking perseo-fe to execute actions is failing |Check HTTP connectivity to perseo from perseo-core, as set in the config file.

<a name="action"></a>
### Alarm ACTION

**Severity**: Major

**Detection strategy:** `lvl`:`ERROR` `op`:`DoHTTPPost`, `msg`: `action response is not OK`

**Stop condition**: `lvl`:`INFO` `op`:`DoHTTPPost` `msg`:`action response body: {"error":null,"data":null}`

**Description**: Asking perseo-fe to execute actions is failing.

**Action**: Check HTTP connectivity to perseo from perseo-core, as set in the config file.
