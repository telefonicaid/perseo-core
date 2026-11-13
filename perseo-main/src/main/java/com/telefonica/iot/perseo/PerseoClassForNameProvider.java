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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class PerseoClassForNameProvider
        implements ClassForNameProvider {

    private static final String ALLOWED_EXTRA_CLASSES_FILE_ENV =
        "PERSEO_EPL_ALLOWED_EXTRA_CLASSES_FILE";

    private static final Logger LOGGER =
        LoggerFactory.getLogger(PerseoClassForNameProvider.class);

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
            "java.util.GregorianCalendar",
            "java.util.List",
            "java.util.Set",
            "java.util.Map",
            "java.util.Arrays",
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
    private final String allowedClassesFile;

    public PerseoClassForNameProvider() {
        this(System.getenv(ALLOWED_EXTRA_CLASSES_FILE_ENV));
    }

    PerseoClassForNameProvider(String extraClassesFile) {
        this.allowedClasses = DEFAULT_ALLOWED_CLASSES;
        this.allowedClassesFile = extraClassesFile;
    }

    private Set<String> readExtraAllowedClassesFile() {
        if (allowedClassesFile == null || allowedClassesFile.trim().isEmpty()) {
            return Collections.emptySet();
        }

        try {
            List<String> lines = Files.readAllLines(
                Paths.get(allowedClassesFile),
                StandardCharsets.UTF_8
            );
            Set<String> extraClasses = new HashSet<String>();
            for (String line : lines) {
                String className = line.trim();
                if (className.isEmpty() || className.startsWith("#")) {
                    continue;
                }
                if (!FULLY_QUALIFIED_CLASS_NAME.matcher(className).matches()) {
                    throw new IllegalArgumentException(
                        "Expected a fully qualified class name: " + className
                    );
                }
                if (isDeniedClass(className)) {
                    throw new IllegalArgumentException(
                        "Class is blocked by the EPL class policy: " + className
                    );
                }
                extraClasses.add(className);
            }
            return extraClasses;
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.warn(
                "Unable to read additional EPL classes from {}: {}",
                allowedClassesFile,
                exception.getMessage()
            );
            return Collections.emptySet();
        }
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
                || (!allowedClasses.contains(normalizedClassName)
                    && !readExtraAllowedClassesFile().contains(normalizedClassName))) {
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
