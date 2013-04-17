/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.utils.sql;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.PriorityQueue;

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
    
    private ConnectionBuilder builder;
    private PriorityQueue<ConnectionInfo> availableConnections;
    private Map<Connection, ConnectionInfo> usedConnections;
    
    public ConnectionPool(ConnectionBuilder builder) {
        this.builder= builder;
    }
    public void release(Connection conn) {
        availableConnections.add(usedConnections.get(conn));
        usedConnections.remove(conn);
    }
    public Connection getConnection() {
        if (availableConnections.isEmpty()) {
            ConnectionInfo connInfo= new ConnectionInfo();
            connInfo.lastUsed= Calendar.getInstance().getTime();
            connInfo.connection= builder.create();
            availableConnections.add(connInfo);
        }
        usedConnections.put(availableConnections.peek().connection, availableConnections.peek());
        return availableConnections.poll().connection;
    }
}
