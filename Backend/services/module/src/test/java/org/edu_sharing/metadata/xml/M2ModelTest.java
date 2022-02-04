package org.edu_sharing.metadata.xml;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.List;

class M2ModelTest {

    @Test
    void createModel() {
        InputStream stream1 = getClass().getResourceAsStream("org/edu_sharing/mapping/alf2lom.json");
        InputStream stream2 = getClass().getClassLoader().getResourceAsStream("org/edu_sharing/mapping/alf2lom.json");
        List<Object> array = JsonUtils.classpathToList("org/edu_sharing/mapping/alf2lom.json");
        Chainr test = Chainr.fromSpec(array);
    }
}
