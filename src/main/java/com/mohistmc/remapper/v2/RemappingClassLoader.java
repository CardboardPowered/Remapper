package com.mohistmc.remapper.v2;

import cpw.mods.modlauncher.TransformingClassLoader;

/**
 * RemappingClassLoader
 *
 * @author Mainly by IzzelAliz and modified Mgazul
 * @originalClassName RemappingClassLoader
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/1.19/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/RemappingClassLoader.java">Click here to get to github</a>
 * <p>
 * These classes are modified by MohistRemapper to support the Mohist software.
 */
public interface RemappingClassLoader {

    static ClassLoader asTransforming(ClassLoader classLoader) {
        boolean found = false;
        while (classLoader != null) {
            if (classLoader instanceof TransformingClassLoader || classLoader instanceof RemappingClassLoader) {
                found = true;
                break;
            } else {
                classLoader = classLoader.getParent();
            }
        }
        return found ? classLoader : RemappingClassLoader.class.getClassLoader();
    }

    ClassLoaderRemapper getRemapper();
}
