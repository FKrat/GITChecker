package gitcheckerapp.gitcheckerLogic;

import java.io.Serializable;

/**
 * Created by Lukáš on 19.04.2018.
 */
public class StringChangedFile implements Serializable {
    String fileName;
    String dateOfChange;
    String changedLines;
    String version;
    boolean isJavaFile; //nemam starost
    String URL;
    String fileNameShorten;

    public StringChangedFile(String fileName, String dateOfChange, String changedLines, String version, Boolean isJavaFile, String URL, String fileNameShorten) {
        this.fileName = fileName;
        this.dateOfChange = dateOfChange;
        this.changedLines = changedLines;
        this.version = version;
        this.isJavaFile = isJavaFile;
        this.URL = URL;
        this.fileNameShorten = fileNameShorten;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDateOfChange() {
        return dateOfChange;
    }

    public String getChangedLines() {
        return changedLines;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getJavaFile() {
        return isJavaFile;
    }

    public String getURL() {
        return URL;
    }

    public String getFileNameShorten() {
        return fileNameShorten;
    }
}
