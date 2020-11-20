
## Java JPA restful service
This is a Java Servlet application using JPA (Java Persistence API)  and Hibernate to provide a REST Interface to save "Song" objects into a Postgresql database.
The servlet provides three endpoints:

 - POST
	 - /songsservlet/songs
		 - JSON Payload: e.g. {"id": 1,"title": "Can’t Stop the Feeling","artist": "Justin Timberlake","label": "Virgin","released": 2016}
		 - this POST request saves a new song in the database
		 - in the location header of the response you can see where the song is stored (also the id of the new song)
 - GET
	 - /songsservlet/songs?songId={song_id}
		 - this endpoint return a JSON object of the requested song
	 -  /songsservlet/songs?all
		 - this endpoint returns a JSON list with all stored songs 

## Usage
 1. You can deploy the application using docker-compose:
	- clone repository
	- cd into new directory and run `docker-compose up`
	- by default the host is set to `http:/localhost:8080`
	- if you wan't to customize the IP or port of the webservice you can edit the `docker-compose.yaml` file in the root directory of the repository
2. Deploy application manually:
	- make sure you have installed `maven` and `tomcat`
	- deploy a Postgresql database and create a table named `songs`
	- in the root directory of the repository you can find a file named `create_databases.sql`
	- there you can see the syntax of the table 
	- clone repository
	- edit database configurations in `songsservlet/src/main/resources/META-INF/persistence.xml` and in the init method of `songsservlet/src/main/java/servlet/SongsServlet.java`
	- cd into `songsservlet` and run `mvn package`
	- copy the generated `songsservlet/target/songgsservlet.war`into your `ẁebapp` directory of your tomcat server
