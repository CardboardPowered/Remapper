package org.cardboardpowered.mohistremap.utils;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cardboardpowered.mohistremap.ClassMapping;
import org.cardboardpowered.mohistremap.IRemapUtils;
import org.cardboardpowered.mohistremap.RemapUtilProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import com.mohistmc.remap.remappers.BannerInheritanceMap;
import com.mohistmc.remap.remappers.BannerInheritanceProvider;
import com.mohistmc.remap.remappers.BannerJarMapping;
import com.mohistmc.remap.remappers.BannerJarRemapper;
import com.mohistmc.remap.remappers.BannerSuperClassRemapper;
import com.mohistmc.remap.remappers.ClassRemapperSupplier;
import com.mohistmc.remap.remappers.ReflectMethodRemapper;
import com.mohistmc.remap.remappers.ReflectRemapper;

/**
 *
 * @author pyz
 * @date 2019/6/30 11:50 PM
 */
public class BasicRemapUtils implements IRemapUtils {

    public static BannerJarMapping jarMapping;
    public static BannerJarRemapper jarRemapper;
    private static final List<Remapper> remappers = new ArrayList<>();

    public static void initUtils() {
    	new BasicRemapUtils().init();
    }

    @Override
    public void init() {
    	RemapUtilProvider.setInstance(this);
    	
        jarMapping = new BannerJarMapping();
        jarMapping.packages.put("org/bukkit/craftbukkit/v1_19_R1/", "org/bukkit/craftbukkit/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/it/unimi/dsi/fastutil/", "it/unimi/dsi/fastutil/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/jline/", "jline/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/org/apache/commons/", "org/apache/commons/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/org/objectweb/asm/", "org/objectweb/asm/");
        jarMapping.setInheritanceMap(new BannerInheritanceMap());
        jarMapping.setFallbackInheritanceProvider(new BannerInheritanceProvider());

        try {
            jarMapping.loadMappingsFromStream(
            		BasicRemapUtils.class.getClassLoader().getResourceAsStream("mappings/spigot2srg.srg"),
                    null,
                    null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        jarRemapper = new BannerJarRemapper(jarMapping);
        remappers.add(jarRemapper);
        remappers.add(new ReflectRemapper());
        jarMapping.initFastMethodMapping(jarRemapper);
        ReflectMethodRemapper.init();

        try {
            Class.forName("org.cardboardpowered.mohistremap.proxy.ProxyMethodHandlesLookup");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] remapFindClass(byte[] bs) {
        ClassReader reader = new ClassReader(bs); // Turn from bytes into visitor
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        for (Remapper remapper : remappers) {

            ClassNode container = new ClassNode();
            ClassRemapper classRemapper;
            if (remapper instanceof ClassRemapperSupplier) {
                classRemapper = ((ClassRemapperSupplier) remapper).getClassRemapper(container);
            } else {
                classRemapper = new ClassRemapper(container, remapper);
            }
            classNode.accept(classRemapper);
            classNode = container;
        }
        BannerSuperClassRemapper.init(classNode);
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();

    }

    @Override
    public String map(String typeName) {
        typeName = mapPackage(typeName);
        return jarMapping.classes.getOrDefault(typeName, typeName);
    }

    @Override
    public String reverseMap(String typeName) {
        ClassMapping mapping = jarMapping.byNMSInternalName.get(typeName);
        return mapping == null ? typeName : mapping.getNmsSrcName();
    }

    @Override
    public String reverseMap(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? ASMUtils.toInternalName(clazz) : mapping.getNmsSrcName();
    }

    @Override
    public String mapPackage(String typeName) {
        for (Map.Entry<String, String> entry : jarMapping.packages.entrySet()) {
            String prefix = entry.getKey();
            if (typeName.startsWith(prefix)) {
                return entry.getValue() + typeName.substring(prefix.length());
            }
        }
        return typeName;
    }

    @Override
    public String remapMethodDesc(String methodDescriptor) {
        Type rt = Type.getReturnType(methodDescriptor);
        Type[] ts = Type.getArgumentTypes(methodDescriptor);
        rt = Type.getType(ASMUtils.toDescriptorV2(map(ASMUtils.getInternalName(rt))));
        for (int i = 0; i < ts.length; i++) {
            ts[i] = Type.getType(ASMUtils.toDescriptorV2(map(ASMUtils.getInternalName(ts[i]))));
        }
        return Type.getMethodType(rt, ts).getDescriptor();
    }

    @Override
    public String mapMethodName(Class<?> clazz, String name, MethodType methodType) {
        return mapMethodName(clazz, name, methodType.parameterArray());
    }

    @Override
    public String mapMethodName(Class<?> type, String name, Class<?>... parameterTypes) {
        return jarMapping.fastMapMethodName(type, name, parameterTypes);
    }

    @Override
    public String inverseMapMethodName(Class<?> type, String name, Class<?>... parameterTypes) {
        return jarMapping.fastReverseMapMethodName(type, name, parameterTypes);
    }

    @Override
    public String mapFieldName(Class<?> type, String fieldName) {
        String key = reverseMap(type) + "/" + fieldName;
        String mapped = jarMapping.fields.get(key);
        if (mapped == null) {
            Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                mapped = mapFieldName(superClass, fieldName);
            }
        }
        return mapped != null ? mapped : fieldName;
    }

    @Override
    public String inverseMapFieldName(Class<?> type, String fieldName) {
        return jarMapping.fastReverseMapFieldName(type, fieldName);
    }

    @Override
    public String inverseMapName(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? clazz.getName() : mapping.getNmsName();
    }

    @Override
    public String inverseMapSimpleName(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? clazz.getSimpleName() : mapping.getNmsSimpleName();
    }

    @Override
    public boolean isNMSClass(String className) {
        return className.startsWith("net.minecraft.");
    }

    @Override
    public boolean needRemap(String className){
        return className.startsWith("net.minecraft.");
    }


	@Override
	public BannerJarRemapper getJarRemapper() {
		return jarRemapper;
	}

	@Override
	public boolean shouldExtraDebugLog() {
		return false;
	}

	@Override
	public String getClassDescriptorResolveName(String namespace, String name) {
		return name;
	}

}
