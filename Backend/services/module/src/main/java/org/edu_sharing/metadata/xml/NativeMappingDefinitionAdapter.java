package org.edu_sharing.metadata.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class NativeMappingDefinitionAdapter extends XmlAdapter<String, MappingDefinition> {
    @Override
    public MappingDefinition unmarshal(String s) throws Exception {
        MappingDefinition mapping = new MappingDefinition();
        mapping.setName(s);
        return mapping;
    }

    @Override
    public String marshal(MappingDefinition mappingDefinition) throws Exception {
        return mappingDefinition.getName();
    }
}
