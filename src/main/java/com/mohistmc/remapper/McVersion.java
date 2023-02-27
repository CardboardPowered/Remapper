package com.mohistmc.remapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Mgazul by MohistMC
 * @date 2023/2/28 0:46:04
 */

@ToString
@AllArgsConstructor
public enum McVersion {
    v1_19_3("v1_19_R2"),
    v1_18_2("v1_18_R2");

    @Getter
    final String obs_version;
}
