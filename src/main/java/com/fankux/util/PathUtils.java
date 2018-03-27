package com.fankux.util;

import org.springframework.util.StringUtils;

public class PathUtils {
    static public String cleanfix(String path) {
        // for windows, remove driver name
        int idx;
        if ((idx = path.indexOf(':')) != -1) {
            path = path.substring(idx + 1);
        }
        return path;
    }

    static public String padSuffixSlash(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }

        if (path.charAt(path.length() - 1) != '/') {
            path += '/';
        }
        return path;
    }

    static public String padPrefixSlash(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }

        if (path.charAt(0) != '/') {
            path = '/' + path;
        }
        return path;
    }

    static public String clearPrefixSlash(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        path = StringUtils.trimLeadingCharacter(path, '/');
        return path;
    }
}
