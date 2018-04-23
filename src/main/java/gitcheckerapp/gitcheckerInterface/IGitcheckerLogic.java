/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gitcheckerapp.gitcheckerInterface;


import gitcheckerapp.gitcheckerLogic.ChangedFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;


/**
 *
 * @author FilipKrat
 */
public interface IGitcheckerLogic{

    boolean internetIsConnected();

    void setRepositoryPath(String URL);


    String getRepositoryPath();

    ArrayList<ChangedFile>getChangedFilesList(String owner, String repoName) throws IOException, ClassNotFoundException;
    
    void exportDataToExcel(String path) throws java.io.IOException;

    ArrayList<Integer> getDataForGraph(int fileIndex) throws IOException;
    
    void downloadFile(String path, int fileIndex) throws IOException;
    
    int getAllFilesLineNo();
}
