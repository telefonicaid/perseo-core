
## Configuration
You can use environment vars for configuration or a configuration file (/etc/perseo-core.properties). If the two methods are used, the environment variables will have priority over the values of the configuration file.

Moreover in case of use docker container then entrypoint is also have in account. In this case the environment variables will have priority over the values docker entrypoint, and docker entrypoint will have priority over the values of of the configuration file.



### Configure Perseo-core with environment vars

You can set the next environment variables:

PERSEO_FE_URL is the Perseo-fe URL. When a rule is fired, the actions will be sent to  `<PERSEO_FE_URL>/actions/do

Example:
```
PERSEO_FE_URL=http://perseo-fe-example:9090
```

MAX_AGE is the expiration time for dangling rules in milliseconds.

Example:
```
MAX_AGE=6000
```

INTERNAL_TIMER_MSEC_RESOLUTION is the length of the interval (resolution) of the timer thread.

Example:
```
INTERNAL_TIMER_MSEC_RESOLUTION=100
```

`ACTION_HTTP_CONNECT_TIMEOUT_MS` and `ACTION_HTTP_READ_TIMEOUT_MS` set the action HTTP connection
and response read timeouts in milliseconds. Defaults are `5000` and `10000`, respectively.
Both values must be positive integers. Environment variables override the corresponding properties
`action.http.connect_timeout_ms` and `action.http.read_timeout_ms`.

Action POSTs are not automatically retried or redirected. A failed or timed-out request may already
have reached Perseo-FE, so retrying it without deduplication can duplicate actions.


### Configure Perseo-core with configuration file

The configuration file is /etc/perseo-core.propeties. It a Java properties file. In this version there is not way of using other path.
```
# This file should be copied by deployment process
# into /etc/perseo-core.properties, with the appropiate permissions

# URL for invoking actions when a rule is fired
action.url = http://127.0.0.1:9090/actions/do

# Time in milliseconds (long) to "expire" a "dangling" rule
rule.max_age= 60000

# Length of the interval (resolution) of the timer thread
internal_timer_msec_resolution = 10

action.http.connect_timeout_ms = 5000
action.http.read_timeout_ms = 10000
```

The `action.url` field is the URL used to make a POST in the case of a rule being fired by an incoming event. The content POSTed is the JSON representation of the derived  ("complex") event.

The `rule.max_age` field is the value in milliseconds used as a threshold for the age of a rule to deleted safely in case of a PUT request over the rules resource does not contain the rule. All the rules older than rule.max_age will be deleted if they are not in the new set. If a rule is younger than `rule.max_age` it will be kept despite not being in the set sent by the PUT request.

The `internal_timer_msec_resolution` Length of the interval (resolution) of the timer thread. More detail in [esper](https://esper.espertech.com/release-9.0.0/javadoc-esper/com/espertech/esper/common/client/configuration/runtime/ConfigurationRuntimeThreading.html#setInternalTimerMsecResolution-long-)

### Legacy numeric filters

Rules are compiled using Esper's default `ADVANCED` filter index planning. If compilation fails with
`strict filter type coercion`, Perseo-core retries that rule with `BASIC` planning without changing its EPL.
This supports existing comparisons such as `cast(fillingLevel?, float) >= 0.89`, where the cast returns a
`Float` but the threshold is a `double` literal. A successful retry produces a `WARN` identifying the rule.
Other compilation errors are not retried, and a failed retry is returned as an error.

Only affected rules use `BASIC`; other rules retain advanced indexing. These rules may evaluate more
expressions per event, so monitor throughput when many rules need this compatibility path.

Numeric casts in stream filters, pattern filters and `where` expressions are also protected against empty
or whitespace-only strings: those inputs are treated as null before casting. Original event values and
the EPL returned by the rules API are preserved. Other nonnumeric strings are not silently converted;
existing cast errors for those inputs still apply. Casts in other clauses are not rewritten.
