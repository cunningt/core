/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.switchyard.wrapper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.switchyard.Context;

/**
 * A serializable version of Context wherein all the values stored
 * are themselves serializable.
 *
 * @author tcunning
 */
public class SerializableContextWrapper implements Context, Serializable {

    private ConcurrentHashMap<String, Serializable> _properties =
        new ConcurrentHashMap<String, Serializable>();

    public SerializableContextWrapper(final Context context) {
        Map<String, Object> properties = context.getProperties();
        for (String key : properties.keySet()) {
            Object obj = properties.get(key);

            if (obj instanceof Serializable) {
                _properties.put(key, (Serializable) obj);
            }
        }
    }

    @Override
    public Object getProperty(final String name) {
        return _properties.get(name);
    }

    @Override
    public Map<String, Object> getProperties() {
        // create a shallow copy to prevent against direct modification of
        // underlying context map
        return new HashMap<String, Object>(_properties);
    }

    @Override
    public boolean hasProperty(final String name) {
        return _properties.containsKey(name);
    }

    @Override
    public Object removeProperty(final String name) {
        return _properties.remove(name);
    }

    @Override
    public void setProperty(final String name, final Object val) {
        if (val instanceof Serializable) {
            Serializable serializableVal = (Serializable) val;
            _properties.put(name, serializableVal);
        } else {
            throw new RuntimeException("The value for the key "
                    + name + " is not serializable and not "
                    + "allowable within " + SerializableContextWrapper.class);
        }
    }
}
