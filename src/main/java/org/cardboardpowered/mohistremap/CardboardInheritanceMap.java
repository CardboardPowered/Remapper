package org.cardboardpowered.mohistremap;

import java.io.*;
import java.util.*;

import com.google.common.collect.BiMap;

import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.JarRemapper;

public class CardboardInheritanceMap extends InheritanceMap {

	private boolean errorOnNotRemap = false;
	
	public CardboardInheritanceMap setErrorOnNotRemap(boolean a) {
		this.errorOnNotRemap = a;
		return this;
	}
	
	public void load(BufferedReader reader, BiMap<String, String> classMap) throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            int commentIndex = line.indexOf('#');
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            if (line.isEmpty()) {
                continue;
            }
            String[] tokens = line.split(" ");

            if (tokens.length < 2) {
                throw new IOException("Invalid inheritance map file line: " + line);
            }

            String className = tokens[0];
            List<String> parents = Arrays.asList(tokens).subList(1, tokens.length);

            if (classMap == null) {
                setParents(className, new ArrayList<String>(parents));
            } else {
                String remappedClassName = JarRemapper.mapTypeName(className, /*packageMap*/ null, classMap, /*defaultIfUnmapped*/ null);
                if (remappedClassName == null) {
                	printDebug("Inheritance map input class not remapped: " + className);
                } else {

	                ArrayList<String> remappedParents = new ArrayList<String>();
	                for (String parent : parents) {
	                    String remappedParent = JarRemapper.mapTypeName(parent, /*packageMap*/ null, classMap, /*defaultIfUnmapped*/ null);
	                    if (remappedParent == null) {
	                    	printDebug("Inheritance map parent class not remapped: " + parent);
	                    } else {
	
	                    	remappedParents.add(remappedParent);
	                    }
	                }
	
	                setParents(remappedClassName, remappedParents);
                }
            }
        }
    }
	
	private void printDebug(String out) throws IOException {
		if (errorOnNotRemap) {
			throw new IOException(out);
		}
		
		if (RemapUtilProvider.isNull()) {
			return;
		}
		if (RemapUtilProvider.get().shouldExtraDebugLog()) {
			System.out.println(out);
		}
	}
	
}
