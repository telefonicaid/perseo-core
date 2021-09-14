/**
* Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
*
* This file is part of perseo-core project.
*
* perseo-core is free software: you can redistribute it and/or modify it under the terms of the GNU
* General Public License version 2 as published by the Free Software Foundation.
*
* perseo-core is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
* for more details.
*
* You should have received a copy of the GNU General Public License along with perseo-core. If not, see
* http://www.gnu.org/licenses/.
*
* For those usages not covered by the GNU General Public License please contact with
* iot_support at tid dot es
*/

package com.telefonica.iot.perseo.test;
// import com.espertech.esper.client.EventBean;
// import com.espertech.esper.client.EventPropertyDescriptor;
// import com.espertech.esper.client.EventPropertyGetter;
// import com.espertech.esper.client.EventPropertyGetterIndexed;
// import com.espertech.esper.client.EventPropertyGetterMapped;
// import com.espertech.esper.client.EventType;
// import com.espertech.esper.client.FragmentEventType;
// import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventPropertyDescriptor;
import com.espertech.esper.common.client.EventPropertyGetter;
import com.espertech.esper.common.client.EventPropertyGetterIndexed;
import com.espertech.esper.common.client.EventPropertyGetterMapped;
import com.espertech.esper.common.client.meta.EventTypeMetadata;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.FragmentEventType;
import com.espertech.esper.common.client.PropertyAccessException;
import com.espertech.esper.common.client.type.EPTypeClass:
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author brox
 */
public class EventBeanMock implements EventBean {

    private Map<String,String> properties;

    public EventBeanMock(Map props) {
        this.properties = props;
    }

    EventType et = new EventType() {

        @Override
        public Class getPropertyType(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EventPropertyGetter getGetter(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public FragmentEventType getFragmentType(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Class getUnderlyingType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String[] getPropertyNames() {
            return properties.keySet().toArray(new String[0]);
        }

        @Override
        public EventPropertyDescriptor[] getPropertyDescriptors() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EventPropertyDescriptor getPropertyDescriptor(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EventType[] getSuperTypes() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Iterator<EventType> getDeepSuperTypes() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EventPropertyGetterMapped getGetterMapped(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EventPropertyGetterIndexed getGetterIndexed(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        // @Override
        // public int getEventTypeId() {
        //     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        // }

        @Override
        public String getStartTimestampPropertyName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getEndTimestampPropertyName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EventTypeMetadata getMetadata() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public java.util.Set<EventType> getDeepSuperTypesAsSet(){
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    @Override
    public EventType getEventType() {
        return et;
    }

    @Override
    public Object get(String string) throws PropertyAccessException {
        return properties.get(string);
    }

    @Override
    public Object getUnderlying() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public EPTypeClass getUnderlyingEPType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getFragment(String string) throws PropertyAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
