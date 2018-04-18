package gitcheckerapp.gitcheckerLogic;
import gitcheckerapp.gitcheckerService.GitcheckerService;
import org.eclipse.egit.github.core.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gitcheckerapp.gitcheckerInterface.IGitcheckerLogic;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Created by Lukáš on 27.03.2018.
 */

public class GitCheckerLogic implements IGitcheckerLogic{
    private String username;
    private String pass;
    private String repUrl;
    private ArrayList<ChangedFile> cfl;
    private Repository rr;

    public GitCheckerLogic(){}

    public GitCheckerLogic(String username, String pass){
        this.username = username;
        this.pass = pass;
        this.repUrl = null;
    }

    /**
     * Metoda vrací vlastníka podle URL
     * @param URL URL repo
     * @return String vlastnik
     */
    public String getOwnerFromURL(String URL){
        String owner = "";

        String xx[] = URL.split("/");
        owner = xx[3];

        return owner;
    }

    /**
     * Metoda vrací nazev repo dle url
     * @param URL url repo
     * @return string nazev
     */
    public String getRepoFromURL(String URL){
        String repoName = "";
        String xx[] = URL.split("/");
        repoName = xx[4];

        return repoName;
    }

    GitcheckerService gcs = new GitcheckerService();
    GitcheckerService gcsUser = new GitcheckerService(username, pass);


    /**
     * Metoda vraci jméno repozitáře, podle jména a jeho vlastníka
     * @param owner Jméno vlastníka repositáře
     * @param repoName Název repositáře
     * @return Vrací string - název repositáře
     * @throws IOException
     */
    public String getUserRepository(String owner, String repoName) throws IOException{
       Repository repo;
       repo = gcs.getRepository(owner, repoName);
       return repo.getName();
    }

    /**
     * Metoda vrací repozitář pro zadaného vlastníka a dané jméno repo.
     * @param owner vlastník
     * @param repoName jméno repositáře
     * @return repo - objekt Repository
     * @throws IOException
     */
    public Repository getUserRepositoryObject(String owner, String repoName) throws IOException{
        Repository repo;
        repo = gcs.getRepository(owner, repoName);
        return repo;
    }

    /**
     * Metoda vrací názvy všech repozitářů pro zadaného uživatele
     * @param owner uživatel
     * @return Vrací List stringů (názvů repositířů)
     * @throws IOException
     */
    public List<String> getAllUserRepository(String owner) throws IOException{
        List<Repository> Lrepo;
        List<String> LfileNames = new ArrayList<>();
        Lrepo = gcs.getAllUserRepository(owner);

        for(Repository r : Lrepo){
            LfileNames.add(r.getName());
        }
        return LfileNames;
    }

    /**
     * Metoda vrací názvy všech repozitářů pro přihlášeného uživatele
     * @return List stringů (názvů repo)
     * @throws IOException
     */
    public List<String> getAllLoggedUserRepository() throws IOException{
        List<Repository> Lrepo;
        List<String> LfileNames = new ArrayList<>();
        Lrepo = gcsUser.getAllLoggedUserRepository();

        for(Repository r : Lrepo){
            LfileNames.add(r.getName());
        }
        return LfileNames;

    }

    /**
     * Metoda pro vypisování repositáře
     * @param owner vlastník repo
     * @param name název repo
     * @param path cesta k repo, pokud NULL vrací jen top lvl souborů a složek. Pokud cesta - vrací vše
     * @return List stringů - názvy
     * @throws IOException
     */
    public List<String> getAllFilesInRepository(String owner, String name, String path) throws IOException{
        Repository r;
        r = getUserRepositoryObject(owner, name);
        List<RepositoryContents> rc;
        List<String> LfileNames = new ArrayList<>();
        rc = gcs.getFilesInRepository(r, path);
        for(RepositoryContents rcc : rc){
            LfileNames.add(rcc.getName());
        }
        return LfileNames;
    }


    /**
     * Metoda vrací objekt změn
     * @param owner vlastník repo
     * @param repoName jméno repo
     * @return List objektů typu changedFiles
     * @throws IOException
     */
    @Override
    public ArrayList<ChangedFile> getChangedFilesList(String owner, String repoName) throws IOException{
        ChangedFile fdto;
        ArrayList<ChangedFile> Lfile = new ArrayList<>();
        RepositoryCommit rc;
        List<CommitFile> cf;

        Repository r;
        r = getUserRepositoryObject(owner, repoName);
        this.rr = r;

        rc = gcs.getLastRepositoryCommit(r);
        cf = rc.getFiles();


        for(CommitFile cff : cf){
            fdto = new ChangedFile();
            fdto.setFileName(cff.getFilename());
            fdto.setChangedLines(cff.getChanges());
            fdto.setDateOfChange(rc.getCommit().getAuthor().getDate());
            fdto.setVersion(rc.getSha());
            fdto.setURL(getRepositoryPath());
            if (cff.getFilename().contains(".java")){
                fdto.setIsJavaFile(true);
            } else {
                fdto.setIsJavaFile(false);
            }

            Lfile.add(fdto);

        }

        this.cfl = Lfile;
        //saveBackup(Lfile);
        return Lfile;

    }

