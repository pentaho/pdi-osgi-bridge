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
package org.pentaho.di.osgi;

import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;

/**
 * Created by nbaker on 2/18/15.
 */
public enum KettleLifecycleEvent {
    STOPPED, INIT, SHUTDOWN
}
