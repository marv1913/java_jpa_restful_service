package song;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.RollbackException;


public class DBSongDAO {

    public static String ID_COLUMN = "id";
    public static String TITLE_COLUMN = "title";
    public static String ARTIST_COLUMN = "artist";
    public static String LABEL_COLUMN = "label";
    public static String RELEASED_COLUMN = "released";

    public static List<String> allKeys = new ArrayList<>(Arrays.asList(TITLE_COLUMN, ARTIST_COLUMN, LABEL_COLUMN, RELEASED_COLUMN));

    private EntityManagerFactory emf;

    public DBSongDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Save a song to db
     *
     * @param song Song object to be saved
     * @return id assigned to song
     * @throws PersistenceException
     */
    public Integer saveSong(Song song) throws PersistenceException {
        // EntityManager provides access to database
        EntityManager em = null;
        EntityTransaction transaction = null;
        try {
            em = emf.createEntityManager();
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(song);
            transaction.commit();
            return song.getId();
        } catch (IllegalStateException | EntityExistsException | RollbackException ex) {
            if (em != null) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException(ex.getMessage());
        } finally {
            // EntityManager is closed after each database interaction
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Alle Song aus der DB lesen mit JPQL
     */
    @SuppressWarnings("unchecked")
    public List<Song> findAllSongs() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            Query q = em.createQuery("SELECT s FROM Song s");
            List<Song> songList = q.getResultList();
            return songList;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Alle Song aus der DB lesen mit JPQL
     *
     * @param songId Song id
     * @return Song
     */
    @SuppressWarnings("unchecked")
    public Song findSongById(int songId) {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            Query q = em.createQuery("SELECT s FROM Song s WHERE id=" + songId);
            return (Song) q.getSingleResult();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Delete Song from db
     *
     * @param id id of song
     * @throws PersistenceException
     */
    public void deleteSong(Integer id) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        Song song = null;
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            // find User with id
            song = em.find(Song.class, id);
            if (song != null) {
                em.remove(song);
                transaction.commit();
            }
        } catch (Exception e) {
            System.out.println("Error removing user: " + e.getMessage());
            throw new PersistenceException("Could not remove entity: " + e.toString());
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
