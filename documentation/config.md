
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
```

The `action.url` field is the URL used to make a POST in the case of a rule being fired by an incoming event. The content POSTed is the JSON representation of the derived  ("complex") event.

The `rule.max_age` field is the value in milliseconds used as a threshold for the age of a rule to deleted safely in case of a PUT request over the rules resource does not contain the rule. All the rules older than rule.max_age will be deleted if they are not in the new set. If a rule is younger than `rule.max_age` it will be kept despite not being in the set sent by the PUT request.

The `internal_timer_msec_resolution` Length of the interval (resolution) of the timer thread. More detail in [esper](http://esper.espertech.com/release-8.4.0/javadoc-esper/com/espertech/esper/common/client/configuration/runtime/ConfigurationRuntimeThreading.html#setInternalTimerMsecResolution-long-)
