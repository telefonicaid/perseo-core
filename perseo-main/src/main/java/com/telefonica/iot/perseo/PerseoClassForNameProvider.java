/**
* Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
*
* This file is part of perseo-core project.
*
* perseo-core is free software: you can redistribute it and/or modify it under the terms of the GNU
* General Public License version 2 as published by the Free Software Foundation.
*
* perseo-core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
* for more details.
*
* You should have received a copy of the GNU General Public License along with perseo-core. If not, see
* http://www.gnu.org/licenses/.
*
* For those usages not covered by the GNU General Public License please contact with
* iot_support at tid dot es
*/

package com.telefonica.iot.perseo;

import com.espertech.esper.common.client.util.ClassForNameProvider;
import com.espertech.esper.common.client.util.ClassForNameProviderDefault;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class PerseoClassForNameProvider
        implements ClassForNameProvider {

    private static final Set<String> ALLOWED_CLASSES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "java.lang.String",
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Number",
            "java.lang.Character",
            "java.lang.Math",
            "java.util.Collection",
            "java.util.List",
            "java.util.Set",
            "java.util.Map",
            "java.time.ZoneId",
            "com.telefonica.iot.perseo.utils.DateTimeUtils"
        )));

    @Override
    public Class classForName(String className)
            throws ClassNotFoundException {

        if (className == null || className.trim().isEmpty()) {
            throw new ClassNotFoundException(
                "Empty class name is not allowed"
            );
        }

        String normalizedClassName = className.trim();

        if (!ALLOWED_CLASSES.contains(normalizedClassName)) {
            throw new ClassNotFoundException(
                "Class access from EPL is not allowed: "
                    + normalizedClassName
            );
        }

        return ClassForNameProviderDefault.INSTANCE.classForName(
            normalizedClassName
        );
    }
}
