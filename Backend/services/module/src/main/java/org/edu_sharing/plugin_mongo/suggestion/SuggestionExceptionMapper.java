package org.edu_sharing.plugin_mongo.suggestion;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.WriteError;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.edu_sharing.restservices.shared.ErrorResponse;
import org.springframework.data.mongodb.util.MongoDbErrorCodes;
import org.springframework.stereotype.Component;

@Component
public class SuggestionExceptionMapper implements ExceptionMapper<MongoBulkWriteException> {
    @Override
    public Response toResponse(MongoBulkWriteException e) {
        if((e.getWriteErrors().stream().map(WriteError::getCode).map(MongoDbErrorCodes::isDuplicateKeyCode).filter(x->x).findAny().orElse(false))) {
            return ErrorResponse.createResponse(e, Response.Status.CONFLICT);

        }
        return ErrorResponse.createResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
    }
}
