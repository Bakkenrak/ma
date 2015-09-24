# D2S Web Directory
discover2share, discover2share-EJB, discover2share-Persistence and discover2share-Web are the projects representing the Java EE application.

discover2share-scripts contains additional scripts, e.g. the Excel to RDF transformation and an Alexa user distribution parser.

## Setup
#### Setting up the web directory under Windows
* As a preliminary, a current version of Java needs to be installed. The development was undertaken with [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
* [Download WildFly](http://wildfly.org/downloads/) (tested with version 8.2.0 Final) and extract the archive’s contents to a path which must not contain spaces (e.g. <tt>C:\wildfly</tt>)
* Run the server using the <tt>`<WildFly directory>`\bin\standalone.bat</tt> script. It should then be accessible via [http://localhost:8080](http://localhost:8080). 
  * Click the “Administration Console” link and follow the instructions to create a management user. 
  * Afterwards, revisit the Administration Console and go to “Deployments”. Add the <tt>hsqldb.jar</tt> which can be found in the repository’s lib directory via “Add Content”. Copying it to <tt>`<WildFly directory>`\standalone\deployments</tt> beforehand might be beneficial to prevent certain errors in the future.
  * Go to “Configuration” > “Connector” > “Datasources” > “Add” to create a new data source with the following information. Enable it afterwards.
    * Name: D2SDS
    * JNDI Name: java:/D2SDS
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

#### Setting up the web directory in eclipse
* [Download Eclipse](http://www.eclipse.org/downloads/) (tested with Eclipse Java EE IDE for Web Developers, [Luna Service Release 2 (4.4.2)](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/lunasr2)) and extract it to a folder on your hard drive and run it.
  * Select “Help” > “Install new Software…” and select Luna from the dropdown menu as the site to work with. After the list of software is loaded, select “Eclipse Java EE Developer Tools” and “Eclipse Java Web Developer Tools”. Click “Next” twice, “Accept” and “Finish”. When prompted to restart eclipse, confirm.
  * Select “Help” > “Eclipse Marketplace…” and choose “Eclipse Marketplace” as the marketplace catalogue. Search for “JBoss Tools” and install “JBoss Tools (Luna)”, version 4.2.x. After all requirements have been checked, select all features and confirm. Accept the license agreements and click “Finish”. Warnings regarding unsigned content can be closed via “OK”. Afterwards, confirm the restart of eclipse.
  * Choose the “Java EE” perspective from the upper right corner of eclipse. If it’s not visible, add it via “Window” > “Open Perspective” > “Other…”.
  * Choose the “Servers” view tab in the lower part of the eclipse window. Right-click the empty area and select “New” > “Server”. Select “WildFly 8” from the “JBoss Community” category and click “Next”. Set the “Home directory” to the installation directory of WildFly (e.g. <tt>C:\wildfly</tt>) and click “Finish”.
  * If the <tt>discover2share.ear</tt> was added as explained above. The server can now be run by selecting it and clicking the green start button.
* Import all projects from the repository via “File” > “Import” > “Existing Projects into Workspace”. They should then appear in the projects explorer.
* Right-click the previously created WildFly server entry and select “Add and Remove…”. Select the “discover2share” project  and click “Add >”, then “Finish”.
* The project will now be automatically deployed when starting the server in eclipse. It is then available at [http://localhost:8080/discover2share-Web/](http://localhost:8080/discover2share-Web/).
