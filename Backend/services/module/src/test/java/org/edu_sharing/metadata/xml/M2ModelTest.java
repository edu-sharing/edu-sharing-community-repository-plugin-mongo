package org.edu_sharing.metadata.xml;

import org.junit.jupiter.api.Test;
import java.io.InputStream;

class M2ModelTest {

    @Test
    void createModel() {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("org/edu_sharing/model/metadata-model.xml");
        ModelDefinition model = ModelDefinition.createModel(xmlStream);
    }
}