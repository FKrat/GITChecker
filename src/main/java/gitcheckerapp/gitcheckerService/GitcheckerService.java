package gitcheckerapp.gitcheckerService;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
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
}
