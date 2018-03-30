/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gitcheckerapp;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import gitcheckerapp.gitcheckerLogic.ChangedFile;
import gitcheckerapp.gitcheckerLogic.GitCheckerLogic;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import static javafx.collections.FXCollections.observableArrayList;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;

/**
 *
 * @author FilipKrat
 */
public class HomeController implements Initializable{
    private GitCheckerLogic logic = new GitCheckerLogic();
    @FXML
    private JFXButton stateButton;
    @FXML
    private JFXButton filesButton;
    @FXML
    private JFXButton repositoryButton;
    @FXML
    private Pane statePane;
    @FXML
    private Pane repositoryPane;
    @FXML
    private Pane filesPane;
    @FXML
    private JFXTreeTableView<ChangedFile> table;
    @FXML
    private MaterialDesignIconView iconCheck;
    @FXML
    private MaterialDesignIconView iconCross;
    @FXML
    private Label internetConnectionLabel;
     @FXML
    private Label nextControlLabel;
     @FXML
    private JFXTextField repositoryName;
    
    
    
    
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
       checkConnection();
       refreshTable("https://github.com/jfoenixadmin/JFoenix");
    }  
    
    @FXML
    private void stateButtonClicked(){
        repositoryPane.setVisible(false);
        filesPane.setVisible(false);        
        statePane.setVisible(true);
    }
    
    @FXML
    private void repositoryButtonClicked(){
        repositoryPane.setVisible(true);
        filesPane.setVisible(false);        
        statePane.setVisible(false);
    }
    
    @FXML
    private void filesButtonClicked(){
        repositoryPane.setVisible(false);
        filesPane.setVisible(true);        
        statePane.setVisible(false);
    }
    
    private void checkConnection(){
        if(logic.internetIsConnected() == true){
            iconCross.setVisible(false);
            iconCheck.setVisible(true);
            internetConnectionLabel.setText("Jste připojeni k internetu");
            nextControlLabel.setVisible(true);
            nextControlLabel.setText("Další kontrola repozitáře za: ");
            refreshTable("https://github.com/jfoenixadmin/JFoenix");
        }else{
            iconCross.setVisible(true);
            iconCheck.setVisible(false);
            internetConnectionLabel.setText("Bez připojení k internetu");
            nextControlLabel.setVisible(false);
        }
    }
    
    private void refreshTable(String URL){
        ObservableList <ChangedFile> fileList = null;
        try {
            
            fileList= FXCollections.observableArrayList(logic.getChangedFilesList(logic.getOwnerFromURL(URL), logic.getRepoFromURL(URL)));
        } catch (IOException ex) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        JFXTreeTableColumn<ChangedFile, String> fileNameCol = new JFXTreeTableColumn("Název souboru");
       fileNameCol.setMinWidth(120);
       fileNameCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ChangedFile, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ChangedFile, String> param) {
            return param.getValue().getValue().getFileName();
                    }
        });
       
       
       JFXTreeTableColumn changeDateCol = new JFXTreeTableColumn("Datum změny");
       changeDateCol.setMinWidth(100);
       changeDateCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ChangedFile, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ChangedFile, String> param) {
            return param.getValue().getValue().getDateOfChange();
                    }
        });
       JFXTreeTableColumn versionCol = new JFXTreeTableColumn("Verze");
       changeDateCol.setMaxWidth(80);
       JFXTreeTableColumn graphButtonCol = new JFXTreeTableColumn("Graf");
       JFXTreeTableColumn saveButtonCol = new JFXTreeTableColumn("Uložit");
        
        TreeItem <ChangedFile> root = new RecursiveTreeItem <ChangedFile>(fileList, RecursiveTreeObject::getChildren);
        table.getColumns().addAll(fileNameCol,changeDateCol,versionCol, graphButtonCol, saveButtonCol);
        table.setRoot(root);
        table.setShowRoot(false);
    }
}
