package com.fl.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;

public class PathUtils {
    static public String cleanfix(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        // for windows, modify slash
        path = FilenameUtils.separatorsToUnix(path);
        path = FilenameUtils.normalize(path, true);
        return path;
    }

    static public String basename(String path) {
        cleanfix(path);
        path = FilenameUtils.getName(path);
        return path;
    }

    static public String dirname(String path) {
        cleanfix(path);
        path = FilenameUtils.getPath(path);
        return path;
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
