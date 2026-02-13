package org.cardboardpowered.mohistremap.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;

public class PluginFixManager {


    public static byte[] injectPluginFix(String className, byte[] clazz) {
        if (className.endsWith("PaperLib")) {
            // return PluginFixManager.removePaper(clazz);
        }
        if (className.equals("com.earth2me.essentials.utils.VersionUtil")) {
            return helloWorld(clazz, "net.fabricmc.loader.launch.knot.KnotServer", "hello.World");
        }
        if (className.equals("com.sk89q.worldedit.bukkit.adapter.Refraction")) {
            return helloWorld(clazz, "net.minecraft.nbt.ListTag", "hello.World");
        }
        if (className.endsWith("net.ess3.nms.refl.providers.ReflServerStateProvider")) {
            return helloWorld(clazz, "u", "method_3828");
        }
        return clazz;
    }

    public static boolean isPaper() {
        return true;
    }

    public static byte[] helloWorld(byte[] basicClass, String a, String b) {
        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        ClassWriter classWriter = new ClassWriter(0);
        classReader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode next : method.instructions) {
                if (next instanceof LdcInsnNode ldcInsnNode) {
                    if (ldcInsnNode.cst instanceof String str) {
                        if (a.equals(str)) {
                            ldcInsnNode.cst = b;
                        }
                    }
                }
            }
        }

        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
