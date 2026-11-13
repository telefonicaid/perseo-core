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

import com.espertech.esper.common.client.module.Module;
import com.espertech.esper.common.client.module.ModuleItem;
import com.espertech.esper.common.client.soda.CastExpression;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;
import com.espertech.esper.common.client.soda.Expression;
import com.espertech.esper.common.client.soda.Expressions;
import com.espertech.esper.common.client.soda.FilterStream;
import com.espertech.esper.common.client.soda.PatternExpr;
import com.espertech.esper.common.client.soda.PatternFilterExpr;
import com.espertech.esper.common.client.soda.PatternStream;
import com.espertech.esper.common.client.soda.Stream;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompiler;
import java.util.Arrays;
import java.util.Locale;

public final class EplCompatibility {
    private EplCompatibility() {
    }

    public static boolean isBlank(Object value) {
        return value instanceof String && ((String) value).trim().isEmpty();
    }

        static Module prepare(EPCompiler compiler, String epl, CompilerArguments args,
            boolean protectEmptyNumericCasts) throws Exception {
        Module module = compiler.parseModule(epl);
        for (int index = 0; index < module.getItems().size(); index++) {
            ModuleItem item = module.getItems().get(index);
            if (item.isCommentOnly()) {
                continue;
            }
            EPStatementObjectModel model = compiler.eplToModel(item.getExpression(), args.getConfiguration());
            boolean changed = protectEmptyNumericCasts && guard(model.getWhereClause());
            if (model.getFromClause() != null) {
                for (Stream stream : model.getFromClause().getStreams()) {
                    if (stream instanceof FilterStream) {
                        if (protectEmptyNumericCasts) {
                            changed |= guard(((FilterStream) stream).getFilter().getFilter());
                        }
                    } else if (stream instanceof PatternStream) {
                        if (protectEmptyNumericCasts) {
                            changed |= guardPattern(((PatternStream) stream).getExpression());
                        }
                    }
                }
            }
            if (changed) {
                ModuleItem protectedItem = new ModuleItem(model);
                module.getItems().set(index, protectedItem);
            }
        }
        return module;
    }

    private static boolean guardPattern(PatternExpr pattern) {
        boolean changed = false;
        if (pattern instanceof PatternFilterExpr) {
            changed = guard(((PatternFilterExpr) pattern).getFilter().getFilter());
        }
        for (PatternExpr child : pattern.getChildren()) {
            changed |= guardPattern(child);
        }
        return changed;
    }

    private static boolean guard(Expression expression) {
        if (expression == null) {
            return false;
        }
        boolean changed = false;
        for (Expression child : expression.getChildren()) {
            changed |= guard(child);
        }
        if (expression instanceof CastExpression && expression.getChildren().size() == 1) {
            String type = ((CastExpression) expression).getTypeName().toLowerCase(Locale.ROOT);
            if (type.startsWith("java.lang.")) {
                type = type.substring("java.lang.".length());
            }
            if (Arrays.asList("float", "double", "int", "integer", "long", "short", "byte",
                    "bigdecimal", "biginteger", "java.math.bigdecimal", "java.math.biginteger").contains(type)) {
                Expression input = expression.getChildren().get(0);
                expression.getChildren().set(0, Expressions.caseWhenThen()
                        .add(Expressions.staticMethod(EplCompatibility.class.getName(), "isBlank", input),
                                Expressions.constant(null))
                        .setElse(input));
                changed = true;
            }
        }
        return changed;
    }
}
