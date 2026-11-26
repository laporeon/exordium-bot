package com.laporeon.javoid.helpers;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class BundleManager {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", Locale.ROOT);

    private BundleManager() {
    }

    public static String get(String key, Object... args) {
        String pattern = BUNDLE.getString(key);
        return new MessageFormat(pattern, Locale.ROOT).format(args);
    }
}