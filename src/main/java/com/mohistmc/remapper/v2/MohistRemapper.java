package com.mohistmc.remapper.v2;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mohistmc.remapper.McVersion;
import com.mohistmc.remapper.utils.Unsafe;
import lombok.Setter;
import lombok.SneakyThrows;
import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.JointProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * MohistRemapper
 *
 * @author Mainly by IzzelAliz and modified Mgazul
 * @originalClassName ArclightRemapper
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/1.19/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ArclightRemapper.java">Click here to get to github</a>
 * <p>
 * These classes are modified by MohistRemapper to support the Mohist software.
 */
@SuppressWarnings("unchecked")
public class MohistRemapper {

    public static final File DUMP = null;
    public static MohistRemapper INSTANCE;
    public static Logger LOGGER = LogManager.getLogger(MohistRemapper.class.getName());
    @Setter
    public static McVersion obsVersion;
    private static long pkgOffset, clOffset, mdOffset, fdOffset, mapOffset;

    static {
        try {
            pkgOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("packages"));
            clOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("classes"));
            mdOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("methods"));
            fdOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("fields"));
            mapOffset = Unsafe.objectFieldOffset(JarMapping.class.getDeclaredField("inheritanceMap"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public final InheritanceMap inheritanceMap;
    private final JarMapping toNmsMapping;
    private final JarMapping toBukkitMapping;
    private final List<PluginTransformer> transformerList = new ArrayList<>();
    private final JarRemapper toBukkitRemapper;
    private final JarRemapper toNmsRemapper;

    public MohistRemapper() throws Exception {
        this.toNmsMapping = new JarMapping();
        this.toBukkitMapping = new JarMapping();
        this.inheritanceMap = new InheritanceMap();
        InputStream nmsFile = MohistRemapper.class.getClassLoader().getResourceAsStream("mappings/" + obsVersion.getObs_version() + "/nms.srg");
        this.toNmsMapping.loadMappings(new BufferedReader(new InputStreamReader(nmsFile)), null, null, false);
        // TODO workaround for https://github.com/md-5/SpecialSource/pull/81
        //  remove on update
        var content = new String(nmsFile.readAllBytes(), StandardCharsets.UTF_8);
        var i = content.indexOf("net/minecraft/server/level/ChunkMap net/minecraft/server/level/ChunkTracker");
        var nextSection = content.substring(i).lines().skip(1).dropWhile(it -> it.startsWith("\t")).findFirst().orElseThrow();
        var nextIndex = content.indexOf(nextSection);
        this.toBukkitMapping.loadMappings(
                new BufferedReader(new StringReader(content.substring(0, i) + content.substring(nextIndex))),
                null, null, true
        );
        this.toBukkitMapping.loadMappings(
                new BufferedReader(new StringReader(content.substring(i, nextIndex))),
                null, null, true
        );
        BiMap<String, String> inverseClassMap = HashBiMap.create(toNmsMapping.classes).inverse();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(MohistRemapper.class.getClassLoader().getResourceAsStream("mappings/" + obsVersion.getObs_version() + "/inheritanceMap.txt")))) {
            inheritanceMap.load(reader, inverseClassMap);
        }
        JointProvider inheritanceProvider = new JointProvider();
        inheritanceProvider.add(inheritanceMap);
        inheritanceProvider.add(new ClassLoaderProvider(ClassLoader.getSystemClassLoader()));
        this.toNmsMapping.setFallbackInheritanceProvider(inheritanceProvider);
        this.toBukkitMapping.setFallbackInheritanceProvider(inheritanceProvider);
        this.transformerList.add(MohistInterfaceInvokerGen.INSTANCE);
        this.transformerList.add(MohistRedirectAdapter.INSTANCE);
        this.transformerList.add(ClassLoaderAdapter.INSTANCE);
        //this.transformerList.add((node, remapper) -> SwitchTableFixer.handleClass(node));
        toBukkitMapping.setFallbackInheritanceProvider(GlobalClassRepo.inheritanceProvider());
        this.toBukkitRemapper = new LenientJarRemapper(toBukkitMapping);
        this.toNmsRemapper = new LenientJarRemapper(toNmsMapping);
        RemapSourceHandler.register();
    }

    @SneakyThrows
    public static void init(McVersion mcVersion) {
        if (!mcVersion.equals(McVersion.v1_19_3)) {
            throw new UnsupportedOperationException("Versions earlier than 1.19.3 are not supported, Please wait for follow-up support!");
        }
        obsVersion = mcVersion;
        INSTANCE = new MohistRemapper();
    }

    public static ClassLoaderRemapper createClassLoaderRemapper(ClassLoader classLoader) {
        return new ClassLoaderRemapper(INSTANCE.copyOf(INSTANCE.toNmsMapping), INSTANCE.copyOf(INSTANCE.toBukkitMapping), classLoader);
    }

    public static JarRemapper getResourceMapper() {
        return INSTANCE.toBukkitRemapper;
    }

    public static JarRemapper getNmsMapper() {
        return INSTANCE.toNmsRemapper;
    }

    public List<PluginTransformer> getTransformerList() {
        return transformerList;
    }

    private JarMapping copyOf(JarMapping mapping) {
        JarMapping jarMapping = new JarMapping();
        Unsafe.putObject(jarMapping, pkgOffset, Unsafe.getObject(mapping, pkgOffset));
        Unsafe.putObject(jarMapping, clOffset, Unsafe.getObject(mapping, clOffset));
        Unsafe.putObject(jarMapping, mdOffset, Unsafe.getObject(mapping, mdOffset));
        Unsafe.putObject(jarMapping, fdOffset, Unsafe.getObject(mapping, fdOffset));
        Unsafe.putObject(jarMapping, mapOffset, Unsafe.getObject(mapping, mapOffset));
        return jarMapping;
    }
}
