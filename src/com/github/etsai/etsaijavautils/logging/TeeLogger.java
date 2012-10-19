/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.etsaijavautils.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Logs all output on STDOUT and STDERR to file
 * @author etsai
 */
public class TeeLogger extends OutputStream {
    public static FileWriter getFileWriter(String execName) throws IOException {
        String localHostAddress;
            
        try {
            localHostAddress= InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            ex.printStackTrace(System.err);
            localHostAddress= "unknown";
        }
        
        String filename= String.format("%s.%s.%tY%<tm%<td-%<tH%<tM%<tS.log",
                execName, localHostAddress, new Date());
        
        return new FileWriter(new File(filename)); 
    }
    
    FileWriter log;
    PrintStream oldStream;
    
    TeeLogger(FileWriter log, PrintStream oldStream) {
        this.log= log;
        this.oldStream= oldStream;
    }
    
    @Override
    public void flush() throws IOException {
        super.flush();
        log.flush();
        oldStream.flush();
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        log.close();
    }
    
    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
