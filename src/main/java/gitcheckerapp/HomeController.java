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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 *
 * @author FilipKrat
 */
public class HomeController implements Initializable {
    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private IntegerProperty index = new SimpleIntegerProperty();
    public GitCheckerLogic logic = new GitCheckerLogic();
    private ObservableList<ChangedFile> fileList = null;

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
    private Label allFilesLineNoLabel;
    @FXML
    private Label nextControlLabelSmall;
    @FXML
    private JFXTextField repositoryName;
    @FXML
    private JFXButton setRepositoryButton;
    @FXML
    private JFXButton exportButton;

    private ScheduledService<List<String>> checkNet;
    private ScheduledService<Void> checkGit;

    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        //iconCross.setVisible(false);
        //iconCrossSmall.setVisible(false);
        checkConnection();
        checkNet = new ScheduledService<List<String>>() {
            @Override
            protected Task<List<String>> createTask() {
                return new Task<List<String>>() {
                    @Override
                    protected List<String> call() throws Exception {
                        List<String> result = new ArrayList<>();
                        if(logic.internetIsConnected()){
                            result.add("false");
                            result.add("true");
                            result.add("false");
                            result.add("true");
                            result.add("Jste připojeni k internetu");
                            result.add("true");


                        } else {
                            result.add("true");
                            result.add("false");
                            result.add("true");
                            result.add("false");
                            result.add("Bez připojení k internetu");
                            result.add("false");

                        }
                        return result;
                    }
                };
            }
        };

        checkNet.setDelay(new Duration(100));
        checkNet.setPeriod(new Duration(300000));
        checkNet.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                List<String> result = (List<String>)(event.getSource().getValue());
                String now = sdf.format(new Date(System.currentTimeMillis() + 3600000));
                iconCross.setVisible(Boolean.parseBoolean(result.get(0)));
                iconCheck.setVisible(Boolean.parseBoolean(result.get(1)));
                iconCrossSmall.setVisible(Boolean.parseBoolean(result.get(2)));
                iconCheckSmall.setVisible(Boolean.parseBoolean(result.get(3)));
                internetConnectionLabel.setText(result.get(4));
                nextControlLabelSmall.setText("Další kontrola v: "+now);
                nextControlLabel.setText("Další kontrola v: "+now);
            }

        });
        checkNet.start();
        checkGit = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        fileList = FXCollections.observableArrayList(logic.getChangedFilesList(logic.getOwnerFromURL(logic.getRepositoryPath()), logic.getRepoFromURL(logic.getRepositoryPath())));
                        return null;
                    }
                };
            }
        };
        checkGit.setPeriod(new Duration(3600000));
        checkGit.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                refreshList();
            }
        });
        checkGit.start();
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
    
    /**
     * Metoda kontorluje připojení a v závislosti na tom mění stavy komponent
     */
    private void checkConnection() {
        if (logic.internetIsConnected() == true) {
            iconCross.setVisible(false);
            iconCheck.setVisible(true);
            iconCrossSmall.setVisible(false);
            iconCheckSmall.setVisible(true);
            //internetConnectionLabelSmall.setText("Jste připojeni k internetu");
            internetConnectionLabel.setText("Jste připojeni k internetu");
            nextControlLabel.setVisible(true);
            nextControlLabelSmall.setVisible(true);
            nextControlLabel.setText("Další kontrola repozitáře za: ");
            nextControlLabelSmall.setText("Další kontrola: ");
            try{
                boolean b = logic.loadBackup();
                if (b) { //pokud soubor existuje a něco tam je tak to z něj nahraje
                    repositoryName.setText(logic.getRepositoryPath());
                }
            } catch (Exception ex){
                System.out.println("Neexistuje záloha");
            }
            if (logic.getRepositoryPath() != null) {
                refreshTable(logic.getRepositoryPath());
                repositoryName.setText(logic.getRepositoryPath());
            }
        } else {
            //tenhle try catch jsem pridal celej
            //proste pokud nejde net, nahraju zalohu, z ní vytáhnu URL a střelim ti to do
            //refreshTable, ofc je možné na to udělat metodu v logice
            try{
                boolean b = logic.loadBackup();
                if (b == true) { //pokud soubor existuje a něco tam je tak to z něj nahraje
                    refreshTable(logic.getRepositoryPath());
                    repositoryName.setText(logic.getRepositoryPath());
                }
            } catch (Exception ex){
                System.out.println("problem: " + ex.getMessage());
            }

            iconCross.setVisible(true);
            iconCheck.setVisible(false);
            iconCrossSmall.setVisible(true);
            iconCheckSmall.setVisible(false);
            internetConnectionLabel.setText("Bez připojení k internetu");
            //nextControlLabel.setVisible(false);
            internetConnectionLabelSmall.setText("Bez připojení k internetu");
            //nextControlLabelSmall.setVisible(false);
        }
    }
    
    /**
     * Metoda nastaví repozitář pro kontrolu a zavolá aktualizaci tabulky
     */
    @FXML
    private void setRepositoryButtonClicked() {
        String repUrl = repositoryName.getText();
        logic.setRepositoryPath(repUrl);
        /*Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                checkConnection();
                if (logic.getRepositoryPath() != null) {
                    refreshTable(logic.getRepositoryPath());
                } else {
                    iconCrossSmall.setVisible(true);
                }
                return null;
            }
        };
        new Thread(task).start();*/
        checkConnection();
        if (logic.getRepositoryPath() != null) {
            fileList.clear();
            table.getColumns().clear();
            table.refresh();
            refreshTable(logic.getRepositoryPath());
        } else {
            iconCrossSmall.setVisible(true);
        }
    }
    
    /**
     * Metoda aktualizuje list a předpokládá již použitou metodu refreshTable, 
     * aby bylo vloženo do tabulky
     */
    public void refreshList(){
       
        fileList.clear();
        table.getColumns().clear();
        table.refresh();
        refreshTable(logic.getRepositoryPath());
        
    }
    
    /**
     * Metoda nastavuje tabulku a její parametry
     * @param URL url repo
     */
    private void refreshTable(String URL) {
        
        try {
            fileList = FXCollections.observableArrayList(logic.getChangedFilesList(logic.getOwnerFromURL(URL), logic.getRepoFromURL(URL)));
            allFilesLineNoLabel.setText("Počet řádků v souborech: "+ logic.getAllFilesLineNo());
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(fileList == null){
            fileList = FXCollections.observableArrayList(logic.getCfl());
        }
        if(fileList!=null){
        allFilesLineNoLabel.setText("Počet řádků v souborech: "+ logic.getAllFilesLineNo());
        JFXTreeTableColumn<ChangedFile, String> fileNameCol = new JFXTreeTableColumn("Název souboru");
        fileNameCol.setMinWidth(280);
        fileNameCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ChangedFile, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ChangedFile, String> param) {
                return param.getValue().getValue().getFileNameShortenProperty();
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
        table.refresh();
        }else{
            allFilesLineNoLabel.setText("Počet řádků v souborech: "+ 0);
        }
    }
    
    /**
     * Metoda obstarává načtení cesty složky pro uložení a 
     * zavolání metody na generování .xlsx
     */
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
                        series.getData().add(new XYChart.Data<>(i, data.get(i)));
                    }
                    lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                    
                    Scene scene = new Scene(lineChart, 800, 600);
                  
                    lineChart.getData().add(series);
                    lineChart.setLegendVisible(false);
                    stage.setTitle("Graf vývoje řádků souboru "+ logic.getCfl().get(currentFile).getFileName().getValue());
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
