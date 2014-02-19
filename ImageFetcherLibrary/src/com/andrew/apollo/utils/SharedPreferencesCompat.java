package com.andrew.apollo.utils;

import android.content.SharedPreferences.Editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection utils to call {@link android.content.SharedPreferences.Editor} apply() when
 * possible, falling back to commit when apply isn't available.
 */
public final class SharedPreferencesCompat {

    private final static Method mApplyMethod = findApplyMethod();

    /**
     * @return The apply() method from {@link android.content.SharedPreferences.Editor}.
     */
    private final static Method findApplyMethod() {
        try {
            final Class<Editor> class1 = Editor.class;
            return class1.getMethod("apply");
        } catch (final NoSuchMethodException ignored) {
        }
        return null;
    }

    /**
     * @param editor The {@link android.content.SharedPreferences.Editor} to use.
     */
    public static void apply(final Editor editor) {
        if (mApplyMethod != null) {
            try {
                mApplyMethod.invoke(editor);
                return;
            } catch (final InvocationTargetException ignored) {
            } catch (final IllegalAccessException ignored) {
            }
        }
        editor.commit();
    }
}
