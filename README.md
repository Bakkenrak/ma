# D2S Web Directory
discover2share, discover2share-EJB, discover2share-Persistence and discover2share-Web are the projects representing the Java EE application.

discover2share-scripts contains additional scripts, e.g. the Excel to RDF transformation and an Alexa user distribution parser.

## Setup
### Setting up the web directory under Windows
*
* As a preliminary, a current version of Java needs to be installed. The development was undertaken with [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
* [Download WildFly](http://wildfly.org/downloads/) (tested with version 8.2.0 Final) and extract the archive’s contents to a path which must not contain spaces (e.g. <tt>C:\wildfly</tt>)
* Run the server using the <tt><WildFly directory>\bin\standalone.bat</tt> script. It should then be accessible via http://localhost:8080 . 
  * Click the “Administration Console” link and follow the instructions to create a management user. 
  * Afterwards, revisit the Administration Console and go to “Deployments”. Add the <tt>hsqldb.jar</tt> which can be found in the repository’s lib directory via “Add Content”. Copying it to <tt><WildFly directory>\standalone\deployments</tt> beforehand might be beneficial to prevent certain errors in the future.
  * Go to “Configuration” -> “Connector” -> “Datasources” -> “Add” to create a new data source with the following information. Enable it afterwards.
    * Name: D2SDS
    * JNDI Name: java:/D2SDS
    * Choose hsqldb.jar as the driver
    * Connection URL: jdbc:hsqldb:${jboss.server.data.dir}${/}hypersonic${/}localDB;shutdown=true
    * Username: sa
  * Optional: Add the <tt>discover2share.ear</tt> from the bin directory as another deployment if you want to run the web directory in standalone mode and not via eclipse. The application is then available at http://localhost:8080/discover2share-Web/
