package org.cardboardpowered.mohistremap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.mohistmc.remap.remappers.BannerJarRemapper;

/**
 * @author Cardboard Mod
 * @reason Add interface for RemapUtil
 */
public interface IRemapUtils {

    public byte[] remapFindClass(byte[] bs);

    public String map(String typeName);

    public String reverseMap(String typeName);

    public String reverseMap(Class<?> clazz);

    public String mapPackage(String typeName);

    public String remapMethodDesc(String methodDescriptor);

    public String mapMethodName(Class<?> clazz, String name, MethodType methodType);

    public String mapMethodName(Class<?> type, String name, Class<?>... parameterTypes);

    public String inverseMapMethodName(Class<?> type, String name, Class<?>... parameterTypes);

    public String mapFieldName(Class<?> type, String fieldName);

    public String inverseMapFieldName(Class<?> type, String fieldName);

    public String inverseMapName(Class<?> clazz);

    public String inverseMapSimpleName(Class<?> clazz);

    public default boolean isNMSClass(String className) {
        return className.startsWith("net.minecraft.");
    }

    public default boolean needRemap(String className){
        return className.startsWith("net.minecraft.");
    }
	
    public BannerJarRemapper getJarRemapper();
    
    public boolean shouldExtraDebugLog();

	public void init();
	
	/**
	 */
	public static File exportResource(String res, File folder) {
        try (InputStream stream = IRemapUtils.class.getClassLoader().getResourceAsStream(res)) {
            if (stream == null) throw new IOException("Null " + res);

            Path p = Paths.get(folder.getAbsolutePath() + File.separator + res);
            Files.copy(stream, p, StandardCopyOption.REPLACE_EXISTING);
            return p.toFile();
        } catch (IOException e) { e.printStackTrace(); return null;}
    }
	
	public default String getMethodDescriptor(Method method, String name) {
        StringBuilder descriptor = new StringBuilder();
        descriptor.append("(");
        for (java.lang.reflect.Type parameterType : method.getGenericParameterTypes()) {
            descriptor.append(getTypeDescriptor(parameterType, name));
        }
        descriptor.append(")");
        descriptor.append(getTypeDescriptor(method.getGenericReturnType(), name));
        return descriptor.toString();
    }

    public default String getTypeDescriptor(java.lang.reflect.Type parameterType, String name) {
        if (parameterType instanceof Class<?>) {
            return getClassDescriptor((Class<?>) parameterType, name);
        } else if (parameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) parameterType;
            StringBuilder descriptor = new StringBuilder();
            descriptor.append(getClassDescriptor((Class<?>) parameterizedType.getRawType(), name));
            for (java.lang.reflect.Type arg : parameterizedType.getActualTypeArguments()) {
                descriptor.append(getTypeDescriptor(arg, name));
            }
            return descriptor.toString();
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }

    public default String getClassDescriptor(Class<?> clazz, String name) {
        if (clazz.isArray()) {
            return "[" + getClassDescriptor(clazz.getComponentType(), name);
        } else if (clazz.isPrimitive()) {
            if (clazz == void.class) return "V";
            if (clazz == boolean.class) return "Z";
            if (clazz == byte.class) return "B";
            if (clazz == char.class) return "C";
            if (clazz == short.class) return "S";
            if (clazz == int.class) return "I";
            if (clazz == long.class) return "J";
            if (clazz == float.class) return "F";
            if (clazz == double.class) return "D";
        } else {
        	
        	if (!name.isEmpty()) {
        		// String in = jt.getInternalName().replace('/', '.');
    			String ll = getClassDescriptorResolveName(name, clazz.getName().replace('/', '.'));
    			return "L" + ll.replace('.', '/') + ";";
        	}
        	
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
        throw new IllegalArgumentException("Unsupported class: " + clazz);
    }
    
    public static String getDescriptor(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == void.class) return "V";
            else if (clazz == boolean.class) return "Z";
            else if (clazz == byte.class) return "B";
            else if (clazz == char.class) return "C";
            else if (clazz == short.class) return "S";
            else if (clazz == int.class) return "I";
            else if (clazz == long.class) return "J";
            else if (clazz == float.class) return "F";
            else if (clazz == double.class) return "D";
        } else if (clazz.isArray()) {
            return clazz.getName().replace('.', '/');
        } else {
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
        throw new IllegalArgumentException("Unknown type: " + clazz);
    }
    
    public static String getMethodDescriptor(Class<?>... parameterTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> param : parameterTypes) {
            sb.append(getDescriptor(param));
        }
        sb.append(')');
        // sb.append('V'); // if you want to include return type, replace 'V' with getDescriptor(returnType)
        return sb.toString();
    }
    
    /**
     * @param Namespace
     * @param Class name (in dot form)
     */
    public String getClassDescriptorResolveName(String namespace, String name);
    
}
