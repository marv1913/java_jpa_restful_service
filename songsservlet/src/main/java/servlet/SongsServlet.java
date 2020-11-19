package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.servlet.ServletConfig;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import song.DBSongDAO;
import song.Song;

public class SongsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private DBSongDAO songDAO;

    private String SONG_PATH = "/songsservlet-MarvEn/songs?songId=%s";

    @Override
    public void init(ServletConfig servletConfig) {
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("javax.persistence.jdbc.user", "postgres");
        credentials.put("javax.persistence.jdbc.password", System.getenv("SONGS_PASSWORD"));
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("song_db", credentials);
        this.songDAO = new DBSongDAO(emf);
    }

    /**
     * POST
     * URI: ../songsservlet-MarvEn/songs
     * Body: Json {"title": "", "artist": "", "label": "", "released": }
     *
     * @param request  Request
     * @param response Response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        if (request.getHeader("accept") != null && !request.getHeader("accept").equals("*/*")) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            // ToDo send acceptable formats to client
            return;
        }
        // ToDo send content type of response to client
        try {
            this.checkForGetPath(request);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        ServletInputStream inputStream = request.getInputStream();
        byte[] inBytes = IOUtils.toByteArray(inputStream);
        String header = new String(inBytes);

        if (header.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (request.getHeader("content-type") == null || !(request.getHeader("content-type").equals("application/json"))) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            this.validateJSON(header);
        } catch (NullPointerException | IllegalArgumentException e) {
            sendAnswer(e.getMessage(), response.getWriter());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (ClassCastException e) {
            sendAnswer("A Value has bad format", response.getWriter());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jsonObject = (JSONObject) parser.parse(header);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            Song song;
            assert jsonObject != null;
            if (jsonObject.get(DBSongDAO.RELEASED_COLUMN) != null) {
                song = new Song((String) jsonObject.get(DBSongDAO.TITLE_COLUMN), (String) jsonObject.get(DBSongDAO.ARTIST_COLUMN),
                        (String) jsonObject.get(DBSongDAO.LABEL_COLUMN), Math.toIntExact((Long) jsonObject.get(DBSongDAO.RELEASED_COLUMN)));
            } else {
                song = new Song((String) jsonObject.get(DBSongDAO.TITLE_COLUMN), (String) jsonObject.get(DBSongDAO.ARTIST_COLUMN),
                        (String) jsonObject.get(DBSongDAO.LABEL_COLUMN), null);
            }

            int songId = this.songDAO.saveSong(song);
            response.setStatus(HttpServletResponse.SC_CREATED);

            response.setContentType("application/json");
            response.addHeader("Location", String.format(this.SONG_PATH, songId));


        } catch (ClassCastException e) {
            sendAnswer("a key has a wrong data type", response.getWriter());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }

    /**
     * Check if get path is correct
     *
     * @param request Request
     * @throws IllegalArgumentException
     */
    private void checkForGetPath(HttpServletRequest request) {
        if (null != request.getQueryString() && (request.getQueryString().equals("all") || request.getQueryString().contains("songId="))) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * GET 1: ../songsservlet-MarvEn/songs?songId=SONGID
     * Get a song from songID in JSON
     * Get 2: ..../songsservlet-MarvEn/songs?all
     * Get all songs in JSON
     *
     * @param request  Request
     * @param response Response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8"); // ToDo ask for setCharacterEncoding
        String clientAcceptParameter = request.getHeader("Accept");
        if (!clientAcceptParameter.equals("*/*") && !clientAcceptParameter.equals("application/json")) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }

        HashMap<String, List<String>> keyValueOfRequest = this.getAllParametersFromURI(request);

        if (keyValueOfRequest.keySet().isEmpty()) {
            // there are no params => base url => only for post request
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        boolean isSongId = true;
        if (null == keyValueOfRequest.get("all") && null == keyValueOfRequest.get("songId")) {
            // if there are parameter but no "all" or "songId" the url is wrong
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (null != keyValueOfRequest.get("all")) {
            // all is the parameter
            isSongId = false;
        }
        // if "all" is null songId is the parameter (isSongId is already initialized with true)

        try {
            if (isSongId) {
                checkResponseAndParameter("songId", request, response, false);
            } else {
                checkResponseAndParameter("all", request, response, true);
                if (!request.getQueryString().equals("all")) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            return;
        }

        if (isSongId) {
            List<String> values = keyValueOfRequest.get("songId");
            try {
                int songId = Integer.parseInt(values.get(0));
                Song song = this.songDAO.findSongById(songId);
                PrintWriter out = response.getWriter();
                response.setContentType("application/json");
                out.print(getSongAsJSONObject(song).toJSONString());
                out.flush();

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (IllegalStateException | NoResultException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else {

            JSONArray allSongs = new JSONArray();
            for (Song song : this.songDAO.findAllSongs()) {
                allSongs.add(getSongAsJSONObject(song));
            }
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            out.print(allSongs.toJSONString());
            response.setStatus(HttpServletResponse.SC_OK);
            out.flush();
            return;

        }
    }

    /**
     * Checks Response and Parameters
     *
     * @param parameterName parameter
     * @param request       Request
     * @param response      Response
     * @param shouldBeBlank should parameter be blank?
     * @return
     * @throws IOException
     */
    private boolean checkResponseAndParameter(String parameterName, HttpServletRequest request, HttpServletResponse response, boolean shouldBeBlank) throws IOException {
        HashMap<String, List<String>> keyValueOfRequest = this.getAllParametersFromURI(request);
        List<String> valuesOfParameter = keyValueOfRequest.get(parameterName);

        if (keyValueOfRequest.keySet().size() != 1) {
            // if there are more than one parameter in uri, uri is wrong
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new IllegalArgumentException();
        }
        if (valuesOfParameter.isEmpty() || shouldBeBlank != valuesOfParameter.get(0).isBlank() || valuesOfParameter.size() != 1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            sendAnswer(String.valueOf(valuesOfParameter.size()), response.getWriter());

            throw new IllegalArgumentException();

        }
        return true;
    }

    /**
     * Gets all parameters from an URI
     *
     * @param request
     * @return
     */
    private HashMap<String, List<String>> getAllParametersFromURI(HttpServletRequest request) {
        HashMap<String, List<String>> parameterKeysAndValues = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] parameterValuesTemp = request.getParameterValues(paramName);
            if (parameterValuesTemp != null) {
                parameterKeysAndValues.put(paramName, Arrays.asList(request.getParameterValues(paramName)));
            } else {
                parameterKeysAndValues.put(paramName, new ArrayList<>());
            }
        }
        return parameterKeysAndValues;
    }

    /**
     * Send answer message
     *
     * @param message Message to be send
     * @param writer  Write to be used to write message
     */
    private void sendAnswer(String message, PrintWriter writer) {
        writer.print(message);
    }

    /**
     * Validates JSON format
     *
     * @param json
     */
    private void validateJSON(String json) throws ClassCastException {
        // Should be duplicate keys be allowed ? -> already deleted when parsed
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (jsonObject == null || jsonObject.isEmpty()) {
            throw new NullPointerException("JSON has bad format");
        }

        if (!jsonObject.containsKey(DBSongDAO.TITLE_COLUMN)) {
            throw new IllegalArgumentException("Title has to be set");
        }

        // Should be safe Json only accepts String as Key -> already at parse exception thrown
        Set<String> keySet = jsonObject.keySet();
        // Goes through all the given keys and compares them with expected keys, if key not equal to any of the expected keys Json is invalid
        for (String key : keySet) {
            boolean containsKey = false;
            for (String expectedKey : DBSongDAO.allKeys) {
                if (key.contentEquals(expectedKey)) {
                    containsKey = true;
                    break;
                }
            }
            if (!containsKey) {
                if (key.isEmpty())
                    throw new IllegalArgumentException("Empty key not valid");
                else
                    throw new IllegalArgumentException(key + " is not a valid key");
            }
            //Checks if value type is correct (should really per convention be saved as type in JSON but we can't don't it)
            System.out.println(key);
            if (!key.contentEquals(DBSongDAO.RELEASED_COLUMN)) {
                String string = (String) jsonObject.get(key);
            } else {
                Long release = (Long) jsonObject.get(key);
            }

        }
    }

    /**
     * Convert Song to JSON Array
     *
     * @param song Song to be converted
     * @return JSON Object
     */
    static JSONObject getSongAsJSONObject(Song song) {
        JSONObject obj = new JSONObject();
        obj.put(DBSongDAO.ID_COLUMN, song.getId());
        obj.put(DBSongDAO.TITLE_COLUMN, song.getTitle());
        obj.put(DBSongDAO.ARTIST_COLUMN, song.getArtist());
        obj.put(DBSongDAO.LABEL_COLUMN, song.getLabel());
        obj.put(DBSongDAO.RELEASED_COLUMN, song.getReleaseYear());
        return obj;
    }
}
