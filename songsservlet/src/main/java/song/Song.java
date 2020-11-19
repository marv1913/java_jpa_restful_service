package song;


import javax.persistence.*;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 31-10-2020
 **/
@Entity
@Table(name="songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String artist;
    private String label;
    @Column(name="released")
    private Integer releaseYear;

    public Song(){
    }

    /**
     * Song without explicitly assigning id
     *
     * @param title       title of song
     * @param artist      artist name of song
     * @param label       song label
     * @param releaseYear song release year
     */
    public Song(String title, String artist, String label, Integer releaseYear) {
        this.title = title;
        this.artist = artist;
        this.label = label;
        if (releaseYear != null)
            this.releaseYear = releaseYear;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLabel() {
        return label;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", label='" + label + '\'' +
                ", releaseYear=" + releaseYear +
                '}' + System.lineSeparator();
    }


}
