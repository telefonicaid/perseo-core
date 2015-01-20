<%-- 

 Copyright 2015 Telefonica Investigacion y Desarrollo, S.A.U

 This file is part of perseo-core.

 perseo-core is free software: you can redistribute it and/or
 modify it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 perseo-core is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with perseo-core. If not, see http://www.gnu.org/licenses/.

 For those usages not covered by this license please contact with
 iot_support at tid dot es

--%>

<%@page import="es.tid.fiware.perseo.Configuration"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Esper Server</title>
    </head>
    <body>
        <h1>reload: <%= Configuration.reload()%></h1>
    </body>
</html>
