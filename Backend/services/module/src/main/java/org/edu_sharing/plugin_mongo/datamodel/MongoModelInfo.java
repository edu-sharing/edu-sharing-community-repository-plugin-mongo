package org.edu_sharing.plugin_mongo.datamodel;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import lombok.Getter;
import lombok.Value;
import org.edu_sharing.plugin_mongo.datamodel.mering.BaseMerger;
import org.edu_sharing.plugin_mongo.datamodel.mering.Merger;
import org.edu_sharing.plugin_mongo.datamodel.remover.BaseRemover;
import org.edu_sharing.plugin_mongo.datamodel.remover.Remover;

@Value
public class MongoModelInfo {
    String name;

    Chainr alf2mongoChainr;
    Chainr mongo2alfChainr;

    Merger merger;
    Remover remover;

    public MongoModelInfo(String name, String alf2mongo, String mongo2alf) {
        this(name, alf2mongo, mongo2alf, new BaseMerger(), new BaseRemover());
    }

    public MongoModelInfo(String name, String alf2mongo, String mongo2alf,  Merger merger) {
        this(name, alf2mongo, mongo2alf, merger, new BaseRemover());
    }

    public MongoModelInfo(String name, String alf2mongo, String mongo2alf,  Remover remover) {
        this(name, alf2mongo, mongo2alf, new BaseMerger(), remover);
    }

    public MongoModelInfo(String name, String alf2mongo, String mongo2alf, Merger merger, Remover remover) {
        this.name = name;
        ClassLoader classLoader = getClass().getClassLoader();
        this.alf2mongoChainr = Chainr.fromSpec(JsonUtils.jsonToList(classLoader.getResourceAsStream(alf2mongo)));
        this.mongo2alfChainr = Chainr.fromSpec(JsonUtils.jsonToList(classLoader.getResourceAsStream(mongo2alf)));
        this.merger = merger;
        this.remover = remover;
    }

}
