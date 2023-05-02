package com.mohistmc.remapper;

/**
 * @author Cardboard - 5/1/23
 * @author Mgazul by MohistMC
 * @date 2023/2/28 0:46:04
 */
public enum McVersion {

    v1_19_4("v1_19_R3"),
    v1_19_3("v1_19_R2"),
    v1_19_2("v1_19_R2"),
    v1_18_2("v1_18_R2");

    final String obs_version;
	
	private McVersion(String obs_version) {
		this.obs_version = obs_version;
	}
	
	public String getObs_version() {
		return obs_version;
	}
	
	@Override
	public String toString() {
		return "McVersion(" + this.obs_version + ")";
	}

}