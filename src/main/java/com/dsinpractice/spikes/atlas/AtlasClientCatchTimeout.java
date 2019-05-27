package com.dsinpractice.spikes.atlas;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientHandlerException;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.hook.AtlasHook;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.TypesDef;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.apache.atlas.typesystem.json.TypesSerialization;
import org.apache.atlas.typesystem.types.*;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created by AmirM on 03/08/17.
 */

final class AtlasEntityMapEntry<String, Referenceable> implements Map.Entry ,Serializable  {
    private final String entityId;
    private Referenceable entity;

    AtlasEntityMapEntry(String entityId, Referenceable entity) {
        this.entityId = entityId;
        this.entity = entity;
    }

    @Override
    public Object getKey() {
        return entityId;
    }

    @Override
    public Object getValue() {
        return entity;
    }

    @Override
    public Object setValue(Object entity) {
        this.entity = (Referenceable)entity;
        return null;
    }
}

class SSLVerificationOverrider {
    /**
     * this is a temporary solution to cancel java ssl verification while connecting to Atlas through https.
     * after setting the SSL certificates delete this class
     */
    public static void override() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509ExtendedTrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
                    public X509Certificate[] getAcceptedIssuers() {return null;}

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {}
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {}
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }
}

public class AtlasClientCatchTimeout extends AtlasHook {

    @Override
    protected String getNumberOfRetriesPropertyKey() {
        return null;
    }

    /**
     * this is a wrapper for the atlas client, written for the EDP.
     * on this object initialization, the Types will be created on atlas, if not already exist.
     * provide methods to create EDP entities and edit its attributes.
     * at a temporary solution, this client catch timeout exception since we have
     * security issues preventing from atlas to response the HTTP requests
     */

    // an Exception for EDP logic errors
    class DataLakeException extends Exception {
        DataLakeException(String messege) {super(messege);}
    }

    public static class Attributes {
        /**
         * list of all attributes for all types as published in EDP procedure
         * this class is meant so there will be no use of key strings in code,
         * also in case of change in attribute names, could be changed here only.
         * the class is public to the user, to be used in methods such as 'setAttr'.
         */
        public static final String FULLPATH = "fullPath";
        public static final String SYSPATH = "sysPath";
        public static final String SYSNAME = "sysName";
        public static final String SRCNAME = "srcName";
        public static final String CREATEDATE = "createDate";
        public static final String SRCTYPE = "srcType";
        public static final String ISDELETED = "isDeleted";
        public static final String SCHEME = "scheme";
        public static final String TTL = "TTL";
        public static final String MASKEDFIELDS = "maskedFields";
        public static final String LASTUPDATE = "lastUpdate";
        public static final String REPRESENTATION = "representation";
        public static final String NAME = "name";
        public static final String OWNER = "owner";
        public static final String DESCRIPTION = "description";
    }

    public static class URL {
        public static final String LOCAL = "http://localhost:21000";
        public static final String INT = "https://localhost:21443";
        public static final String PROD = "https://localhost:21443";
    }

    public static class Types {
        public static final String EDPLandingZoneFile = "sourcesLandingZone2";
        public static final String EDPRawZoneFile = "sourcesRawZoneTest";
        public static final String EDPRawZoneDir = "sources_dir";
    }

    private AtlasClient atlasClient;

    private static final AttributeDefinition[] landingZoneFileAttrDef = new AttributeDefinition[]{
            /*
            * a list of landing zone file attributes
            */
            TypesUtil.createUniqueRequiredAttrDef(Attributes.FULLPATH, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SYSPATH, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SYSNAME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SRCNAME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.CREATEDATE, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SRCTYPE, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.ISDELETED, DataTypes.BOOLEAN_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.REPRESENTATION, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.NAME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.OWNER, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.DESCRIPTION, DataTypes.STRING_TYPE)
    };

