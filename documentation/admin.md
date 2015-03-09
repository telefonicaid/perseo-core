# Administration

Perseo-core is run as a web application inside a Tomcat 7, acting as a servlet container. The administration is the usual of an application in a [Tomcat](http://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html)

The way of starting Tomcat would be
```
service start tomcat
```

To stop it
```
service stop tomcat
```

A quick check can be done with
```
curl -v localhost:8080/perseo-core/index.jsp
```

that should return a 200 OK  and a body similar to
```
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esper Server</title>
    </head>
    <body>
        <h1>Welcome to perseo core!</h1>
        <dl>
            <dt>Version</dt>
            <dd>0.1.1</dd>
        </dl>
    </body>
</html>
```

After a change in the configuration file, a configuration reload can be asked for with
```
curl -v localhost:8080/perseo-core/reload.jsp
```

that will return a body with
```
reload: true
```
in its content

What version is deployed can be checked with
```
curl -v localhost:8080/perseo-core/version.jsp
```
that returns a text/plain like
```
0.1.1
```
