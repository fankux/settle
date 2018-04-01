package com.fankux.util;

import org.springframework.util.StringUtils;

public class PathUtils {
    static public String cleanfix(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        // for windows, modify slash
        return path.replaceAll("\\\\", "/").replaceAll("//", "/");
    }

    static public String padSuffixSlash(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }

        path = cleanfix(path);
        if (path.charAt(path.length() - 1) != '/') {
            path += '/';
        }
        return path;
    }

    static public String padPrefixSlash(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }

        path = cleanfix(path);
        if (path.charAt(0) != '/') {
            path = '/' + path;
        }
        return path;
    }

    static public String clearPrefixSlash(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }

        path = cleanfix(path);
        path = StringUtils.trimLeadingCharacter(path, '/');
        return path;
    }
}
