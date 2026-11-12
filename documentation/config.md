
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


The following compatibility and hardening options are opt-in and default to `false`:

| Environment variable | Effect when `true` |
| --- | --- |
| `PERSEO_ESPER_BASIC_FILTER_FALLBACK_ENABLED` | Retry strict filter-coercion failures with Esper `BASIC` filter index planning. |
| `PERSEO_ESPER_EMPTY_NUMERIC_CASTS_ENABLED` | Treat empty and whitespace-only strings as null before numeric casts in filters. |
| `PERSEO_ACTION_HTTP_HARDENING_ENABLED` | Apply action HTTP timeouts and disable redirects, preventing implicit POST replay. |
| `PERSEO_RULE_UPDATE_SAFETY_ENABLED` | Preserve the current rule on compilation failure and continue bulk updates while reporting per-rule errors. |

Use the literal values `true` or `false` (case-insensitive). Invalid values make configuration reload fail.
The flags are read at startup/configuration reload; restart the service after changing container environment variables.

When action HTTP hardening is enabled, `ACTION_HTTP_CONNECT_TIMEOUT_MS` and
`ACTION_HTTP_READ_TIMEOUT_MS` set the connection and response-read timeouts in milliseconds. Defaults are
`5000` and `10000`; both must be positive integers. Environment variables override
`action.http.connect_timeout_ms` and `action.http.read_timeout_ms` in the properties file.

HTTP streams and connections are closed in either mode. Action POSTs are not explicitly retried. With
hardening enabled, redirects are not followed; without it, the legacy redirect behavior is retained. A
failed or timed-out request may already have reached Perseo-FE, so automatic retries could duplicate actions.

To extend the built-in EPL allowlist without restarting Perseo-core, set `PERSEO_EPL_ALLOWED_EXTRA_CLASSES_FILE` to a mounted UTF-8 text file containing one fully qualified Java class name per line. Package names, wildcards, and unqualified class names are rejected. Blank lines and lines beginning with `#` are ignored. Classes and packages blocked by the security policy cannot be enabled through this file. Invalid entries and read errors are logged and fail closed for classes supplied by the file; the built-in allowlist remains available.

Example:
```
PERSEO_EPL_ALLOWED_EXTRA_CLASSES_FILE=/etc/perseo-core/epl-allowed-classes.txt
```

For example, `/etc/perseo-core/epl-allowed-classes.txt` can contain:
```text
java.util.Date
java.util.TimeZone
```

Rules that are already compiled are not revalidated when the file changes. After removing a class, undeploy and provision again any existing rules that used it.



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

With `PERSEO_ESPER_BASIC_FILTER_FALLBACK_ENABLED=false`, rules use Esper's default `ADVANCED` filter index
planning and strict coercion failures are returned to the caller. When the flag is true, compilation retries
if it fails with `strict filter type coercion`, using `BASIC` planning without changing the EPL.
This supports existing comparisons such as `cast(fillingLevel?, float) >= 0.89`, where the cast returns a
`Float` but the threshold is a `double` literal. A successful retry produces a `WARN` identifying the rule.
Other compilation errors are not retried, and a failed retry is returned as an error.

Only affected rules use `BASIC`; other rules retain advanced indexing. These rules may evaluate more
expressions per event, so monitor throughput when many rules need this compatibility path.

With `PERSEO_ESPER_EMPTY_NUMERIC_CASTS_ENABLED=true`, numeric casts in stream filters, pattern filters and
`where` expressions are protected against empty or whitespace-only strings; otherwise Esper receives the
original expression. When enabled, original event values and the EPL returned by the rules API are preserved.
Other nonnumeric strings are not silently converted; existing cast errors for those inputs still apply.
Casts in other clauses are not rewritten.

With `PERSEO_RULE_UPDATE_SAFETY_ENABLED=false`, rule replacement and bulk updates retain their legacy
fail-fast behavior. When true, an existing rule is compiled before its previous deployment is removed. If
deployment fails, Perseo-core attempts to restore the prior EPL and listener. Bulk updates continue after
individual failures, return HTTP 400 with a `details` array, keep successful changes, and skip cleanup of
rules absent from the request. This is best-effort, not an atomic transaction; window contents and timer
state are not restored.