    private static final AttributeDefinition[] rawZoneFileAttrDef = new AttributeDefinition[]{

            /*
            * a list of raw zone file attributes.
            * raw zone file has an additional scheme & TTL according to the published EDP procedure
            * also, we use maskedFields for internal use.
             */

            // shared with landing zone file
            TypesUtil.createUniqueRequiredAttrDef(Attributes.FULLPATH, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SYSPATH, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SYSNAME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SRCNAME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.CREATEDATE, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SRCTYPE, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.ISDELETED, DataTypes.BOOLEAN_TYPE),
            // raw zone file
            TypesUtil.createRequiredAttrDef(Attributes.SCHEME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.TTL, DataTypes.INT_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.MASKEDFIELDS, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.REPRESENTATION, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.NAME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.OWNER, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.DESCRIPTION, DataTypes.STRING_TYPE)
    };

    private static final AttributeDefinition[] rawZoneDirAttrDef = new AttributeDefinition[]{
            // shared with zone file
            TypesUtil.createUniqueRequiredAttrDef(Attributes.FULLPATH, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SYSPATH, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SYSNAME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SRCNAME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.CREATEDATE, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.SRCTYPE, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.ISDELETED, DataTypes.BOOLEAN_TYPE),
            // directory has no TTL or maskedFields, but has extra attribute last update
            TypesUtil.createRequiredAttrDef(Attributes.SCHEME, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.LASTUPDATE, DataTypes.STRING_TYPE),
            TypesUtil.createRequiredAttrDef(Attributes.REPRESENTATION, DataTypes.STRING_TYPE)
    };

    public AtlasClientCatchTimeout(String url, String user, String pass) throws AtlasServiceException, InterruptedException, KeyManagementException, NoSuchAlgorithmException, DataLakeException {
//        String url = null;
//        switch (environment) {
//            case "LOCAL" : url = URL.LOCAL;
//                break;
//            case "INT" : url = URL.INT;
//                break;
//            case "PROD" : url = URL.PROD;
//                break;
//            default: throw new DataLakeException("environment parameter must be on of: LOCAL, INT, PROD");
//        }
        SSLVerificationOverrider.override();
        atlasClient = new AtlasClient(new String[]{url}, new String[]{user, pass});
        this.createTypes();
    }

    public void createTypes() throws AtlasServiceException, InterruptedException {
        HierarchicalTypeDefinition<ClassType> EDPLandingZoneFileTypeDef = TypesUtil.createClassTypeDef(Types.EDPLandingZoneFile, null , landingZoneFileAttrDef);
        HierarchicalTypeDefinition<ClassType> EDPRawZoneFileTypeDef = TypesUtil.createClassTypeDef(Types.EDPRawZoneFile, null , rawZoneFileAttrDef);
        HierarchicalTypeDefinition<ClassType> EDPRawZoneDirTypeDef = TypesUtil.createClassTypeDef(Types.EDPRawZoneDir, null , rawZoneDirAttrDef);

        createType(EDPLandingZoneFileTypeDef);
        createType(EDPRawZoneFileTypeDef);
        createType(EDPRawZoneDirTypeDef);

        // wait for the request to sync to atlas, and check types on atlas
        Thread.sleep(1000);
        /*
        List<String> typeList = atlasClient.listTypes();
        if (!typeList.contains(Types.EDPLandingZoneFile) | !typeList.contains(Types.EDPRawZoneFile) | !typeList.contains(Types.EDPRawZoneDir)) {
            throw new AtlasServiceException(new Exception(String.format("could not create all atlas type definitions")));
        }
        */
    }

    private void createType(HierarchicalTypeDefinition<ClassType> TypeDef) {
        TypesDef typeDef = TypesUtil.getTypesDef(ImmutableList.<EnumTypeDefinition>of(), ImmutableList.<StructTypeDefinition>of(), ImmutableList.<HierarchicalTypeDefinition<TraitType>>of(), ImmutableList.of(TypeDef));
        String typeDefJson = TypesSerialization.toJson(typeDef);
        try {
            this.atlasClient.createType(typeDefJson);
        } catch (ClientHandlerException e) {

        } catch (AtlasServiceException e) {

        }
    }

