package gitcheckerapp.gitcheckerService;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

public class GitcheckerService {

    private GitHubClient client;

    public GitcheckerService(String username, String pass) {
        this.client = new GitHubClient();
        client.setCredentials(username, pass);
    }

    public GitcheckerService() {
        this.client = new GitHubClient();
    }

    /**
     * Metoda vrací všechny repozitáře, které má přihlášený uživatel.
     * @return Vrací list Repository
     * @throws IOException
     */
    public List<Repository> getAllLoggedUserRepository() throws IOException{
        RepositoryService service = new RepositoryService(client);
        return service.getRepositories();
    }

    /**
     * Metoda vrací repozitáře daného uživatele
     *
     * @param username Uzivatel v GitHubu
     * @return Vrací list Repository
     * @throws IOException
     */
    public List<Repository> getAllUserRepository(String username) throws IOException{
        RepositoryService service = new RepositoryService(client);
        return service.getRepositories(username);
    }

    /**
     * Metoda pro vypsáníobsahu repozitáře.
     * @param repo Repozitář, který prohledáváme
     * @param path pokud NULL, vrací jen top level složek a souborů. Pokud je tam cesta,vrací soubory a složky v dané cestě
     * @return Vrací list RepositoryContents
     * @throws IOException
     */
    public List<RepositoryContents> getFilesInRepository(Repository repo, String path) throws IOException{
        ContentsService content = new ContentsService(client);
        if(path == null){
            return content.getContents(repo);
        } else {
            return content.getContents(repo, path);
        }
    }

    /**
     * Metoda vraci repozitář, podle jména a jeho vlastníka
     * @param owner Jméno vlastníka repositáře
     * @param name Název repositáře
     * @return Vrací objekt typu Repository
     * @throws IOException
     */
    public Repository getRepository(String owner, String name) throws IOException{
        RepositoryService service = new RepositoryService(client);
        return service.getRepository(owner, name);
    }

    /**
     * Metoda vrací poslední commit udělaný v repositáři
     * @param repo Repositář
     * @return Vrací objekt typu RepositoryCommit :)
     * @throws IOException
     */
    public RepositoryCommit getLastRepositoryCommit(Repository repo) throws IOException {
        CommitService service = new CommitService(client);
        PageIterator<RepositoryCommit> iter = service.pageCommits(repo, 1);
        RepositoryCommit res = service.getCommit(repo, iter.next().iterator().next().getSha());
        return res;
    }
    /**
     +     * Metoda pro vrácení všech commitu (není list souborů)
     +     * @param repo Repositář
     +     * @return Vrací list RepositoryCommit (ale bez souborů)
     +     * @throws IOException
     +     */
    public List<RepositoryCommit> getAllCommits(Repository repo) throws IOException{
        CommitService service = new CommitService(client);
        return service.getCommits(repo);
        }
    /**
     * Metoda pro ukládání souboru z internetu. URL pro stazeni je v CommitFile pomoci metody getRawURL()
     * Příklad použití:
     *      GitcheckerService gs = new GitcheckerService();
     *      gs.downloadFile("https://blabla.cz/soubor.bin","soubor.bin","C://stazeny");
     * @param url URL k souboru ke stazeni
     * @param filename název souboru i s koncovkou
     * @param path cesta,kam uložit soubor
     * @throws IOException
     */
    public void downloadFile(String url, String filename, String path) throws IOException{
        try{
            URL web = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(web.openStream());
            FileOutputStream fos = new FileOutputStream(new File(path+"/"+filename));
            fos.getChannel().transferFrom(rbc,0,Long.MAX_VALUE);
            rbc.close();
            fos.close();
        } catch(MalformedURLException e){
            throw new IOException("Chyba pri pripojeni k url", e);
        } catch(IOException e){
            throw new IOException("Chyba pri cteni z url nebo pri ukladani souboru", e);
        }
    }
    /**
     * Vrací commit repozitáře podle SHA commitu (součástí commitu je i list změněných souborech)
     * @param repo Repositář
     * @param commitSHA SHA commitu
     * @return Vrací objekt typu RepositoryCommit :)
     * @throws IOException
     */
    public RepositoryCommit getCommit(Repository repo, String commitSHA) throws IOException{
        CommitService service = new CommitService(client);
        return service.getCommit(repo, commitSHA);
    }
    /**
     * Metoda pro vracení všech commitu s určitým souborem
     * @param repo Repositář
     * @param path Cesta i s nazvem souboru na Githubu
     * @return List RepositoryCommit (bez souborů)
     * @throws IOException
     */
    public List<RepositoryCommit> getAllCommitsWithExactFile(Repository repo, String path) throws IOException{
        CommitService service = new CommitService(client);
        return service.getCommits(repo, null, path);
    }
}
