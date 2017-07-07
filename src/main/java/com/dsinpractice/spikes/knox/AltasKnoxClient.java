package com.dsinpractice.spikes.knox;

/**
 * Created by nixon on 1/7/17.
 */



import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class AltasKnoxClient {
    static final String JSON_MEDIA_TYPE = MediaType.APPLICATION_JSON + "; charset=UTF-8";

    public static void main(String args[]){
            ClientConfig config = new DefaultClientConfig();
            Client client = Client.create(config);
            WebResource service = client.resource("http://localhost.openstacklocal.com:21000/api/atlas/admin/session");
            Cookie cookie=new Cookie("hadoop-jwt", "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6IktOT1hTU08iLCJleHAiOjE1MDE5NzUzNTV9.K7lTaXniXJtrdkXAG_k7MwhWpL363NPC75fA4hvY7ilNXCOr-_gVV-ZLndVxMGFmbSl-cLHv3ormDH2W2hvUQhUDvXg3QN5iQGlEoVDPjO6O1yoNnMBW0VgVhAbSimRQ5NTMgAF09gkTsPIfG8Qhu2kcvkKEMFOfcsDyrKn2cRM");


        ClientResponse blogResponse = service
                    .accept(JSON_MEDIA_TYPE)
                    .type(JSON_MEDIA_TYPE).cookie(cookie)

                    .method(HttpMethod.GET, ClientResponse.class, null);


//            final ClientResponse blogResponse = service.cookie(cookie).get  (ClientResponse.class);
            final String blog = blogResponse.getEntity(String.class);
            System.out.println(blog);
        }

}
