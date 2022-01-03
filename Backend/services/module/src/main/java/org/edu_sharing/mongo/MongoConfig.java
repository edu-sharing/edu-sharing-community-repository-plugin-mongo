package org.edu_sharing.mongo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MongoConfig implements Serializable {
    private String connectionString;
    private String database;
}
