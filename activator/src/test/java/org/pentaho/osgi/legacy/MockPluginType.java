/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
