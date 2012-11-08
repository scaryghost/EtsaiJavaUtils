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
 * Renames the project jar file with the project version included in the name.  The information 
 * is pulled in from "git describe --dirty --long"
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
 *          <td>preserve</td>
 *          <td>If true, the original jar file will be kept otherwise it will be deleted.  
 *          If attribute is not specified, the attribute will default to true</td>
 *          <td>No</td>
 *      </tr>
 *      <tr>
 *          <td>srcjar</td>
 *          <td>Jar to be renamed</td>
 *          <td>Yes</td>
 *      </tr>
 *      <tr>
 *          <td>dest</td>
 *          <td>Destination folder the renamed jar will reside in</td>
 *          <td>Yes</td>
 *      </tr>
 * </tbody>
 * </table>
 * @author etsai
 */
public class Publish extends Task {
    private static String gitCommand= "git describe --dirty --long";
    
    private Boolean preserve= true;
    private File dest;
    private File srcJar;
    
    /**
     * Executes the task.
     * @throws BuildException If any of the required attributes are missing, there 
     * is an error renaming the jar, or git describe failed to run
     */
    @Override public void execute() throws BuildException {
        try {
            if (srcJar == null) {
                throw new BuildException("Required attribute srcjar not set");
            }
            if (dest == null) {
                throw new BuildException("Required attribute dest not set");
            }
            
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
            File newJar= new File(baseDir, new File(dest, String.format("%s-%s.jar", jarName, version)).toString());
            File fullSrcJar= new File(baseDir, srcJar.toString());
            
            if (preserve) {
                utils.copyFile(fullSrcJar, newJar);
                System.out.println("Jar copied to: " + newJar);
            } else {
                utils.rename(fullSrcJar, newJar);
                System.out.println("Jar renamed to: " + newJar);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            throw new BuildException("Error execuing command: " + gitCommand);
        }
    }
    
    /**
     * Sets the preserve attribute.  If the attribute is true (case insensitive), the 
     * original jar will be kept.  Otherwise, it will be deleted.
     * @param preserve Flag to determine if the original jar should be kept
     */
    public void setPreserve(String preserve) {
        this.preserve= Boolean.valueOf(preserve);
    }
    /**
     * Sets the srcjar attribute.  The attribute is the jar to be renamed.  This is a required attribute.
     * @param srcJar The jar to be renamed
     */
    public void setSrcJar(String srcJar) {
        this.srcJar= new File(srcJar);
    }
    /**
     * Sets the dest attribute.  The attribute is the folder the renamed jar should reside in.  
     * This is a required attribute.
     * @param dest Folder the renamed jar will be
     */
    public void setDest(String dest) {
        this.dest= new File(dest);
    }
}
