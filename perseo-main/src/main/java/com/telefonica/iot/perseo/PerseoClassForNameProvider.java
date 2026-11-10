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
import java.util.regex.Pattern;

public final class PerseoClassForNameProvider
        implements ClassForNameProvider {

    private static final String ALLOWED_EXTRA_CLASSES_ENV =
        "PERSEO_EPL_ALLOWED_EXTRA_CLASSES";

    private static final Pattern FULLY_QUALIFIED_CLASS_NAME = Pattern.compile(
        "[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)+"
    );

    private static final Set<String> DEFAULT_ALLOWED_CLASSES =
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
            "java.util.Calendar",
            "java.util.List",
            "java.util.Set",
            "java.util.Map",
            "java.time.ZoneId",
            "com.telefonica.iot.perseo.utils.DateTimeUtils"
        )));

    private static final Set<String> DENIED_CLASSES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.System",
            "java.lang.Class",
            "java.lang.ClassLoader",
            "java.lang.Module",
            "java.lang.reflect.Method",
            "java.lang.reflect.Constructor",
            "java.lang.reflect.Field"
        )));

    private static final Set<String> DENIED_PACKAGE_PREFIXES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "java.io.",
            "java.nio.file.",
            "java.lang.reflect.",
            "java.net.",
            "javax.script.",
            "jdk.",
            "sun."
        )));

    private final Set<String> allowedClasses;

    public PerseoClassForNameProvider() {
        this(System.getenv(ALLOWED_EXTRA_CLASSES_ENV));
    }

    PerseoClassForNameProvider(String extraAllowedClasses) {
        Set<String> configuredClasses = new HashSet<String>(DEFAULT_ALLOWED_CLASSES);
        configuredClasses.addAll(parseExtraAllowedClasses(extraAllowedClasses));
        this.allowedClasses = Collections.unmodifiableSet(configuredClasses);
    }

    private static Set<String> parseExtraAllowedClasses(String configuredClasses) {
        if (configuredClasses == null || configuredClasses.trim().isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> extraClasses = new HashSet<String>();
        for (String configuredClass : configuredClasses.split(",", -1)) {
            String className = configuredClass.trim();
            if (!FULLY_QUALIFIED_CLASS_NAME.matcher(className).matches()) {
                throw new IllegalArgumentException(
                    ALLOWED_EXTRA_CLASSES_ENV
                        + " accepts only comma-separated fully qualified class names: "
                        + className
                );
            }
            if (isDeniedClass(className)) {
                throw new IllegalArgumentException(
                    "Class cannot be added to " + ALLOWED_EXTRA_CLASSES_ENV
                        + ": " + className
                );
            }
            extraClasses.add(className);
        }
        return extraClasses;
    }

    private static boolean isDeniedClass(String className) {
        for (String deniedClass : DENIED_CLASSES) {
            if (className.equals(deniedClass) || className.startsWith(deniedClass + "$")) {
                return true;
            }
        }
        for (String deniedPackagePrefix : DENIED_PACKAGE_PREFIXES) {
            if (className.startsWith(deniedPackagePrefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class classForName(String className)
            throws ClassNotFoundException {

        if (className == null || className.trim().isEmpty()) {
            throw new ClassNotFoundException(
                "Empty class name is not allowed"
            );
        }

        String normalizedClassName = className.trim();

    if (isDeniedClass(normalizedClassName)
        || !allowedClasses.contains(normalizedClassName)) {
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