    public String createLandingZoneFileEntity(String fullPath,
                                              String sysPath,
                                              String sysName,
                                              String srcName,
                                              String created,
                                              String srcType,
                                              String fullOrInc,
                                              String description,
                                              String owner
    ) throws Exception {
        /*
        List<AtlasEntityMapEntry> entities = this.getEntity(Types.EDPLandingZoneFile, fullPath);
        if (entities.size() != 0) {
            throw new DataLakeException(String.format("entity with same fullPath already exist, ID: %s %s", entities.get(0).getKey(), entities.get(0).getValue()));
        }
        */

        // create json object of the entity
        Referenceable entity = new Referenceable(Types.EDPLandingZoneFile, new HashMap<String, Object>() {
        });
        entity.set(Attributes.FULLPATH, fullPath);
        entity.set(Attributes.SYSPATH, sysPath);
        entity.set(Attributes.SYSNAME, sysName);
        entity.set(Attributes.SRCNAME, srcName);
        entity.set(Attributes.CREATEDATE, created);
        entity.set(Attributes.SRCTYPE, srcType);
        entity.set(Attributes.REPRESENTATION, fullOrInc);
        entity.set(Attributes.DESCRIPTION, description);
        entity.set(Attributes.OWNER, owner);
        entity.set(Attributes.NAME, sysName+'|'+srcName);

        ArrayList<Referenceable> entityList = new ArrayList();
        entityList.add(entity);
        // isDeleted always 'false' on creation
        entity.set(Attributes.ISDELETED, false);

        String jsonEntity = InstanceSerialization.toJson(entity, true);

        return createEntity(jsonEntity);
    }

    public void createRawZoneFileEntity(String fullPath,
                                          String sysPath,
                                          String sysName,
                                          String srcName,
                                          String created,
                                          String srcType,
                                          String scheme,
                                          String maskedFields,
                                          String fullOrInc,
                                          int TTL,
                                          String description,
                                          String owner) throws Exception {

        /*
        List<AtlasEntityMapEntry> entities = this.getEntity(Types.EDPRawZoneFile, fullPath);
        if (entities.size() != 0) {
            throw new DataLakeException(String.format("entity with same fullPath already exist, ID: %s %s", entities.get(0).getKey(), entities.get(0).getValue()));
        }
        */


        // create json object of the entity
        Referenceable entity = new Referenceable(Types.EDPRawZoneFile, new HashMap<String, Object>() {
        });
        entity.set(Attributes.MASKEDFIELDS, maskedFields);
        entity.set(Attributes.FULLPATH, fullPath);
        entity.set(Attributes.SYSPATH, sysPath);
        entity.set(Attributes.SYSNAME, sysName);
        entity.set(Attributes.SRCNAME, srcName);
        entity.set(Attributes.CREATEDATE, created);
        entity.set(Attributes.SRCTYPE, srcType);
        entity.set(Attributes.REPRESENTATION, fullOrInc);
        entity.set(Attributes.DESCRIPTION, description);
        entity.set(Attributes.OWNER, owner);
        entity.set(Attributes.NAME, sysName+'|'+srcName);


        // isDeleted always 'false' on creation
        entity.set(Attributes.ISDELETED, false);

        entity.set(Attributes.SCHEME, scheme);
        entity.set(Attributes.TTL, TTL);
        ArrayList<Referenceable> entityList = new ArrayList();
        entityList.add(entity);
//        String jsonEntity = InstanceSerialization.toJson(entity, true);

        notifyEntities("testAdmin",entityList );

       // return createEntity(jsonEntity);
    }

    public String createOrUpdateDirEntity(String fullPath,
                                          String sysPath,
                                          String sysName,
                                          String srcName,
                                          String srcType,
                                          String scheme,
                                          String lastUpdate) throws Exception {

        return null;
    }


    private String getJson(String type,
                           String fullPath,
                           String sysPath,
                           String sysName,
                           String srcName,
                           String created,
                           String srcType,
                           String scheme,
                           int TTL,
                           String lastUpdate) throws Exception {

        // create a Referenceable object representing our entity to be submitted to atlas client
        Referenceable entity = new Referenceable(Types.EDPLandingZoneFile, new HashMap<String, Object>() {
        });

        entity.set(Attributes.FULLPATH, fullPath);
        entity.set(Attributes.SYSPATH, sysPath);
        entity.set(Attributes.SYSNAME, sysName);
        entity.set(Attributes.SRCNAME, srcName);
        entity.set(Attributes.CREATEDATE, created);
        entity.set(Attributes.SRCTYPE, srcType);
        // isDeleted always 'false' on creation
        entity.set(Attributes.ISDELETED, false);

        // for raw zone file and directory, we write also scheme
        if (!type.equals(Types.EDPLandingZoneFile)) {
            entity.set(Attributes.SCHEME, scheme);
            if (type.equals(Types.EDPRawZoneFile)) {
                entity.set(Attributes.TTL, TTL);
            } else {
                entity.set(Attributes.LASTUPDATE, lastUpdate);
            }
        }

        // convert the referenceable entity to json
        return InstanceSerialization.toJson(entity, true);
    }

