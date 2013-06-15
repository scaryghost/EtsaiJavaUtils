/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.utils.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a pool of connections to an SQL database.
 * @author etsai
 */
public class ConnectionPool {
    /**
     * Stores state information for a connection in the form of a tuple
     * @author etsai
     */
    static class ConnectionInfo implements Comparator<Date> {
        /** Last time the connection was removed from the pool */
        public Date lastUsed;
        /** Connection to the database */
        public Connection connection;

        @Override
        public int compare(Date o1, Date o2) {
            return o1.compareTo(o2);
        }
    }
    
    /** Max number of connections in the pool */
    private int maxConnections;
    /** URL to the database */
    private String url;
    /** Properties for connecting to the database */
    private Properties dbProps;
    /** Open connections that are not being used, ordered by last used date */
    private PriorityQueue<ConnectionInfo> availableConnections;
    /** Connections that are currently being used */
    private Map<Connection, ConnectionInfo> usedConnections;
    
    /**
     * Creates a pool with a configurable connection limit
     * @param   maxConnections      Max number of connections in the pool
     */
    public ConnectionPool(int maxConnections) {
        this.maxConnections= maxConnections;
        dbProps= new Properties();
        availableConnections= new PriorityQueue<>();
        usedConnections= new HashMap<>();
    }
    /**
     * Creates a pool with the default number of 5 connections
     */
    public ConnectionPool() {
        this(5);
    }
    /**
     * Set the URL to the database
     * @param   url     URL to the database
     */
    public void setJdbcUrl(String url) {
        this.url= url;
    }
    /**
     * Set the user name for logging into the database
     * @param   user    Username to login
     */
    public void setDbUser(String user) {
        dbProps.setProperty("user", user);
    }
    /**
     * Set the password for logging into the database
     * @param   password    Password to login
     */
    public void setDbPassword(String password) {
        dbProps.setProperty("password", password);
    }
    /**
     * Set the max number of connections the pool will hold
     * @param   maxConnections  Max number of open connections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections= maxConnections;
    }
    /**
     * Set the JDBC driver name that is needed to connect to the database
     * @param   driver  JDBC driver name
     * @throws ClassNotFoundException If the driver class cannot be loaded
     */
    public void setDbDriver(String driver) throws ClassNotFoundException {
        Class.forName(driver);
   }
    /**
     * Release a connection, adding it back to the pool of available connections.  If the connection 
     * to release is null or not a valid used connection in the pool, this function will not do anything
     * @param   conn    Connection to release
     */
    public synchronized void release(Connection conn) {
        if (conn != null && usedConnections.containsKey(conn)) {
            availableConnections.add(usedConnections.get(conn));
            usedConnections.remove(conn);
            notifyAll();
        }
    }
    /**
     * Get a connection from the pool.  If the pool is empty but max connections not reached, a new 
     * connection will be opened.  If max connections has been reached, the function will block until 
     * a connection is available.
     * @throws SQLException If a connection cannot be made to the database
     */
    public synchronized Connection getConnection() throws SQLException {
        if (availableConnections.isEmpty()) {
            if (usedConnections.keySet().size() >= maxConnections) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConnectionPool.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                ConnectionInfo connInfo= new ConnectionInfo();
                connInfo.lastUsed= Calendar.getInstance().getTime();
                connInfo.connection= DriverManager.getConnection(url, dbProps);
                availableConnections.add(connInfo);
            }
        }
        usedConnections.put(availableConnections.peek().connection, availableConnections.peek());
        return availableConnections.poll().connection;
    }
    /**
     * Close all connections in the pool
     * @throws SQLException If an error occured from closing a connection
     */
    public void close() throws SQLException {
        while(!availableConnections.isEmpty()) {
            availableConnections.poll().connection.close();
        }
        for(Entry<Connection, ConnectionInfo> entry: usedConnections.entrySet()) {
            entry.getKey().close();
        }
    }
}
