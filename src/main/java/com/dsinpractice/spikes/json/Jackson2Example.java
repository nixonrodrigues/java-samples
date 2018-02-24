package com.dsinpractice.spikes.json;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Jackson2Example {

    public static void main(String[] args) {
        Jackson2Example obj = new Jackson2Example();
        obj.run();
    }

    private void run() {
        ObjectMapper mapper = new ObjectMapper();

        try {

            // Convert JSON string from file to Object
            AtlasSimpleAuthzPolicy policy = mapper.readValue(new File("/Users/nixon/projects/apache-atlas-1.0.0-SNAPSHOT/conf/atlas-simple-authz-policy.json"), AtlasSimpleAuthzPolicy.class);
            System.out.println(policy);

//            // Convert JSON string to Object
//            String jsonInString = "{\"name\":\"mkyong\",\"salary\":7500,\"skills\":[\"java\",\"python\"]}";
//            Staff staff1 = mapper.readValue(jsonInString, Staff.class);
            System.out.println((policy.getRoles()));
            Map<String, AtlasSimpleAuthzPolicy.AtlasAuthzRole> role = policy.getRoles();

            for (Map.Entry<String, AtlasSimpleAuthzPolicy.AtlasAuthzRole> entry : role.entrySet()){
                System.out.println("Key = " + entry.getKey() +
                        ", Value = " + entry.getValue());

        }

            AtlasSimpleAuthzPolicy.AtlasAuthzRole role_admin  =      role.get("ROLE_ADMIN");

           List<AtlasSimpleAuthzPolicy.AtlasEntityPermission> permissionList = role_admin.getEntityPermissions();

            for(AtlasSimpleAuthzPolicy.AtlasEntityPermission permission:permissionList) {

                System.out.println(" AtlasEntityPermission permision"+ permission);
            }
            List<AtlasSimpleAuthzPolicy.AtlasTypePermission> permissionTypeList = role_admin.getTypePermissions();


            for(AtlasSimpleAuthzPolicy.AtlasTypePermission permission:permissionTypeList) {

                System.out.println(" AtlasTypePermission permision"+ permission);
            }




//            AtlasSimpleAuthzPolicy.AtlasAuthzRole role1 = role.get("entityPermissions");
//
//            role1.


            //Pretty print
//            String prettyStaff1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(staff1);
//            System.out.println(prettyStaff1);

        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
