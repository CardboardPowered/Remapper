package com.mohistmc.remapper.v2;

import org.objectweb.asm.tree.ClassNode;

/**
 * PluginTransformer
 *
 * @author Mainly by IzzelAliz and modified Mgazul
 * @originalClassName PluginTransformer
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/1.19/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/PluginTransformer.java">Click here to get to github</a>
 * <p>
 * These classes are modified by MohistRemapper to support the Mohist software.
 */
@FunctionalInterface
public interface PluginTransformer {

    void handleClass(ClassNode node, ClassLoaderRemapper remapper);

    default int priority() {
        return 0;
    }
}
