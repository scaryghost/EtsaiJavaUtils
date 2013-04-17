/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.utils.sql;

import java.sql.Connection;

/**
 *
 * @author eric
 */
public class ConnectionBuilder {
    protected String url;
    
    public void setJdbcUrl(String url) {
        this.url= url;
    }
    public Connection create() {
        return null;
    }
}
