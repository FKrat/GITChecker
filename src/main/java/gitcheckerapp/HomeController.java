/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gitcheckerapp;

import com.jfoenix.controls.*;
import com.jfoenix.controls.cells.editors.base.JFXTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import gitcheckerapp.gitcheckerLogic.ChangedFile;
import gitcheckerapp.gitcheckerLogic.GitCheckerLogic;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * @author FilipKrat
 */
public class HomeController implements Initializable {
    
    private IntegerProperty index = new SimpleIntegerProperty();
    public GitCheckerLogic logic = new GitCheckerLogic();
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
    private MaterialDesignIconView iconCheckSmall;
    @FXML
    private MaterialDesignIconView iconCrossSmall;
    @FXML
    private Label internetConnectionLabel;
    @FXML
    private Label nextControlLabel;
    @FXML
    private Label internetConnectionLabelSmall;
    @FXML
    private Label nextControlLabelSmall;
    @FXML
    private JFXTextField repositoryName;
    @FXML
    private JFXButton setRepositoryButton;
    @FXML
    private JFXButton exportButton;
    
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        checkConnection();
    }
    
    @FXML
    private void stateButtonClicked() {
        repositoryPane.setVisible(false);
        filesPane.setVisible(false);
        statePane.setVisible(true);
        statePane.toFront();
    }
    
    @FXML
    private void repositoryButtonClicked() {
        repositoryPane.setVisible(true);
        filesPane.setVisible(false);
        statePane.setVisible(false);
        repositoryPane.toFront();
    }
    
    @FXML
    private void filesButtonClicked() {
        repositoryPane.setVisible(false);
        filesPane.setVisible(true);
        statePane.setVisible(false);
        filesPane.toFront();
    }
    
    private void checkConnection() {
        if (logic.internetIsConnected() == true) {
            iconCross.setVisible(false);
            iconCheck.setVisible(true);
            iconCrossSmall.setVisible(false);
            iconCheckSmall.setVisible(true);
            internetConnectionLabelSmall.setText("Jste připojeni k internetu");
            internetConnectionLabel.setText("Jste připojeni k internetu");
            nextControlLabel.setVisible(true);
            nextControlLabelSmall.setVisible(true);
            nextControlLabel.setText("Další kontrola repozitáře za: ");
            nextControlLabelSmall.setText("Další kontrola: ");
            if (logic.getRepositoryPath() != null) {
                //refreshTable(logic.getRepositoryPath());
            }
        } else {
            //refreshTable(logic.getRepositoryPath());
            iconCross.setVisible(true);
            iconCheck.setVisible(false);
            iconCrossSmall.setVisible(true);
            iconCheckSmall.setVisible(false);
            internetConnectionLabel.setText("Bez připojení k internetu");
            nextControlLabel.setVisible(false);
            internetConnectionLabelSmall.setText("Bez připojení k internetu");
            nextControlLabelSmall.setVisible(false);
        }
    }
    
    @FXML
    private void setRepositoryButtonClicked() {
        String repUrl = repositoryName.getText();
        logic.setRepositoryPath(repUrl);
        checkConnection();
        if (logic.getRepositoryPath() != null) {
            refreshTable(logic.getRepositoryPath());
        } else {
            iconCrossSmall.setVisible(true);
        }
    }
    
    private void refreshTable(String URL) {
        
        ObservableList<ChangedFile> fileList = null;
        try {
            fileList = FXCollections.observableArrayList(logic.getChangedFilesList(logic.getOwnerFromURL(URL), logic.getRepoFromURL(URL)));
        } catch (IOException ex) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        JFXTreeTableColumn<ChangedFile, String> fileNameCol = new JFXTreeTableColumn("Název souboru");
        fileNameCol.setMinWidth(280);
        fileNameCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ChangedFile, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ChangedFile, String> param) {
                return param.getValue().getValue().getFileName();
            }
        });
        
        JFXTreeTableColumn changeDateCol = new JFXTreeTableColumn("Datum změny");
        changeDateCol.setMinWidth(130);
        changeDateCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ChangedFile, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ChangedFile, String> param) {
                return param.getValue().getValue().getDateOfChange();
            }
        });
        
        JFXTreeTableColumn graphButtonCol = new JFXTreeTableColumn("Graf");
        //

        graphButtonCol.setSortable(false);
        graphButtonCol.setMinWidth(75);
        
        graphButtonCol.setCellValueFactory(
                new Callback<JFXTreeTableColumn.CellDataFeatures<ChangedFile, Boolean>, ObservableValue<Boolean>>() {
            
            @Override
            public ObservableValue<Boolean> call(JFXTreeTableColumn.CellDataFeatures<ChangedFile, Boolean> p) {
                
                return new SimpleBooleanProperty(p.getValue().getValue().getIsJavaFile().getValue() != false);
                
            }
        });
        
        graphButtonCol.setCellFactory(
                new Callback<JFXTreeTableColumn<ChangedFile, Boolean>, JFXTreeTableCell<ChangedFile, Boolean>>() {
            
            @Override
            public JFXTreeTableCell<ChangedFile, Boolean> call(JFXTreeTableColumn<ChangedFile, Boolean> p) {
                
                return new ButtonCell(logic);
            }
            
        });
        
        JFXTreeTableColumn saveButtonCol = new JFXTreeTableColumn("Uložit");
        saveButtonCol.setMinWidth(75);
        saveButtonCol.setCellValueFactory(
                new Callback<JFXTreeTableColumn.CellDataFeatures<ChangedFile, Boolean>, ObservableValue<Boolean>>() {
            
            @Override
            public ObservableValue<Boolean> call(JFXTreeTableColumn.CellDataFeatures<ChangedFile, Boolean> p) {
                
                return new SimpleBooleanProperty(true);
                
            }
        });
        
        saveButtonCol.setCellFactory(
                new Callback<JFXTreeTableColumn<ChangedFile, Boolean>, JFXTreeTableCell<ChangedFile, Boolean>>() {
            
            @Override
            public JFXTreeTableCell<ChangedFile, Boolean> call(JFXTreeTableColumn<ChangedFile, Boolean> p) {
                
                return new SaveButtonCell(logic);
            }
            
        });
        
        TreeItem<ChangedFile> root = new RecursiveTreeItem<ChangedFile>(fileList, RecursiveTreeObject::getChildren);
        table.getColumns().addAll(fileNameCol, changeDateCol, graphButtonCol, saveButtonCol);
        table.setRoot(root);
        table.setShowRoot(false);
    }
    
    @FXML
    private void exportExcelClicked(){
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(new Stage());
            logic.exportDataToExcel(selectedDirectory.getAbsolutePath());
            
        } catch (IOException ex) {
            System.out.println(ex);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Soubor neuložen");
            alert.setHeaderText(null);
            alert.setContentText("Bohužel se nám soubor nepodařilo uložit!");
            alert.showAndWait();
        }
    }
    
}



