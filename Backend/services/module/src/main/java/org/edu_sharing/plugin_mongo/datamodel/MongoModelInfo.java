package org.edu_sharing.plugin_mongo.datamodel;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import lombok.Getter;
import org.edu_sharing.plugin_mongo.datamodel.mering.BaseMerger;
import org.edu_sharing.plugin_mongo.datamodel.mering.Merger;
import org.edu_sharing.plugin_mongo.datamodel.remover.BaseRemover;
import org.edu_sharing.plugin_mongo.datamodel.remover.Remover;

public class MongoModelInfo {
    @Getter
    private final String name;
//    @Getter
//    private String schema;
    @Getter
    private final Chainr alf2mongoChainr;
    @Getter
    private final Chainr mongo2alfChainr;

    @Getter
    private final Merger merger;
    @Getter
    private final Remover remover;

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
