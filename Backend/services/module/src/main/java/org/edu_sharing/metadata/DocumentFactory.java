package org.edu_sharing.metadata;

import java.util.Map;

public interface DocumentFactory {
    Document createDocument();

    Document createDocument(String key, Object value);

    Document createDocument(String json);
}
