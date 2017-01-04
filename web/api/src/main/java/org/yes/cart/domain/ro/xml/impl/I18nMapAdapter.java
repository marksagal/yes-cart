/*
 * Copyright 2009 Denys Pavlov, Igor Azarnyi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.domain.ro.xml.impl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: denispavlov
 * Date: 20/08/2014
 * Time: 10:56
 */
public class I18nMapAdapter extends XmlAdapter<I18nMapAdapter.AdaptedMap, Map<String, String>> {

    @XmlType(name = "i18n-map")
    public static class AdaptedMap {

        public List<Entry> value = new ArrayList<Entry>();

    }

    @XmlType(name = "value")
    public static class Entry {

        @XmlAttribute(name = "lang")
        public String language;

        @XmlValue
        public String value;

    }

    @Override
    public Map<String, String> unmarshal(AdaptedMap adaptedMap) throws Exception {
        if (adaptedMap != null && adaptedMap.value != null && !adaptedMap.value.isEmpty()) {
            Map<String, String> map = new HashMap<String, String>();
            for(Entry entry : adaptedMap.value) {
                map.put(entry.language, entry.value);
            }
            return map;
        }
        return null;
    }

    @Override
    public AdaptedMap marshal(Map<String, String> map) throws Exception {
        if (map != null) {
            AdaptedMap adaptedMap = new AdaptedMap();
            for(Map.Entry<String, String> mapEntry : map.entrySet()) {
                Entry entry = new Entry();
                entry.language = mapEntry.getKey();
                entry.value = mapEntry.getValue();
                adaptedMap.value.add(entry);
            }
            return adaptedMap;
        }
        return null;
    }

}
