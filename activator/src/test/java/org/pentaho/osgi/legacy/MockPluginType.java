/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.legacy;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginFolderInterface;
import org.pentaho.di.core.plugins.PluginTypeInterface;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;

/**
 * Created by pminutillo on 11/11/2015.
 */
public class MockPluginType implements PluginTypeInterface {
    private static final String PLUGIN_ID = "testPluginId";

    public MockPluginType() {
    }

    public MockPluginType( String id ){

    }
    @Override
    public void addObjectType(Class<?> aClass, String s) {

    }

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<PluginFolderInterface> getPluginFolders() {
        return null;
    }

    @Override
    public void searchPlugins() throws KettlePluginException {

    }

    @Override
    public void handlePluginAnnotation(Class<?> aClass, Annotation annotation, List<String> list, boolean b, URL url) throws KettlePluginException {

    }
}
