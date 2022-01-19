package org.edu_sharing.metadata.xml;

import org.apache.commons.lang.NotImplementedException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TypeConverterAdapter extends XmlAdapter<String, Converter> {
    @Override
    public Converter unmarshal(String s) throws Exception {
        return (Converter) Class.forName(s).newInstance();
    }

    @Override
    public String marshal(Converter converter) throws Exception {
        throw new NotImplementedException();
    }
}
