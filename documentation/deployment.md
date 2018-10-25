# Deployment

### Dependencies

The only dependecy for perseo-core is the servlet engine container for its WAR file, in particular, a Tomcat 7.

### Installation using Docker

#### Build the image (optional)

You only need to do this once in your system:

	docker build -t perseo .

The parameter `-t perseo` gives the image a name. This name could be anything, or even include an organization like
`-t org/fiware-perseo`. This name is later used to run the container based on the image.

If you want to know more about images and the building process you can find it in [Docker's documentation](https://docs.docker.com/userguide/dockerimages/).

#### Run the container

The following line will run the container, using a manually built image (see above),
exposing port `8080`, give it a name -in this case `perseo1`, and present a bash prompt:

		docker run -d --name perseo1 -p 8080:8080 perseo

As a result of this command, there is a PERSEO listening on port 8080 on localhost. Try to see if it works now with

	curl localhost:8080/perseo-core/version
	
To get access to the log file of Perseo Core you can run:

```
docker exec perseo1 tail -f /var/log/perseo/perseo-core.log
```

#### Run the container together with Perseo Front-End

The following line will run the container, from Dockerhub, exposing port `8080`, give it a name -in this case `perseo_core` (hostname `perseocore`),
and binding it to a [Perseo Front-End](https://github.com/telefonicaid/perseo-fe)
instance (hostname `perseo-frontend`) listening on port 9090.

		docker run -d --name perseo_core -h perseocore -p 8080:8080 telefonicaiot/perseo-core:master -perseo_fe_url perseo-frontend:9090

A few points to consider:

* The name `perseo_core` can be anything and doesn't have to be related to the name given to the docker image built.
* In `-p 8080:8080` the first value represents the port to listen in on localhost. If you wanted to run a second Perseo on your machine
you should change this value to something else, for example `-p 8081:8080`.
* Anything after the name of the container image (in this case `telefonicaiot/perseo-core:master`) is interpreted as a parameter for the Perseo CEP. 
* If you have previously built your own image you can run the same command as above but substituting `telefonicaiot/perseo-core:master` by the
name given at image build time (`-t` option)

### Installation from RPM

This project provides the specs to create the RPM Package for the project, that may (in the future) be installed in a
package repository.

To generate the RPM, checkout the project to a machine with the RPM Build Tools installed, and, from the `rpm/` folder,
execute the following command:

```
./create-rpm.sh 1 0.1
```

The create-rpm.sh script uses the following parameters:

* Perseo-core release (1 in the example above), tipically set with the commit number corresponding to the RPM.
* Perseo-core version (0.1 in the example above), which is the base version of the software

This command will generate some folders, including one called RPMS, holding the RPM created for every architecture
(x86_64 is currently generated).

In order to install the generated RPM from the local file, use the following command:

```
yum --nogpgcheck localinstall  perseo-core-0.1-1.x86_64.rpm
```

It should automatically download all the dependencies provided they are available (Node.js and NPM may require the
EPEL repositories to be added).

The RPM package can also be deployed in a artifact repository and the installed using:

```
yum install perseo-core
```

NOTE: Perseo (front-end) is not installed as part of the dependencies in the RPM, so the URL of an existing Perseo (front-end)
must be provided and configured for Perseo core to work properly.

#### Activate service

Perseo-core is a web application run in a Tomcat. The service will be active if the Tomcat is running.
```
service tomcat start
```

### Installation from Sources

Deployment requires a Tomcat 7 with Java 7. The basic steps for deploying it would be

* Put the war into the webapps directory of the Tomcat server
* Use [environment variables](config.md#configure-perseo-core-with-environment-vars) or copy the [configuration file](config.md#configure-perseo-core-with-configuration-file) (in src/main/resources/perseo-core.properties) to /etc and update the values in the configuration file.

* Verify that the directory for logs exists and has the proper permissions. The log is generated in the /var/log/perseo/perseo.log

Building the .war file can be done by executing
```
    mvn package
```
from the root directory of the project.

#### Undeployment
In order to undeploy the proxy just remove the .war and the directoryin webapps/.


### Log Rotation
Independently of how the service is installed, the log files will need an external rotation (e.g.: the logrotate command) to avoid disk full problems.

Logrotate is installed as RPM dependency along with perseo. The system is configured to rotate every day and whenever the log file size is greater than 100MB (checked very 30 minutes by default):
* For daily rotation: /etc/logrotate.d/logrotate-perseo-daily: which enables daily log rotation
* For size-based rotation:
	* /etc/sysconfig/logrotate-perseo-size: in addition to the previous rotation, this file ensures log rotation if the log file grows beyond a given threshold (100 MB by default)
	* /etc/cron.d/cron-logrotate-perseo-size: which ensures the execution of etc/sysconfig/logrotate-perseo-size at a regular frecuency (default is 30 minutes)
