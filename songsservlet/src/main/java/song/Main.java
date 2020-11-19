package song;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap credentials = new HashMap<>();
        credentials.put("javax.persistence.jdbc.user", "marven");
        credentials.put("javax.persistence.jdbc.password", System.getenv("SONGS_PASSWORD"));
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("song_db", credentials);
        System.out.println(emf.getProperties().keySet());
        DBSongDAO songDAO = new DBSongDAO(emf);

        System.out.println(songDAO.findAllSongs());
    }
}
