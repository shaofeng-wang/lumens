/*
 * Copyright Lumens Team, Inc. All Rights Reserved.
 */
package com.lumens.engine;

import com.lumens.addin.AddinContext;
import com.lumens.addin.ServiceEntity;
import com.lumens.connector.ConnectorFactory;

/**
 *
 * @author Shaofeng Wang <shaofeng.wang@outlook.com>
 */
public class DefaultConnectorFactoryHolder implements ConnectorFactoryHolder {

    private AddinContext addinContext;

    public DefaultConnectorFactoryHolder(AddinContext ac) {
        addinContext = ac;
    }

    @Override
    public ConnectorFactory getFactory(String className) {
        ServiceEntity<ConnectorFactory> se = addinContext.getService(className);
        return se != null ? se.getService() : null;
    }
}
