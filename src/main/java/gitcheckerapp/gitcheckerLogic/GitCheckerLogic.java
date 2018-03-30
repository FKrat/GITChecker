package gitcheckerapp.gitcheckerLogic;
import gitcheckerapp.gitcheckerService.GitcheckerService;
import org.eclipse.egit.github.core.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import gitcheckerapp.gitcheckerInterface.IGitcheckerLogic;


/**
 * Created by Lukáš on 27.03.2018.
 */

public class GitCheckerLogic implements IGitcheckerLogic{
    private String username;
    private String pass;


    public GitCheckerLogic(){}

    public GitCheckerLogic(String username, String pass){
        this.username = username;
        this.pass = pass;
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
        List<String> LfileNames = new ArrayList<String>();
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
        List<String> LfileNames = new ArrayList<String>();
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
        List<String> LfileNames = new ArrayList<String>();
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
    public ArrayList<ChangedFile> getChangedFilesList(String owner, String repoName) throws IOException{
        ChangedFile fdto;
        ArrayList<ChangedFile> Lfile = new ArrayList<ChangedFile>();
        RepositoryCommit rc;
        List<CommitFile> cf;

        Repository r;
        r = getUserRepositoryObject(owner, repoName);

        //String repoNamee = r.getName();
        rc = gcs.getLastRepositoryCommit(r);
        cf = rc.getFiles();

        for(CommitFile cff : cf){
            fdto = new ChangedFile();
            fdto.setFileName(cff.getFilename());
            fdto.setChangedLines(cff.getChanges());
            fdto.setDateOfChange(rc.getCommit().getAuthor().getDate());

            Lfile.add(fdto);
        }

        return Lfile;

    }

    public boolean internetIsConnected() {
        return false;
    }

    public void setRepositoryPath(String URL) {

    }

    public void setDirectoryPath(String path) {

    }

    public String getDirectoryPath() {
        return null;
    }

    public String getRepositoryPath() {
        return null;
    }

    public LocalTime nextDownloadTime() {
        return null;
    }
}
