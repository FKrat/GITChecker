package gitcheckerapp.gitcheckerLogic;


import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import java.util.Date;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * Created by Lukáš on 27.03.2018.
 */
public class ChangedFile extends RecursiveTreeObject<ChangedFile>{
    StringProperty dateOfChange;
    StringProperty fileName;
    StringProperty changedLines;

    public ChangedFile() {
    }

    public ChangedFile(Date dateOfChange, String fileName, Integer changedLines) {
        this.dateOfChange = new SimpleStringProperty(dateOfChange.toString());
        this.fileName = new SimpleStringProperty(fileName);
        this.changedLines = new SimpleStringProperty(changedLines.toString());
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

    public void setDateOfChange(Date dateOfChange) {
        this.dateOfChange = new SimpleStringProperty(dateOfChange.toString());
    }

    public void setFileName(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
    }

    public void setChangedLines(Integer changedLines) {
        this.changedLines = new SimpleStringProperty(changedLines.toString());
    }
}