    /**
     * Metoda zjišťuje stav internetu na počítači
     * @return true pokud je připojen k internetu
     * @return false pokud není připojen k internetu
     */
    @Override
    public boolean internetIsConnected() {
        try {
            URL url = new URL("https://github.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        } catch (Exception e){
            return false;
        }

    }

    /**
     * Metoda pro uložení objektů do souboru
     * @param acf arraylist objektů changedfile
     * @throws IOException
     */
    public void saveBackup(ArrayList<ChangedFile> acf) throws IOException{
        FileOutputStream fos = new FileOutputStream("backup.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        int size = acf.size();
        oos.writeInt(size);
        for(ChangedFile cf : acf){
            oos.writeObject(cf);
        }

        oos.close();
        fos.close();
    }

    /**
     * Metoda pro nahrání dat ze zálohy
     * @return arraylist changedfile
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public  ArrayList<ChangedFile> loadBackup() throws IOException, ClassNotFoundException {
        ArrayList<ChangedFile> load = new ArrayList<ChangedFile>();
        FileInputStream fis = new FileInputStream("backup.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);

        int size = ois.readInt();
        for(int i = 0; i< size; i++) {
            ChangedFile chf = ((ChangedFile) ois.readObject());
            load.add(chf);
        }
        ois.close();
        fis.close();

        return load;
    }

    @Override
    public void setRepositoryPath(String URL) {
           this.repUrl = URL;
    }


    @Override
    public String getRepositoryPath() {
        return this.repUrl;
    }

    @Override
    public LocalTime nextDownloadTime() {
        return null;
    }

    /**
     * Metoda pro exportování dat z tabulky do excelu
     * @throws java.io.IOException
     */
    @Override
    public void exportDataToExcel(String path) throws java.io.IOException{
        
        XSSFWorkbook workbook = new XSSFWorkbook();
        ArrayList<ChangedFile> acf = this.cfl;
        XSSFSheet sheet = workbook.createSheet("GitChecker data");// creating a blank sheet
        int rownum = 1;
        for (ChangedFile file : acf)
        {
            Row row = sheet.createRow(rownum++);
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("File Name");
            header.createCell(1).setCellValue("Version");
            header.createCell(2).setCellValue("Date of change");

            createList(file, row);

        }

        FileOutputStream out = new FileOutputStream(new File("excel.xlsx")); // file name with path
        workbook.write(out);
        out.close();

    }

    /**
     * Metoda pro vytvoření bunky pro každý řádek
     * @param file který soubor s atributy
     * @param row který řádek
     */
    private static void createList(ChangedFile file, Row row)
    {
        Cell cell = row.createCell(0);
        cell.setCellValue(file.getFileName().get());

        cell = row.createCell(1);
        cell.setCellValue(file.getVersion().get());

        cell = row.createCell(2);
        cell.setCellValue(file.getDateOfChange().get());

    }

    /**
     * Metoda vrací změný řádku pro daný soubor
     * @param fileIndex
     * @return počty změn řádku pro soubor, arraylist intů
     * @throws IOException
     */
    @Override
    public ArrayList<Integer> getDataForGraph(int fileIndex) throws IOException{
        ChangedFile file = this.cfl.get(fileIndex);
        Repository r = this.rr;

        List<RepositoryCommit> rc;

        ArrayList<Integer> res = new ArrayList<>();
        List<CommitFile> cf;
        rc = gcs.getAllCommits(r);
        for(RepositoryCommit rcc: rc){

            RepositoryCommit xy = gcs.getCommit(r, rcc.getSha());
            cf = xy.getFiles();
            for(CommitFile cff : cf){
                if(cff.getFilename().equals(file.getFileName().getValue())){
                    res.add(cff.getChanges());
                }
            }
        }

        return res;
    }

    /**
     * Metoda pro stažení zadaného souboru do zadané složky
     * @param Path
     * @param fileIndex
     * @throws IOException
     */
    @Override
    public void downloadFile(String Path, int fileIndex) throws IOException{
        ChangedFile file =  this.cfl.get(fileIndex);
        String[] bits = file.getFileName().getValue().split("/");
        String lastOne = bits[bits.length-1];
        String sha = file.getVersion().getValue();
        RepositoryCommit rc = gcs.getCommit(this.rr, sha);

        List<CommitFile> cf = rc.getFiles();
        String tmp = "";
        String res = "";
        for(CommitFile cff : cf){
            tmp = cff.getFilename();
            if (tmp.equals(file.getFileName().getValue())){
                res = cff.getRawUrl();
            }
        }

        gcs.downloadFile(res, lastOne, Path);
    }


}
