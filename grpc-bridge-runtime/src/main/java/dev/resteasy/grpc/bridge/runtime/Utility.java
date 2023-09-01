package dev.resteasy.grpc.bridge.runtime;

public final class Utility {

    private Utility() {
        // restrict instantiation
    }

    public static String camelize(String s) {
        boolean sawUnderScore = false;
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(s.charAt(0)));
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == '_') {
                sawUnderScore = true;
                continue;
            }
            if (sawUnderScore) {
                if (s.substring(i).startsWith("INNER_")) {
                    sb.append("INNER");
                    i += "INNER".length();
                } else {
                    sb.append(Character.toUpperCase(s.charAt(i)));
                    sawUnderScore = false;
                }
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }
}
