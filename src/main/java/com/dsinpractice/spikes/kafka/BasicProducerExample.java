package com.dsinpractice.spikes.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Properties;
import java.util.Random;

public class BasicProducerExample {

    public static void main(String[] args){

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "node11:6667");

        //configure the following three settings for SSL Encryption
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/etc/security/kafka.server.truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  "password");

        // configure the following three settings for SSL Authentication
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/etc/security/kafka.server.keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "password");
        props.put("sasl.kerberos.service.name","kafka");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(props);


        String test = "{\"version\":{\"version\":\"1.0.0\"},\"message\":{\"entities\":[{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Reference\",\"id\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Id\",\"id\":\"-269129065644035\",\"version\":0,\"typeName\":\"hive_db\",\"state\":\"ACTIVE\"},\"typeName\":\"hive_db\",\"values\":{\"name\":\"default\",\"location\":\"hdfs://rs-atl-290517-1.openstacklocal:8020/apps/hive/warehouse\",\"description\":\"Default Hive database\",\"ownerType\":2,\"qualifiedName\":\"default@ce30\",\"owner\":\"public\",\"clusterName\":\"ce30\",\"parameters\":{}},\"traitNames\":[],\"traits\":{},\"systemAttributes\":{}},{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Reference\",\"id\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Id\",\"id\":\"-269129065644034\",\"version\":0,\"typeName\":\"hive_table\",\"state\":\"ACTIVE\"},\"typeName\":\"hive_table\",\"values\":{\"tableType\":\"MANAGED_TABLE\",\"name\":\"testtabl1e\",\"createTime\":\"2017-06-01T09:21:32.000Z\",\"temporary\":false,\"db\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Reference\",\"id\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Id\",\"id\":\"-269129065644035\",\"version\":0,\"typeName\":\"hive_db\",\"state\":\"ACTIVE\"},\"typeName\":\"hive_db\",\"values\":{\"name\":\"default\",\"location\":\"hdfs://rs-atl-290517-1.openstacklocal:8020/apps/hive/warehouse\",\"description\":\"Default Hive database\",\"ownerType\":2,\"qualifiedName\":\"default@ce30\",\"owner\":\"public\",\"clusterName\":\"ce30\",\"parameters\":{}},\"traitNames\":[],\"traits\":{},\"systemAttributes\":{}},\"retention\":0,\"qualifiedName\":\"default.testtabl1e@ce30\",\"columns\":[{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Reference\",\"id\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Id\",\"id\":\"-269129065644032\",\"version\":0,\"typeName\":\"hive_column\",\"state\":\"ACTIVE\"},\"typeName\":\"hive_column\",\"values\":{\"name\":\"name\",\"qualifiedName\":\"default.testtabl1e.name@ce30\",\"position\":0,\"owner\":\"anonymous\",\"type\":\"int\",\"table\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Id\",\"id\":\"-269129065644034\",\"version\":0,\"typeName\":\"hive_table\",\"state\":\"ACTIVE\"}},\"traitNames\":[],\"traits\":{},\"systemAttributes\":{}}],\"lastAccessTime\":\"2017-06-01T09:21:32.000Z\",\"owner\":\"anonymous\",\"sd\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Reference\",\"id\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Id\",\"id\":\"-269129065644033\",\"version\":0,\"typeName\":\"hive_storagedesc\",\"state\":\"ACTIVE\"},\"typeName\":\"hive_storagedesc\",\"values\":{\"location\":\"hdfs://rs-atl-290517-1.openstacklocal:8020/apps/hive/warehouse/testtabl1e\",\"serdeInfo\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Struct\",\"typeName\":\"hive_serde\",\"values\":{\"serializationLib\":\"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe\",\"parameters\":{\"serialization.format\":\"1\"}}},\"qualifiedName\":\"default.testtabl1e@ce30_storage\",\"outputFormat\":\"org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat\",\"compressed\":false,\"numBuckets\":-1,\"inputFormat\":\"org.apache.hadoop.mapred.TextInputFormat\",\"parameters\":{},\"storedAsSubDirectories\":false,\"table\":{\"jsonClass\":\"org.apache.atlas.typesystem.json.InstanceSerialization$_Id\",\"id\":\"-269129065644034\",\"version\":0,\"typeName\":\"hive_table\",\"state\":\"ACTIVE\"}},\"traitNames\":[],\"traits\":{},\"systemAttributes\":{}},\"parameters\":{\"rawDataSize\":\"0\",\"numFiles\":\"0\",\"transient_lastDdlTime\":\"1496308892\",\"totalSize\":\"0\",\"COLUMN_STATS_ACCURATE\":\"{\\\"BASIC_STATS\\\":\\\"true\\\"}\",\"numRows\":\"0\"},\"partitionKeys\":[]},\"traitNames\":[],\"traits\":{},\"systemAttributes\":{}}],\"type\":\"ENTITY_FULL_UPDATE\",\"user\":\"anonymous\"}}";

        TestCallback callback = new TestCallback();
        //for (long i = 0; i < 10 ; i++) {
            Random rnd = new Random();

            String table = "{\"version\":{\"version\":\"1.0.0\"},\"message\":{\"typeName\":\"hive_table\",\"attribute\":\"qualifiedName\",\"attributeValue\":\"default."+rnd+"\",\"type\":\"ENTITY_DELETE\",\"user\":\"ambari-qa\"}}";
            ProducerRecord<String, String> data = new ProducerRecord<String, String>(
                    "ATLAS_HOOK", "key-" , test );
            producer.send(data, callback);
        //}




        producer.close();
    }


    private static class TestCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null) {
                System.out.println("Error while producing message to topic :" + recordMetadata);
                e.printStackTrace();
            } else {
                String message = String.format("sent message to topic:%s partition:%s  offset:%s", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
                System.out.println(message);
            }
        }
    }

}