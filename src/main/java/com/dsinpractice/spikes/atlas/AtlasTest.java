package com.dsinpractice.spikes.atlas;

import org.apache.atlas.AtlasServiceException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by nixon on 07/01/18.
 */
public class AtlasTest {
    public static void main(String[] args) throws Exception {
        AtlasClientCatchTimeout atlas = new AtlasClientCatchTimeout("LOCAL", "admin", "admin");
        for (int i = 0; i < 500; i++) {
            atlas.createRawZoneFileEntity(String.format("test3_%d", i), String.format("test3_%d", i), String.format("test3_%d", i), "test", "x", "x", "x", "x", "x", -1, "x", "x");
            System.out.println(500 - i);
        }
    }
}
