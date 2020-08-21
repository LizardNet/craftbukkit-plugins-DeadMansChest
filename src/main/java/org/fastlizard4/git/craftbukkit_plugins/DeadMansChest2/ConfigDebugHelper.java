package org.fastlizard4.git.craftbukkit_plugins.DeadMansChest2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ConfigDebugHelper {
    private ConfigDebugHelper() {
        throw new IllegalStateException("no u");
    }

    public static String dumpConfig(Config config) throws IllegalAccessException, InvocationTargetException {
        Field[] configFields = Config.class.getDeclaredFields();
        final StringBuilder sb = new StringBuilder("Dumping Config object with identity hash code ")
                .append(System.identityHashCode(config))
                .append(":\n");

        int fieldCount = 0;
        int getterCount = 0;

        for (Field field : configFields) {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            fieldCount++;

            field.setAccessible(true);

            sb.append("* Field named \"")
                    .append(field.getName())
                    .append("\" has value \"")
                    .append(field.get(config))
                    .append("\" and type ")
                    .append(field.getType());

            String isAssignableToString = String.class.isAssignableFrom(field.getType()) ? "yes" : "no";
            String isAssignableToIntPrimitive = int.class.isAssignableFrom(field.getType()) ? "yes" : "no";
            String isAssignableToBooleanPrimitive = boolean.class.isAssignableFrom(field.getType()) ? "yes" : "no";

            sb.append(" (assignable to String: ")
                    .append(isAssignableToString)
                    .append("; assignable to int: ")
                    .append(isAssignableToIntPrimitive)
                    .append("; assignable to boolean: ")
                    .append(isAssignableToBooleanPrimitive)
                    .append(")\n");
        }

        List<Method> configGetters = Arrays.stream(Config.class.getDeclaredMethods())
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> m.getName().startsWith("get") || m.getName().startsWith("is"))
                .collect(Collectors.toList());

        for (Method getter : configGetters) {
            if (Modifier.isStatic(getter.getModifiers())) {
                continue; //?
            }

            getterCount++;

            sb.append("* Getter named \"")
                    .append(getter.getName())
                    .append("()\" returned value: \"")
                    .append(getter.invoke(config))
                    .append("\" of return type ")
                    .append(getter.getReturnType())
                    .append('\n');
        }

        if (fieldCount != getterCount) {
            sb.append("\nWARNING: Fields count does not match methods count!\n");

            List<String> getterNames = configGetters
                    .stream()
                    .map(Method::getName)
                    .collect(Collectors.toList());

            List<String> fieldNames = Arrays.stream(configFields)
                    .map(Field::getName)
                    .collect(Collectors.toList());

            List<String> mismatches = fieldNames.stream()
                    .filter(f -> !getterNames.contains("get" + f) && !getterNames.contains("is" + f))
                    .collect(Collectors.toList());

            sb.append("The following fields do not appear to have corresponding getters: \n");

            for (String mismatch : mismatches) {
                sb.append("* ")
                        .append(mismatch)
                        .append('\n');
            }

            mismatches = getterNames.stream()
                    .map(n -> n.replaceFirst("^get|is", ""))
                    .filter(n -> !fieldNames.contains(n))
                    .collect(Collectors.toList());

            sb.append("The following methods do not appear to have corresponding fields: \n");

            for (String mismatch : mismatches) {
                sb.append("* ")
                        .append(mismatch)
                        .append('\n');
            }
        }

        return sb.toString().trim();
    }
}
