<%-- 

 Copyright 2015 Telefonica Investigacion y Desarrollo, S.A.U

 This file is part of perseo-core.

 perseo-core is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 perseo-core is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with perseo-core. If not, see http://www.gnu.org/licenses/.

 For those usages not covered by this license please contact with
 iot_support at tid dot es

--%>
<%@page import="com.telefonica.iot.perseo.Version"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
            <dd><%= Version.get() %></dd>
        </dl>
    </body>
</html>
