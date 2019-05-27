package com.dsinpractice.spikes.atlas;

import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.atlas.notification.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.notification.*;
import org.apache.atlas.notification.hook.*;
import org.apache.atlas.notification.*;
import org.apache.atlas.notification.hook.*;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.notification.hook.HookNotification.HookNotificationType;
public class AtlasKafkaLogReader {


    public static void main(String args[]) throws Exception {

    File file = new File(args[0]);

        readKafkaFailedLogs(file);

    }

    public static void readKafkaFailedLogs(File file) throws Exception {

        LineIterator iterator = FileUtils.lineIterator(file, "UTF-8");

        try {
            AtlasClient atlasClient = new AtlasClient(new String[]{"http://localhost:21000/"}, new String[]{"admin", "admin"});

            MessageDeserializer deserializer =  NotificationInterface.NotificationType.HOOK.getDeserializer();//new HookMessageDeserializer();

            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                String atlasJsonMessage = line.substring("2018-04-11 04:44:09,979".length());

                HookNotification.HookNotificationMessage message =  (HookNotification.HookNotificationMessage) deserializer.deserialize(atlasJsonMessage);
                System.out.println(" "+ message.getType());

                try {
                    switch (message.getType()) {
                        case ENTITY_CREATE:
                            HookNotification.EntityCreateRequest createRequest =
                                    (HookNotification.EntityCreateRequest) message;
                            atlasClient.createEntity(createRequest.getEntities());
                            break;

                        case ENTITY_PARTIAL_UPDATE:
                            HookNotification.EntityPartialUpdateRequest partialUpdateRequest =
                                    (HookNotification.EntityPartialUpdateRequest) message;
                            atlasClient.updateEntity(partialUpdateRequest.getTypeName(),
                                    partialUpdateRequest.getAttribute(),
                                    partialUpdateRequest.getAttributeValue(), partialUpdateRequest.getEntity());
                            break;

                        case ENTITY_DELETE:
                            HookNotification.EntityDeleteRequest deleteRequest =
                                    (HookNotification.EntityDeleteRequest) message;
                            atlasClient.deleteEntity(deleteRequest.getTypeName(),
                                    deleteRequest.getAttribute(),
                                    deleteRequest.getAttributeValue());
                            break;

                        case ENTITY_FULL_UPDATE:
                            HookNotification.EntityUpdateRequest updateRequest =
                                    (HookNotification.EntityUpdateRequest) message;
                            atlasClient.updateEntities(updateRequest.getEntities());
                            break;

                        // TODO add v2 notification type ENTITY_CREATE_V2, ENTITY_PARTIAL_UPDATE_V2, ENTITY_FULL_UPDATE_V2, ENTITY_DELETE_V2

                        default:
                            throw new IllegalStateException("Unhandled exception!");
                    }

                }catch (Exception e){

                }finally {

                }

                }

        } finally {
            LineIterator.closeQuietly(iterator);
        }


    }



}