    private String createEntity(String jsonEntity) throws DataLakeException, AtlasServiceException {
        // in case of files, submit json to atlas client
        try {
            List<String> guids = this.atlasClient.createEntity(jsonEntity);

            if(guids.size() == 0) {
                return null;
            }
            return guids.get(0);
        } catch (ClientHandlerException e) {
            return "once atlas response issue will be fixed, this should return the new entity ID";
        }
    }

    /*
    public void setAttr(String type, String fullPath, String attrName, Object attrValue) throws Exception {
        List<AtlasEntityMapEntry> entities = this.getEntity(type, fullPath);
        if (entities.size() > 1) {
            throw new DataLakeException(String.format("found more than one entity with same id: %s and %s", entities.get(0).getKey(), entities.get(1).getKey()));
        }
        if (entities.size() == 0) {
            throw new DataLakeException("entity not found. check fullPath?");
        }
        AtlasEntityMapEntry entity = entities.get(0);
        ((Referenceable)entity.getValue()).set(attrName, attrValue);
        try {
            this.atlasClient.updateEntity((String)entity.getKey(), (Referenceable)entity.getValue());
        } catch (ClientHandlerException e) {

        }
    }

    */
    public Object getAttr(String type, String fullPath, String attrName) throws Exception {
        AtlasEntityMapEntry entitiy = this.getEntity(type, fullPath);

        return ((Referenceable)(entitiy.getValue())).get(attrName);
    }

    public String getMaskedFields(String fullPath) throws Exception {
        return (String) this.getAttr(Types.EDPRawZoneFile, fullPath, Attributes.MASKEDFIELDS);
    }


    /*
    public void setMaskedFields(String fullPath, String maskedFields) throws Exception {
        this.setAttr(Types.EDPRawZoneFile, fullPath, Attributes.MASKEDFIELDS, maskedFields);
    }

    public void deleteEntity(String type, String fullPath) throws Exception {
        this.setAttr(type, fullPath, Attributes.ISDELETED, true);
    }

    */
    public AtlasEntityMapEntry getEntity (String type, String fullPath) throws AtlasServiceException, IOException, JSONException, DataLakeException {
        String query = String.format("%s where fullPath='%s'", type, fullPath);
        JSONArray results = this.atlasClient.searchByDSL(query, -1, -1);
        if (results.length() == 0) {
            return null;
        }
        String id = results.getJSONObject(0).getJSONObject("$id$").getString("id");
        if (results.length() > 1) {
            String id2 = results.getJSONObject(1).getJSONObject("$id$").getString("id");
            throw new DataLakeException(String.format("found more than one entity with same id: %s and %s", id, id2));
        }
        return new AtlasEntityMapEntry(id, atlasClient.getEntity(id));
    }


    private void clearTypeEntities(String Type) throws JSONException, IOException, AtlasServiceException {

        JSONArray results = this.atlasClient.searchByDSL(Type, -1, -1);

        List<AtlasEntityMapEntry> ret = new ArrayList<AtlasEntityMapEntry>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String id = result.getJSONObject("$id$").getString("id");
            ret.add(new AtlasEntityMapEntry(id , this.atlasClient.getEntity(id)));
        }

        for (AtlasEntityMapEntry<String, Referenceable> entity: ret) {
            this.atlasClient.deleteEntities((String)entity.getKey());
        }
    }

    public void clearAtlas() throws JSONException, IOException, AtlasServiceException {
        /**
         *
         */
        clearTypeEntities(Types.EDPLandingZoneFile);
        clearTypeEntities(Types.EDPRawZoneFile);
        clearTypeEntities(Types.EDPRawZoneDir);
    }

    private JSONObject searchByFullText(String query) throws AtlasServiceException {
        return this.atlasClient.searchByFullText(query, -1 ,-1 );
    }
}