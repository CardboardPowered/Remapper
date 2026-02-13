package org.cardboardpowered.mohistremap.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cardboardpowered.mohistremap.RemapUtilProvider;
import org.cardboardpowered.mohistremap.utils.ASMUtils;
import org.cardboardpowered.mohistremap.utils.ReflectionUtils;

/**
 *
 * @author pyz
 * @date 2019/7/1 12:24 AM
 */
public class ProxyClass {

    public static Class<?> forName(String className) throws ClassNotFoundException {
        return forName(className, true, ReflectionUtils.getCallerClassLoader());
    }

    public static Class<?> forName(String className, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(ASMUtils.toClassName(RemapUtilProvider.get().map(className.replace('.', '/'))), initialize, loader);
    }

    public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        if (RemapUtilProvider.get().needRemap(clazz.getName())) {
            name = RemapUtilProvider.get().mapMethodName(clazz, name, parameterTypes);
        }
        return clazz.getDeclaredMethod(name, parameterTypes);
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        if (RemapUtilProvider.get().needRemap(clazz.getName())) {
            name = RemapUtilProvider.get().mapMethodName(clazz, name, parameterTypes);
        }
        return clazz.getMethod(name, parameterTypes);
    }

    public static Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException, SecurityException {
        if (RemapUtilProvider.get().needRemap(clazz.getName())) {
            name = RemapUtilProvider.get().mapFieldName(clazz, name);
        }
        return clazz.getDeclaredField(name);
    }

    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException, SecurityException {
        if (RemapUtilProvider.get().needRemap(clazz.getName())) {
            name = RemapUtilProvider.get().mapFieldName(clazz, name);
        }
        return clazz.getField(name);
    }

    public static String getName(Class<?> clazz) {
        return RemapUtilProvider.get().inverseMapName(clazz);
    }

    public static String getSimpleName(Class<?> clazz) {
        return RemapUtilProvider.get().inverseMapSimpleName(clazz);
    }

    public static Method[] getDeclaredMethods(Class<?> inst) {
        try {
            return inst.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            return new Method[]{};
        }
    }

    public static String getName(Field field) {
        return RemapUtilProvider.get().inverseMapFieldName(field.getDeclaringClass(), field.getName());
    }

    public static String getName(Method method) {
        return RemapUtilProvider.get().inverseMapMethodName(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }

    public static Class<?> loadClass(ClassLoader inst, String className) throws ClassNotFoundException {
        if (className.startsWith("net.minecraft.")) {
            className = ASMUtils.toClassName(RemapUtilProvider.get().map(ASMUtils.toInternalName(className)));
        }
        return inst.loadClass(className);
    }
}
