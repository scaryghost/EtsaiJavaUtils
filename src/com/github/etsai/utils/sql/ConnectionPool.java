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
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class ConnectionPool {
    static class ConnectionInfo implements Comparator<Date> {
        public Date lastUsed;
        public Connection connection;

        @Override
        public int compare(Date o1, Date o2) {
            return o1.compareTo(o2);
        }
    }
    
    private int maxConnections;
    private String url;
    private Properties dbProps;
    private PriorityQueue<ConnectionInfo> availableConnections;
    private Map<Connection, ConnectionInfo> usedConnections;
    
    public ConnectionPool(int maxConnections) {
        this.maxConnections= maxConnections;
        dbProps= new Properties();
    }
    public ConnectionPool() {
        this(5);
    }
    public void setJdbcUrl(String url) {
        this.url= url;
    }
    public void setDbUser(String user) {
        dbProps.setProperty("user", user);
    }
    public void setDbPassword(String password) {
        dbProps.setProperty("password", password);
    }
    public void setMaxConnections(int maxConnections) {
        this.maxConnections= maxConnections;
    }
    public synchronized void release(Connection conn) {
        availableConnections.add(usedConnections.get(conn));
        usedConnections.remove(conn);
        notifyAll();
    }
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
    public void close() throws SQLException {
        while(!availableConnections.isEmpty()) {
            availableConnections.poll().connection.close();
        }
        for(Entry<Connection, ConnectionInfo> entry: usedConnections.entrySet()) {
            entry.getKey().close();
        }
    }
}
