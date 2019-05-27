package com.dsinpractice.spikes.atlas;

import org.apache.atlas.AtlasServiceException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by nixon on 07/01/18.
 */
public class AtlasTest {
    public static void main(String[] args) throws Exception {
        int start = 0;
        int noOfrecords = 10;
        String atlasEndPoint = "http://localhost:21000";
        try{
             start = Integer.parseInt(args[0]);
            noOfrecords = Integer.parseInt(args[1]);
            atlasEndPoint = args[2];


        }catch (Exception e) {
            System.out.println(" args missing - start noOfRecords atlasEndPoint ");
        }
        AtlasClientCatchTimeout atlas = new AtlasClientCatchTimeout( atlasEndPoint, "admin", "admin");
        for (int i = start; i < start + noOfrecords ; i++) {
            atlas.createRawZoneFileEntity(String.format("test3_%d", i), String.format("test3_%d", i), String.format("test3_%d", i), "test", "x", "x", "x", "x", "x", -1, "x", "x");
            System.out.println(" Created  " +  start + i +" Entity");
        }
    }
}
