package org.edu_sharing.plugin_mongo.mongo;

import com.mongodb.MongoWriteException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.edu_sharing.restservices.shared.ErrorResponse;
import org.springframework.stereotype.Component;

@Component
public class MongoWriteExceptionMapper implements ExceptionMapper<MongoWriteException> {
    @Override
    public Response toResponse(MongoWriteException e) {
        ErrorResponse errorResponse = new ErrorResponse(e);
        switch (e.getError().getCategory()) {
            case DUPLICATE_KEY:
                return Response.status(Response.Status.CONFLICT).entity(errorResponse).build();
            case EXECUTION_TIMEOUT:
                return Response.status(Response.Status.GATEWAY_TIMEOUT).entity(errorResponse).build();
            default:
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }
}
