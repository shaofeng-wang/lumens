/*
 * Copyright Lumens Team, Inc. All Rights Reserved.
 */
package com.lumens.connector.database.client;

import com.lumens.connector.database.Client;
import com.lumens.connector.database.DbUtils;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;

/**
 *
 * @author shaofeng wang
 */
public abstract class AbstractClient implements Client {
    protected URLClassLoader driverLoader;
    protected Driver driver;
    protected Connection conn;
    protected String connURL;
    protected String user;
    protected String password;
    protected boolean letterUpper;

    public AbstractClient(String driverURL, String driverClass, String connURL, String user, String password, boolean letterUpper) {
        try {
            driver = (Driver) DbUtils.getInstance(driverURL, driverClass);
            this.connURL = connURL;
            this.user = user;
            this.password = password;
            this.letterUpper = letterUpper;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}