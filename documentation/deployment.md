# Deployment

### Dependencies

Perseo-core requires Java 17 or later and a servlet container for its WAR file, in particular Tomcat 9.
Esper 9.0.0 requires Java 17 for both building and running the application.
Tomcat 10 and later use Jakarta Servlet and are not compatible with the current `javax.servlet` application.

### Installation using Docker

#### Build the image (optional)

You only need to do this once in your system:

    docker build -t perseo .

The parameter `-t perseo` gives the image a name. This name could be anything, or even include an organization like
`-t org/fiware-perseo`. This name is later used to run the container based on the image.

If you want to know more about images and the building process you can find it in [Docker's documentation](https://docs.docker.com/userguide/dockerimages/).

#### Run the container

The image runs as the unprivileged user `1000:1000`. The WAR is expanded while building the image and the web application files are read-only. Use a read-only root filesystem and temporary writable mounts only for Tomcat runtime data and logs:

```bash
docker run -d --name perseo1 \
    --read-only \
    --cap-drop=ALL \
    --security-opt=no-new-privileges \
    --tmpfs /tmp:rw,nosuid,nodev,size=64m,uid=1000,gid=1000,mode=1770 \
    --tmpfs /usr/local/tomcat/temp:rw,nosuid,nodev,size=64m,uid=1000,gid=1000,mode=1770 \
    --tmpfs /usr/local/tomcat/work:rw,nosuid,nodev,size=128m,uid=1000,gid=1000,mode=1770 \
    --tmpfs /usr/local/tomcat/logs:rw,nosuid,nodev,size=32m,uid=1000,gid=1000,mode=1770 \
    --tmpfs /var/log/perseo:rw,nosuid,nodev,size=16m,uid=1000,gid=1000,mode=1770 \
    -p 8080:8080 perseo
```

As a result of this command, there is a PERSEO listening on port 8080 on localhost. Try to see if it works now with

    curl localhost:8080/perseo-core/version
    
To get access to the log file of Perseo Core you can run:

```
docker exec perseo1 tail -f /var/log/perseo/perseo-core.log
```

The log directory is temporary in this example; use `docker logs perseo1` for container logs. Do not mount Docker/container-runtime sockets, Kubernetes credentials, or other secrets unless the deployment has a documented need for them. The Perseo-core image does not require these mounts.

#### Run the container together with Perseo Front-End

To connect it to Perseo Front-End, use the same hardening options and set its URL explicitly:

```bash
docker run -d --name perseo_core -h perseocore \
    --read-only \
    --cap-drop=ALL \
    --security-opt=no-new-privileges \
    --tmpfs /tmp:rw,nosuid,nodev,size=64m,uid=1000,gid=1000,mode=1770 \
    --tmpfs /usr/local/tomcat/temp:rw,nosuid,nodev,size=64m,uid=1000,gid=1000,mode=1770 \
    --tmpfs /usr/local/tomcat/work:rw,nosuid,nodev,size=128m,uid=1000,gid=1000,mode=1770 \
    --tmpfs /usr/local/tomcat/logs:rw,nosuid,nodev,size=32m,uid=1000,gid=1000,mode=1770 \
    --tmpfs /var/log/perseo:rw,nosuid,nodev,size=16m,uid=1000,gid=1000,mode=1770 \
    -p 8080:8080 \
    -e PERSEO_FE_URL=http://perseo-frontend:9090 \
    telefonicaiot/perseo-core:master
```

A few points to consider:

* The name `perseo_core` can be anything and doesn't have to be related to the name given to the docker image built.
* In `-p 8080:8080` the first value represents the port to listen in on localhost. If you wanted to run a second Perseo on your machine
you should change this value to something else, for example `-p 8081:8080`.
* The command does not mount host sockets or credentials. Do not add mounts for `/var/run/docker.sock`, container-runtime sockets, Kubernetes service-account tokens, or secrets unless strictly required.
* If you have previously built your own image you can run the same command as above but substituting `telefonicaiot/perseo-core:master` by the
name given at image build time (`-t` option)

#### Kubernetes security settings

For Kubernetes, use a pod security context equivalent to the Docker options and do not mount host sockets or credentials. Disable automatic ServiceAccount token injection unless the workload explicitly needs Kubernetes API access:

```yaml
automountServiceAccountToken: false
securityContext:
    runAsNonRoot: true
    runAsUser: 1000
    runAsGroup: 1000
    fsGroup: 1000
    allowPrivilegeEscalation: false
    readOnlyRootFilesystem: true
    capabilities:
        drop: ["ALL"]
    seccompProfile:
        type: RuntimeDefault
```

With `readOnlyRootFilesystem`, provide writable `emptyDir` mounts for `/tmp`, `/usr/local/tomcat/temp`, `/usr/local/tomcat/work`, `/usr/local/tomcat/logs`, and `/var/log/perseo`. These locations contain temporary Tomcat data and container logs, not persistent application state.

### Installation from Sources

Deployment requires Tomcat 9 running on Java 17 or later. The basic steps for deploying it would be

* Put the war into the webapps directory of the Tomcat server
* Use [environment variables](config.md#configure-perseo-core-with-environment-vars) or copy the [configuration file](config.md#configure-perseo-core-with-configuration-file) (in src/main/resources/perseo-core.properties) to /etc and update the values in the configuration file.

* Verify that the directory for logs exists and has the proper permissions. The log is generated in the /var/log/perseo/perseo.log

Building the .war file requires Maven 3.6.3 or later and JDK 17 or later.
Check that `mvn -version` reports the intended JDK. Build and run the tests by executing
```
    mvn clean verify
```
from the root directory of the project.
The WAR file is generated at `perseo-main/target/perseo-main-1.13.0-SNAPSHOT.war`.

#### Undeployment
In order to undeploy the proxy just remove the .war and the directoryin webapps/.


### Log Rotation (not apply to Docker container)
Independently of how the service is installed, the log files will need an external rotation (e.g.: the logrotate command) to avoid disk full problems.

Logroate example file can be found in [`etc` directory in this repository](../etc). This example is configured to rotate every day and whenever the log file size is greater than 100MB (checked very 30 minutes by default):
* For daily rotation: /etc/logrotate.d/logrotate-perseo-daily: which enables daily log rotation
* For size-based rotation:
    * /etc/sysconfig/logrotate-perseo-size: in addition to the previous rotation, this file ensures log rotation if the log file grows beyond a given threshold (100 MB by default)
    * /etc/cron.d/cron-logrotate-perseo-size: which ensures the execution of etc/sysconfig/logrotate-perseo-size at a regular frecuency (default is 30 minutes)