class ButtonCell extends JFXTreeTableCell<ChangedFile, Boolean> {
    
    GitCheckerLogic logic;
    Button cellButton = new Button("Graf");
    
    ButtonCell(GitCheckerLogic logic) {
        this.logic = logic;
        
        cellButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent t) {
                
                int currentFile = ButtonCell.this.getTreeTableRow().getIndex();
                try {
                    ArrayList<Integer> data = logic.getDataForGraph(currentFile);
                    Stage stage = new Stage();
                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();
                    
                    final LineChart<Number, Number> lineChart
                            = new LineChart<>(xAxis, yAxis);
                    
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    for (int i = 0; i < data.size(); i++) {
                        series.getData().add(new XYChart.Data<>(i+1, data.get(i)));
                    }
                    lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                    
                    Scene scene = new Scene(lineChart, 800, 600);
                    lineChart.getData().add(series);
                    lineChart.setLegendVisible(false);
                    
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException ex) {
                    System.out.println(ex);
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Graf nebyl načten");
                        alert.setHeaderText(null);
                        alert.setContentText("Bohužel se nám graf nepodařilo zobrazit!");
                        alert.showAndWait();
                }
                
                
            }
        });
    }
    //Display button if the row is not empty

    @Override
    protected void updateItem(Boolean t, boolean empty) {
        super.updateItem(t, empty);
        if (!empty) {
            
            if (t == false) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(cellButton);
            }
        } else {
            setGraphic(null);
            setText(null);
        }
    }
}

class SaveButtonCell extends JFXTreeTableCell<ChangedFile, Boolean> {
    
    GitCheckerLogic logic;
    Button cellButton = new Button("Uložit");
    
    SaveButtonCell(GitCheckerLogic logic) {
        this.logic = logic;
        cellButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent t) {
                
                int currentFile = SaveButtonCell.this.getTreeTableRow().getIndex();
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory = directoryChooser.showDialog(new Stage());
                //doplnit
                File initDir = null;
                if (initDir != null && initDir.exists()) {
                    if (initDir.isDirectory()) {
                        directoryChooser.setInitialDirectory(initDir);
                    } else {
                        directoryChooser.setInitialDirectory(initDir.getParentFile());
                    }
                } else {
                    directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                }
                if (selectedDirectory == null) {
                    
                } else {
                    try {
                        logic.downloadFile(selectedDirectory.getAbsolutePath(), currentFile);
                    } catch (IOException ex) {
                        System.out.println(ex);
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Soubor neuložen");
                        alert.setHeaderText(null);
                        alert.setContentText("Bohužel se nám soubor nepodařilo uložit!");
                        alert.showAndWait();
                    }
                    System.out.println(currentFile + " " + selectedDirectory.getAbsolutePath());
                }
                System.out.println(currentFile);
            }
        });
    }
    
    @Override
    protected void updateItem(Boolean t, boolean empty) {
        super.updateItem(t, empty);
        if (!empty) {
            
            if (t == false) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(cellButton);
            }
        } else {
            setGraphic(null);
            setText(null);
        }
    }
}
