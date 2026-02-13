package org.cardboardpowered.mappings;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

/**
 * Tsrg writer for mapping-io
 * 
 * @author Cardboard Mod
 */
public final class TsrgWriter {

    private TsrgWriter() {}

    public static void write(MemoryMappingTree tree, Writer out, String fromNs, String toNs) throws IOException {
        int from = tree.getNamespaceId(fromNs);
        int to = tree.getNamespaceId(toNs);

        // System.out.println("SOURCE: " + tree.getSrcNamespace());
        // tree.getDstNamespaces().forEach(ns -> System.out.println(ns));
        
        // System.out.println(from);
        // System.out.println(to);
        /*
        if (from < 0 || to < 0) {
            throw new IllegalArgumentException("Invalid namespaces: " + fromNs + " → " + toNs);
        }
        */

        Collection<? extends ClassMapping> classes = tree.getClasses();

        // Write top-level classes first
        for (ClassMapping cls : classes) {
            if (!isInner(cls.getName(from))) {
                writeClass(cls, classes, out, from, to);
            }
        }
    }

    private static void writeClass(ClassMapping cls, Collection<? extends ClassMapping> classes, Writer out, int from, int to) throws IOException {
        String src = cls.getName(from);
        String dst = cls.getName(to);

        if (src == null || dst == null) return;

        out.write(src);
        out.write(' ');
        out.write(dst);
        out.write('\n');

        // Fields
        for (FieldMapping f : cls.getFields()) {
            String fSrc = f.getName(from);
            String fDst = f.getName(to);

            if (fSrc != null && fDst != null) {
                out.write('\t');
                out.write(fSrc);
                out.write(' ');
                out.write(fDst);
                out.write('\n');
            }
        }

        // Methods
        for (MethodMapping m : cls.getMethods()) {
            String mSrc = m.getName(from);
            String mDst = m.getName(to);
            String descSrc = m.getDesc(from);

            if (mSrc != null && mDst != null && descSrc != null) {
                out.write('\t');
                out.write(mSrc);
                out.write(' ');
                out.write(descSrc);
                out.write(' ');
                out.write(mDst);
                out.write('\n');
            }
        }

        // Inner classes: find all classes whose names start with this class + "$"
        String prefix = src + "$";
        for (ClassMapping candidate : classes) {
            String candSrc = candidate.getName(from);
            if (candSrc != null && candSrc.startsWith(prefix)) {
                writeClass(candidate, classes, out, from, to);
            }
        }
    }

    private static boolean isInner(String name) {
        return name != null && name.contains("$");
    }
}
