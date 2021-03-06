
package com.microsoft.bingads.v10.campaignmanagement;

import java.util.Collection;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Adapter10
    extends XmlAdapter<String, Collection<KeywordAdditionalField>>
{


    public Collection<KeywordAdditionalField> unmarshal(String value) {
        return (com.microsoft.bingads.v10.campaignmanagement.KeywordAdditionalFieldConverter.convertToList(value));
    }

    public String marshal(Collection<KeywordAdditionalField> value) {
        return (com.microsoft.bingads.v10.campaignmanagement.KeywordAdditionalFieldConverter.convertToString(value));
    }

}
