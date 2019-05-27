package com.dsinpractice.spikes.json;


import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import com.dsinpractice.spikes.json.AtlasSimpleAuthzPolicy.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AtlasSimpleAuthzUpdateTool {


    public static void main(String[] args) {

        updateSimpleAuthzJsonWithRelationshipPermissions("");

    }


    public static boolean  updateSimpleAuthzJsonWithRelationshipPermissions(String jsonConfPath ){

        try {


           // File fileToLoad = new File(jsonConfPath, "atlas-simple-authz-policy.json");

            ObjectMapper mapper = new ObjectMapper();

           // AtlasSimpleAuthzPolicy authzPolicy = AtlasJson.fromJson(targetStream, AtlasSimpleAuthzPolicy.class);
            AtlasSimpleAuthzPolicy authzPolicy = mapper.readValue(new File("/Users/nixon/projects/java-samples/atlas-simple-authz-policy.json"), AtlasSimpleAuthzPolicy.class);


            AtlasSimpleAuthzPolicy.AtlasAuthzRole dataAdmin = authzPolicy.getRoles().get("ROLE_ADMIN");

            List<String> wildCard = new ArrayList<String>();
            wildCard.add(".*");



            if (dataAdmin!=null  &&  dataAdmin.getRelationshipPermissions() == null) {
                AtlasSimpleAuthzPolicy.AtlasRelationshipPermission relationshipPermissions = new AtlasSimpleAuthzPolicy.AtlasRelationshipPermission();
                relationshipPermissions.setPrivileges(wildCard);

                relationshipPermissions.setRelationshipTypes(wildCard);

                relationshipPermissions.setEnd1EntityClassification(wildCard);
                relationshipPermissions.setEnd1EntityId(wildCard);
                relationshipPermissions.setEnd1EntityType(wildCard);

                relationshipPermissions.setEnd2EntityClassification(wildCard);
                relationshipPermissions.setEnd2EntityId(wildCard);
                relationshipPermissions.setEnd2EntityType(wildCard);

                List<AtlasSimpleAuthzPolicy.AtlasRelationshipPermission> relationshipPermissionsList = new ArrayList<AtlasSimpleAuthzPolicy.AtlasRelationshipPermission>();


                relationshipPermissionsList.add(relationshipPermissions);

                dataAdmin.setRelationshipPermissions(relationshipPermissionsList);
            }


            AtlasSimpleAuthzPolicy.AtlasAuthzRole dataSteward = authzPolicy.getRoles().get("DATA_STEWARD");
            List<String> permissiondataSteward = new ArrayList<String>();

            permissiondataSteward.add("add-relationship");
            permissiondataSteward.add("update-relationship");
            permissiondataSteward.add("remove-relationship");

            if (dataSteward!=null &&   dataSteward.getRelationshipPermissions() == null) {
                AtlasSimpleAuthzPolicy.AtlasRelationshipPermission relationshipPermissions = new AtlasSimpleAuthzPolicy.AtlasRelationshipPermission();
                relationshipPermissions.setPrivileges(permissiondataSteward);
                relationshipPermissions.setRelationshipTypes(wildCard);

                relationshipPermissions.setEnd1EntityClassification(wildCard);
                relationshipPermissions.setEnd1EntityId(wildCard);
                relationshipPermissions.setEnd1EntityType(wildCard);

                relationshipPermissions.setEnd2EntityClassification(wildCard);
                relationshipPermissions.setEnd2EntityId(wildCard);
                relationshipPermissions.setEnd2EntityType(wildCard);


                List<AtlasSimpleAuthzPolicy.AtlasRelationshipPermission> relationshipPermissionsList = new ArrayList<AtlasSimpleAuthzPolicy.AtlasRelationshipPermission>();
                relationshipPermissionsList.add(relationshipPermissions);
                dataSteward.setRelationshipPermissions(relationshipPermissionsList);
            }


         //   System.out.print("JSON => "+ AtlasJson.toJson(authzPolicy));

           // writeUsingFiles(toJson(authzPolicy , mapper));




        }catch (Exception e){

                e.printStackTrace();
        }

        return  false;
    }



    public static String toJson(Object obj, ObjectMapper mapper) {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String ret;
        try {
            if (obj instanceof JsonNode && ((JsonNode) obj).isTextual()) {
                ret = ((JsonNode) obj).textValue();
            } else {
                ret = mapper.writeValueAsString(obj);
            }
        }catch (IOException e){

            ret = null;
        }
        return ret;
    }


    private static void writeUsingFiles(String file, String data) {
        try {
            Files.write(Paths.get("/Users/nixon/projects/java-samples/output.json"), data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
