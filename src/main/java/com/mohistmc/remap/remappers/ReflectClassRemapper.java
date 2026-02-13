package com.mohistmc.remap.remappers;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 *
 * @author pyz
 * @date 2019/7/2 9:16 PM
 */
public class ReflectClassRemapper extends ClassRemapper {

    public ReflectClassRemapper(ClassVisitor cv, Remapper remapper) {
        super(cv, remapper);
    }

    public ReflectClassRemapper(int api, ClassVisitor cv, Remapper remapper) {
        super(api, cv, remapper);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, name, desc, signature, value);
    }

    /**
     * @param mv
     * @return ReflectMethodRemapper
     */
    @Override
    protected MethodVisitor createMethodRemapper(MethodVisitor mv) {
        return new ReflectMethodRemapper(mv, remapper);
    }
}
