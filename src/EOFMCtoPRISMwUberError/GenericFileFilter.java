package EOFMCtoPRISMwUberError;

import java.io.File;

/**
 *
 * @author mlb4b
 */
public class GenericFileFilter extends javax.swing.filechooser.FileFilter {

    private String extension;
    private String description;
    
    GenericFileFilter(String extension, String description) {
        setExtension(extension);
        setDescription(description);
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        if (extension.startsWith("."))
            this.extension = extension.replaceFirst(".", "");
        else
            this.extension = extension;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean accept(File pathname) {
        return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(extension);
    }   
}