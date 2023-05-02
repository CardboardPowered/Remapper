package com.mohistmc.remapper.v2;

import com.google.common.io.ByteStreams;
// import com.mohistmc.remapper.utils.Unsafe;
// import cpw.mods.modlauncher.ClassTransformer;
// import cpw.mods.modlauncher.TransformingClassLoader;
import org.objectweb.asm.ClassReader;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Hashtable;

/**
 * RemapSourceHandler
 *
 * @author Mainly by IzzelAliz and modified Mgazul
 * @originalClassName RemapSourceHandler
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/1.19/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/resource/RemapSourceHandler.java">Click here to get to github</a>
 * <p>
 * These classes are modified by MohistRemapper to support the Mohist software.
 */

public class RemapSourceHandler extends URLStreamHandler {

    public static void register() {
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new RemapSourceConnection(new URL(u.getFile()));
    }

    private static class RemapSourceConnection extends URLConnection {

        static {
        }

        private byte[] array;

        protected RemapSourceConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            byte[] bytes = ByteStreams.toByteArray(url.openStream());
            String className = new ClassReader(bytes).getClassName();
            if (className.startsWith("net/minecraft/") || className.equals("com/mojang/brigadier/tree/CommandNode")) {
                /*try {
                    bytes = (byte[]) MH_TRANSFORM.invokeExact(bytes, className.replace('/', '.'), "source");
                } catch (Throwable e) {
                    throw new IOException(e);
                }*/
            }
            this.array = MohistRemapper.getResourceMapper().remapClassFile(bytes, GlobalClassRepo.INSTANCE);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            if (this.array == null) {
                throw new FileNotFoundException(this.url.getFile());
            } else {
                return new ByteArrayInputStream(this.array);
            }
        }
    }
}
