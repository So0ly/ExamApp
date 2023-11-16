package PBS.service;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class Responser {
    public static Response responser(Response.Status status, Object entity, MediaType type){
        return Response.status(status).entity(entity).type(type).build();
    }
}
