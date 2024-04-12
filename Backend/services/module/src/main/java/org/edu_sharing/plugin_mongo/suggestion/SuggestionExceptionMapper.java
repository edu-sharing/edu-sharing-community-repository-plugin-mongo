package org.edu_sharing.plugin_mongo.suggestion;

import com.mongodb.MongoBulkWriteException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.springframework.data.mongodb.util.MongoDbErrorCodes;
import org.springframework.stereotype.Component;

@Component
public class SuggestionExceptionMapper implements ExceptionMapper<MongoBulkWriteException> {
    @Override
    public Response toResponse(MongoBulkWriteException e) {
        if(MongoDbErrorCodes.isDuplicateKeyCode(e.getCode())) {
            return Response.status(Response.Status.CONFLICT).entity("Entry with the same key found").build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong").build();
    }
}
