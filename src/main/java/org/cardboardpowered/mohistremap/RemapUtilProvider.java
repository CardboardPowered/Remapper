package org.cardboardpowered.mohistremap;

public class RemapUtilProvider {

	private static IRemapUtils INSTANCE;
	
	public static boolean isNull() {
		return null == INSTANCE;
	}
	
	public static boolean has() {
		return null != INSTANCE;
	}
	
	public static IRemapUtils get() {
		return INSTANCE;
	}
	
	public static void setInstance(IRemapUtils utils) {
		INSTANCE = utils;
	}
	
}
