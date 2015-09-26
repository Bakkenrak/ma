# D2S Web Directory
discover2share, discover2share-EJB, discover2share-Persistence and discover2share-Web are the projects representing the Java EE application.
The initial D2S web directory user that is created at database setup has the username "sa" and the password "admin".

discover2share-scripts contains additional scripts, e.g. the Excel to RDF transformation and an Alexa user distribution parser.

## Setup
#### Setting up the web directory under Windows
* As a preliminary, a current version of Java needs to be installed. The development was undertaken with [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
* [Download WildFly](http://wildfly.org/downloads/) (tested with version 8.2.0 Final) and extract the archive’s contents to a path which must not contain spaces (e.g. <tt>C:\wildfly</tt>)
* Run the server using the <tt>`<WildFly directory>`\bin\standalone.bat</tt> script. It should then be accessible via [http://localhost:8080](http://localhost:8080). 
  * Click the “Administration Console” link and follow the instructions to create a management user. 
  * Afterwards, revisit the Administration Console and go to “Deployments”. Add the <tt>hsqldb.jar</tt> which can be found in the repository’s lib directory via “Add Content”. Copying it to <tt>`<WildFly directory>`\standalone\deployments</tt> beforehand might be beneficial to prevent certain errors in the future.
  * Go to “Configuration” > “Connector” > “Datasources” > “Add” to create a new data source with the following information. Enable it afterwards.
    * Name: DefaultDS
    * JNDI Name: java:/DefaultDS
    * Choose hsqldb.jar as the driver
    * Connection URL: jdbc:hsqldb:${jboss.server.data.dir}${/}hypersonic${/}localDB;shutdown=true
    * Username: sa
  * Optional: Add the <tt>discover2share.ear</tt> from the bin directory as another deployment if you want to run the web directory in standalone mode and not via eclipse. The application is then available at [http://localhost:8080/discover2share-Web/](http://localhost:8080/discover2share-Web/)

#### Setting up the triplestore server under Windows
* [Download Apache Jena Fuseki](http://jena.apache.org/download/index.cgi#apache-jena-fuseki) (tested with [version 2.0.0](http://archive.apache.org/dist/jena/binaries/)) and extract the archive’s content to a folder on your hard drive.
* Run the server using the <tt>`<Fuseki directory>`\fuseki-server.bat</tt>. It should then be accessible via [http://localhost:3030/](http://localhost:3030/) .
  * Go to “manage datasets” > “add new dataset”, name it <tt>d2s-ont</tt> and choose the type „Persistent“.
  * Afterwards, go to “existing datasets” and select the “upload data” button for the new dataset. Using the “select files…” button select the required .owl files from the <tt>ontology</tt> directory in this repository. Then click “upload all”.
  * The Fuseki server needs to be running in the future in order to have the web directory function properly.

#### Optional: Setting up the web directory in eclipse
* [Download Eclipse](http://www.eclipse.org/downloads/) (tested with Eclipse Java EE IDE for Web Developers, [Luna Service Release 2 (4.4.2)](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/lunasr2)) and extract it to a folder on your hard drive and run it.
  * Select “Help” > “Eclipse Marketplace…” and choose “Eclipse Marketplace” as the marketplace catalogue. Search for “JBoss Tools” and install “JBoss Tools 4.2.x". After all requirements have been checked, select all features and confirm. Accept the license agreements and click “Finish”. Warnings regarding unsigned content can be closed via “OK”. Afterwards, confirm the restart of eclipse.
  * Choose the “Java EE” perspective from the upper right corner of eclipse. If it’s not visible, add it via “Window” > “Open Perspective” > “Other…”.
  * Choose the “Servers” view tab in the lower part of the eclipse window. Right-click the empty area and select “New” > “Server”. Select “WildFly 8.x” from the “JBoss Community” category and click “Next” twice. Set the “Home directory” to the installation directory of WildFly (e.g. <tt>C:\wildfly</tt>) and click “Finish”.
  * If the <tt>discover2share.ear</tt> was added as explained above. The server can now be run by selecting it and clicking the green start button.
* Import all projects from the repository via “File” > “Import” > “Existing Projects into Workspace”. They should then appear in the projects explorer. It might be necessary to exchange the JRE System Library in the build paths of the projects to match the installed Java version.
* Right-click the previously created WildFly server entry and select “Add and Remove…”. Select the “discover2share” project  and click “Add >”, then “Finish”.
* The project will now be automatically deployed when starting the server in eclipse. It is then available at [http://localhost:8080/discover2share-Web/](http://localhost:8080/discover2share-Web/).

#### Running the Alexa Parser script
This script retrieves all platforms from the ontology and tries to find user distribution data for them on Alexa.com. If found, it is added to the ontology or written to a file.
* From within eclipse the script can be run by executing the <tt>de.wwu.d2s.transformation.AlexaParser.java</tt> file in the discover2share-scripts project.
* Alternatively, the script can be run using the <tt>bin/AlexaParser.jar</tt> using the command <tt>java -jar AlexaParser.jar</tt>.
  * By default the following variables are used:
    * Query endpoint to retrieve all platforms: <tt>http://localhost:3030/d2s-ont/query</tt>
    * Update endpoint to add Alexa data: <tt>http://localhost:3030/d2s-ont/update</tt>
    * JSON file containing all countries: <tt>http://localhost:8080/discover2share-Web/resources/js/countries.json</tt>
  * To use different values, command line variables may be used in the same order as above, i.e. <tt>java -jar AlexaParser.com http://localhost:3030/d2s-ont/query http://localhost:3030/d2s-ont/update http://localhost:8080/discover2share-Web/resources/js/countries.json</tt>
  * Additionally, it can be defined that the output should not be written to the update endpoint but into a text file. To achieve this, the parameter <tt>fileOutput</tt> and the desired output file must be attached to any of the end of the command. E.g.: <tt>java -jar AlexaParser.jar fileOutput C:\AlexaInfo.owl</tt>

#### Running the Excel Table Transformation script
This script converts the P2P SCC descriptions in the provided Excel table to RDF data according to the D2S ontology's structure.
* From within eclipse the script can be run by executing the <tt>de.wwu.d2s.transformation.Start.java</tt> file in the discover2share-scripts project.
* Alternatively, the script can be run using the <tt>bin/ExcelTransformation.jar</tt> using the command <tt>java -jar ExcelTransformation.jar</tt>. It then prompts for the full paths and filenames of the Excel table and the desired output file. Optionally, the path to <tt>countries.json</tt> on the D2S server, which is required, can be provided as a command line variable. E.g. <tt>java -jar ExcelTransformation.jar http://www.example.org/countries.json</tt>. By default <tt>http://localhost:8080/discover2share-Web/resources/js/countries.json</tt> is assumed.
