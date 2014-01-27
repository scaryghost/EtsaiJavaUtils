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

/**
 * Polls the git repository for version and dirty status.  The information is pulled in 
 * from "git describe --dirty --long"
 * <h3>Attributes</h3>
 * <table border="1">
 * <thead>
 *      <tr>
 *          <th>Name</th>
 *          <th>Description</th>
 *          <th>Required</th>
 *      </tr>
 * </thead>
 * <tbody>
 *      <tr>
 *          <td>dirtyproperty</td>
 *          <td>Property name storing the dirty status</td>
 *          <td>No</td>
 *      </tr>
 *      <tr>
 *          <td>versionproperty</td>
 *          <td>Property name storing the project version</td>
 *          <td>No</td>
 *      </tr>
 * </tbody>
 * </table>
 * @author etsai
 */
public class Describe extends Task {
    private static String gitCommand= "git describe --dirty --long";
    private String dirtyProperty;
    private String versionProperty;
    
    private String join(String[] elements, String separator) {
        if (elements.length == 0) {
            return "";
        }
        
        int i= 1;
        String joinedStr= elements[0];
        for(;i < elements.length; i++) {
            joinedStr+= separator + elements[i];
        }
        return joinedStr;
    }
    /**
     * Executes the task.
     * @throws BuildException If git describe failed to run
     */
    @Override public void execute() throws BuildException {
        try {
            File baseDir= getProject().getBaseDir();
            Process proc= Runtime.getRuntime().exec(gitCommand, null, baseDir);
            String describe= new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
            String[] describeParts= describe.split("-");
            
            if (dirtyProperty != null) {
                getProject().setProperty(dirtyProperty, String.valueOf(describe.contains("dirty")));
            }
            if (versionProperty != null) {
                String[] versionParts= describeParts[0].split("\\.");
                String version;
                switch (versionParts.length) {
                    case 2:
                        if(describeParts.length == 1) {
                            version= describeParts[0] + ".0";   
                        } else {
                            version= describeParts[0] + "." + describeParts[1];
                        }
                        break;
                    case 3:
                        Integer newStep= Integer.valueOf(versionParts[2]) + Integer.valueOf(describeParts[1]);
                        
                        version= join(new String[] {versionParts[0], versionParts[1], newStep.toString()}, ".");
                        break;
                    default:
                        throw new BuildException("Git describe did not return a version in X.Y or X.Y.Z format: " + describeParts[0]);
                }
                getProject().setProperty(versionProperty, version);
            }
            
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            throw new BuildException("Error execuing command: " + gitCommand);
        }
    }
    
    /**
     * Set the dirtyproperty attribute.  The value of the attribute is the property name 
     * that stores the dirty status of the current repository.  The dirty status is polled 
     * from the git describe command
     * @param dirtyProperty Property name to store the dirty status
     */
    public void setDirtyProperty(String dirtyProperty) {
        this.dirtyProperty= dirtyProperty;
    }
    /**
     * Set the versionproperty attribute.  The value of the attribute is the property name 
     * that stores the version of the current project.  The version is generated from
     * the git describe command/
     * @param versionProperty Property name to store the project version
     */
    public void setVersionProperty(String versionProperty) {
        this.versionProperty= versionProperty;
    }
}
