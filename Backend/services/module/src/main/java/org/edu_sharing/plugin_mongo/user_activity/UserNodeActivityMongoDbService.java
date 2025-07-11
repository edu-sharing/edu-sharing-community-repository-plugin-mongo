package org.edu_sharing.plugin_mongo.user_activity;

import lombok.RequiredArgsConstructor;
import org.edu_sharing.plugin_mongo.oplog.person.DeletePersonMongoAlfOpLogData;
import org.edu_sharing.plugin_mongo.oplog.person.MongodbPersonDeletedAware;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserNodeActivityMongoDbService implements MongodbPersonDeletedAware {


    private final UserNodeActivityDataRepository userNodeActivityDataRepository;


    @Override
    public void onPersonDeleted(DeletePersonMongoAlfOpLogData actionData) {
        userNodeActivityDataRepository.deleteAllByUserId(actionData.getNodeId());

    }
}

