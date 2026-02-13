package org.cardboardpowered.mohistremap;

/**
 *
 * @author pyz
 * @date 2019/7/15 8:52 PM
 */
public class ProxyClassWriter {

    public static byte[] remapClass(byte[] code) {
        return RemapUtilProvider.get().remapFindClass(code);
    }
}
