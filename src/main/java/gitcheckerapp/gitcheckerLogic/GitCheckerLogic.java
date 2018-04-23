package gitcheckerapp.gitcheckerLogic;
import gitcheckerapp.gitcheckerService.GitcheckerService;
import org.eclipse.egit.github.core.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
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

    public ArrayList<ChangedFile> getCfl() {
        if(cfl == null){
            cfl = new ArrayList<>();
        }
        return cfl;
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
    public ArrayList<ChangedFile> getChangedFilesList(String owner, String repoName) throws IOException, ClassNotFoundException {

        ArrayList<ChangedFile> changedFilesList;
        RepositoryCommit rCommit;
        boolean choice = loadBackup();
        if(choice == true) { //pokud existuje soubor a neco v nem je
            if(internetIsConnected() == true){  //pokud je připojen k netu
                Repository r;
                r = getUserRepositoryObject(owner, repoName);
                this.rr = r;
                String fileVersion;
                fileVersion = cfl.get(0).getVersion().getValue();
                System.out.println(fileVersion);
                rCommit = gcs.getLastRepositoryCommit(r);
                if(rCommit.getSha().equals(fileVersion)){
                    //zde se vraci rovnou list z loadu protoze je v souboru aktualni verze commitu
                    System.out.println("nahravam ze souboru verze se shoduji");
                    return cfl;
                } else {
                    //v tehle vetvi neni potreba zase stahovat všechny soubory, jen dostahovat zbytek
                    //ale to neumim, takže to zas stahuju celé
                    changedFilesList = fillListChangedFileFromNet(r);
                    this.cfl = changedFilesList;
                    saveBackup(changedFilesList); //vytvori se zaloha protoze doslo k update
                }
            } else{
                //pokud není připojen z netu vracim list z loadu
                return cfl;
            }

        } else {
            //zde je potreba vse stahnout z netu protože bud soubor neexistuje nebo v nem nic neni
            Repository r;
            r = getUserRepositoryObject(owner, repoName);
            this.rr = r;

            changedFilesList = fillListChangedFileFromNet(r);
            this.cfl = changedFilesList;
            saveBackup(changedFilesList); //vytvori se zaloha protoze doslo k update
        }

        return changedFilesList;

    }

    /**
     * Metoda pro nahrani vsech souboru z netu do ArrayListu changedFiles
     * @param r Repositar pro praci
     * @return ArrayList changedFile
     * @throws IOException
     */
    public ArrayList<ChangedFile> fillListChangedFileFromNet(Repository r) throws IOException{
        ChangedFile changedFile;
        ArrayList<ChangedFile> changedFileList = new ArrayList<>();
        RepositoryCommit rCommit;
        List<CommitFile> commitFileList = new ArrayList<>();
        List<RepositoryCommit> rCommitList;

        rCommitList = gcs.getAllCommits(r);
        for (RepositoryCommit rc : rCommitList) {
            RepositoryCommit xy = gcs.getCommit(r, rc.getSha());
            commitFileList = xy.getFiles();
            for (CommitFile cf : commitFileList) {
                changedFile = new ChangedFile();
                changedFile.setFileName(cf.getFilename());
                changedFile.setChangedLines(cf.getChanges());
                changedFile.setDateOfChange(xy.getCommit().getAuthor().getDate());
                changedFile.setVersion(rc.getSha());
                changedFile.setURL(getRepositoryPath());
                String[] bits = cf.getFilename().split("/");
                String first = "";
                if (bits.length > 2) {
                    first = bits[bits.length - 2];
                }
                String last = bits[bits.length - 1];
                changedFile.setFileNameShortenProperty(first + "/" + last);

                if (cf.getFilename().contains(".java")) {
                    changedFile.setIsJavaFile(true);
                } else {
                    changedFile.setIsJavaFile(false);
                }

                changedFileList.add(changedFile);
            }
        }
        return changedFileList;
    }
    public String getURLFromFile(){
        String url = "";
        ArrayList<ChangedFile> changedFileList = new ArrayList<>();
        changedFileList = cfl;
        for(ChangedFile cf : changedFileList){
            url = cf.getURL();
        }
        System.out.println("toto: " + url);
        return url;
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
        StringChangedFile scf;
        ArrayList<StringChangedFile> ascf = new ArrayList<StringChangedFile>();
        int size = acf.size();
        //oos.writeInt(size);
        for(ChangedFile cf : acf){
            scf = new StringChangedFile(cf.getFileName().getValue(), cf.getDateOfChange().getValue(),
                    cf.getChangedLines().getValue(), cf.getVersion().getValue(),
                    cf.getIsJavaFile().getValue(), cf.getURL(), cf.getFileNameShortenProperty().getValue());
            ascf.add(scf);

        }
        oos.writeObject(ascf);
        oos.close();
        fos.close();
    }

    /**
     * Metoda pro nahrání dat ze zálohy, rovnou v teto metode nastavuji prom.
     * Array list changedFile na ten list co vytáhnu
     * @return Vracim true, pokud soubor existuje a neco tam je
     * @return false pokud bud neexistuje soubor nebo tam nic neni
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean loadBackup() throws IOException, ClassNotFoundException {
        ArrayList<ChangedFile> load = new ArrayList<ChangedFile>();
        File f = new File("backup.ser");
        boolean res = false;

        if(f.exists()){

            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            StringChangedFile scf;
            ChangedFile cf;
            ArrayList<StringChangedFile> ascf = ((ArrayList<StringChangedFile>) ois.readObject());

            String URLFromFile = "";
            //int size = ois.readInt();
            for(StringChangedFile scff : ascf) {
                cf = new ChangedFile(scff.dateOfChange, scff.fileName, scff.changedLines, scff.version,
                        scff.isJavaFile, scff.URL, scff.fileNameShorten);

                URLFromFile = cf.getURL();

                load.add(cf);
            }
            ois.close();
            fis.close();
            if (load.size() > 1){
                this.cfl =  load;
                setRepositoryPath(URLFromFile);
                res = true;
            } else {
                res = false;
            }

        }
        return res;
    }

    @Override
    public void setRepositoryPath(String URL) {
           this.repUrl = URL;
    }


    @Override
    public String getRepositoryPath() {
        return this.repUrl;
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

        FileOutputStream out = new FileOutputStream(new File(path + "/excel.xlsx")); // file name with path
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
        Collections.reverse(rc);
        boolean b = true;
        int tmp = 0;
        for(RepositoryCommit rcc: rc){
            RepositoryCommit xy = gcs.getCommit(r, rcc.getSha());
            cf = xy.getFiles();

            for(CommitFile cff : cf){
                if(cff.getFilename().equals(file.getFileName().getValue())){
                    if (b){
                        b = false;
                        tmp = cff.getAdditions();
                        res.add(cff.getAdditions());
                    } else {
                        tmp = tmp + cff.getAdditions() - cff.getDeletions();
                        res.add(tmp);
                    }

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

    @Override
    public int getAllFilesLineNo() {
        return 0;
        }



}
