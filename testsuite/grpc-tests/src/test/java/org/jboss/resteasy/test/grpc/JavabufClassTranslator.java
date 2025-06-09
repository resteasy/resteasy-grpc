package org.jboss.resteasy.test.grpc;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class JavabufClassTranslator {

    private static Map<String, Method> GET_MAP = new HashMap<String, Method>();
    private static Map<String, Method> SET_MAP = new HashMap<String, Method>();

    public static void main(String[] args) {
    }

    public static Method getGetter(String classname) {
        return GET_MAP.get(classname);
    }

    public static Method getSetter(String classname) {
        return SET_MAP.get(classname);
    }

    static {
        try {
            Class<?> builder = Class.forName("dev.resteasy.grpc.example.CC1_proto$GeneralEntityMessage$Builder");
            Class<?> response = Class.forName("dev.resteasy.grpc.example.CC1_proto$GeneralReturnMessage");
            String dir = System.getProperty("user.dir") + File.separator + "target" + File.separator + "entityTypes";
            final Path file = Path.of(dir);
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            	String line = reader.readLine();
            	while (line != null) {
            		int n = line.indexOf(" ");
            		String l1 = line.substring(0, n);
            		String l2 = line.substring(n + 1);
            		Class<?> javabufClass = Class.forName(l2);
            		if (l2.contains("$")) {
            			l2 = l2.substring(l2.lastIndexOf('$') + 1);
            		}
            		String methodSuffix = squashToCamel(l2) + "Field";
            		try {
            			GET_MAP.put(l1, response.getDeclaredMethod("get" + methodSuffix));
            		} catch (NoSuchMethodException e) {
            		}
            		try {
            			SET_MAP.put(l1, builder.getDeclaredMethod("set" + methodSuffix, javabufClass));
            		} catch (NoSuchMethodException e) {
            		}
            		line = reader.readLine();
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static String squashToCamel(String name) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '_') {
                start = true;
                continue;
            }
            sb.append(start ? name.substring(i, i + 1).toUpperCase() : name.substring(i, i + 1));
            start = false;
        }
        return sb.toString();
    }
}
