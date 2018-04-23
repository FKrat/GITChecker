package gitcheckerapp.gitcheckerLogic;


import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * Created by Lukáš on 27.03.2018.
 */
public class ChangedFile extends RecursiveTreeObject<ChangedFile> implements Serializable {
     StringProperty dateOfChange;
     StringProperty fileName;
     StringProperty changedLines;
     StringProperty version;
     BooleanProperty isJavaFile;
     String URL;



    public ChangedFile() {
    }

    public ChangedFile(Date dateOfChange, String fileName, Integer changedLines, String version, Boolean isJavaFile) {
        SimpleDateFormat sm = new SimpleDateFormat("dd.MM.yyyy-HH:mm", Locale.GERMANY);
        
        this.dateOfChange = new SimpleStringProperty(sm.format(dateOfChange.getTime()).toString());
        this.fileName = new SimpleStringProperty(fileName);
        this.changedLines = new SimpleStringProperty(changedLines.toString());
        this.version = new SimpleStringProperty(version);
        this.isJavaFile = new SimpleBooleanProperty(isJavaFile);
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }



    public StringProperty getDateOfChange() {
        return dateOfChange;
    }

    public StringProperty getFileName() {
        return fileName;
    }

    public StringProperty getChangedLines() {
        return changedLines;
    }

    public StringProperty getVersion() {return version;}

    public BooleanProperty getIsJavaFile() {return isJavaFile;}

    public void setDateOfChange(Date dateOfChange) {
        SimpleDateFormat sm = new SimpleDateFormat("dd.MM.yyyy-hh:mm");
        this.dateOfChange = new SimpleStringProperty(sm.format(dateOfChange).toString());
    }

    public void setFileName(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
    }

    public void setChangedLines(Integer changedLines) {
        this.changedLines = new SimpleStringProperty(changedLines.toString());
    }

    public void setVersion(String version) {this.version = new SimpleStringProperty(version);}

    public void setIsJavaFile(Boolean isJavaFile) {this.isJavaFile = new SimpleBooleanProperty(isJavaFile);}
}
