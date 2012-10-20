/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.utils.ant.taskdef;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 * @author eric
 */
public class Publish extends Task {
    private static String gitCommand= "git describe --dirty --long";
    
    private Boolean preserve= false;
    private File dest;
    private File srcJar;
    
    @Override
    public void execute() {
        try {
            FileUtils utils= FileUtils.getFileUtils();
            
            File baseDir= getProject().getBaseDir();
            Process proc= Runtime.getRuntime().exec(gitCommand, null, baseDir);
            String describe= new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
            String[] versionParts= describe.split("-");
            
            if (describe.contains("dirty")) {
                throw new BuildException("Current project is dirty.  Commit changes before publishing");
            }
            
            String version= versionParts[0] + ".";
            if (versionParts.length == 1) {
                version+= "0";
            } else {
                version+= versionParts[1];
            }
            
            String jar= srcJar.getName();
            String jarName= jar.substring(0, jar.lastIndexOf(".jar"));
            File newJar= new File(dest, String.format("%s-%s.jar", jarName, version));
            
            if (preserve) {
                utils.copyFile(srcJar, newJar);
            } else {
                utils.rename(srcJar, newJar);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            throw new BuildException("Error execuing command: " + gitCommand);
        }
    }
    
    public void setPreserve(String preserve) {
        this.preserve= Boolean.valueOf(preserve);
    }
    public void setSrcJar(String srcJar) {
        this.srcJar= new File(srcJar);
    }
    
    public void setDest(String dest) {
        this.dest= new File(dest);
    }
}
