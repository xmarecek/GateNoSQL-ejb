Author:   Jakub Marecek
Project:  NoSQL Database for the Storage of OWL Data
Purpose:  Describe essential information about the project




## Project

The implemented EJB component provides services for managing and persisting OWL and RDF data.
It can be used as a universal OWL and RDF store. It provides persistence, indexing mechanism
and another advantage of the solution is the ability to logically structure ontologies by subgraphs.
Therefore it is possible to query just desired subset of data instead of all stored data. Naturally,
we can united these ontologies on demand and make queries on them.

The component consists of a class named GateNosqlBean which is Stateless type. The class GateNosqlBean
implements interface GateNosqlLocal and GateNosqlRemote.



## Testing

*It is necessary to install AllegroGraph database (please, see section AllegroGraph 4 Installation).
*After installation, it is necessary to set properties defining connection to AllegroGraph Server in
 configuration/database.properties file (please, see section AllegroGraph 4 Configuration).
*Run the tests by:
 
 mvn clean test



## AllegroGraph 4 Installation

Installation from the RPM (Red Hat/Fedora systems)
1. Obtain agraph-4.12.2-1.x86 64.rpm (or newer) from http://www.franz.com/downloads.lhtml
2. Install the RPM:
# rpm -i agraph-4.12.2-1.x86 64.rpm
3. Run the configuration script:
# /usr/bin/configure-agraph
4. The script asks several questions about configuring AllegroGraph. Please see
http://www.franz.com/agraph/allegrograph/doc/server-installation.html#configscript
for more information. The configuration script asks several questions. The
default answers are usually adequate and can be reconfigured later if necessary.
Note that the last few lines of the script show how to start and stop AllegroGraph. These
lines will be similar to this example:
You can start AllegroGraph by running: /sbin/service agraph start
You can stop AllegroGraph by running: /sbin/service agraph stop
5. To verify that AllegroGraph is running correctly, it is possible to access Web View.
http://localhost:10035

Installation from the TAR.GZ (all systems)
1. Obtain agraph-4.12.2-1.x86 64.rpm (or newer) from http://www.franz.com/downloads.lhtml
2. Extract the installation files:
$ tar zxf agraph-4.12.2-linuxamd64.64.tar.gz
3. This creates the ”agraph-4.12.2” subdirectory, where the installation script is inside. Then,
provide the path to a writable directory where you want to install AllegroGraph.
$ agraph-4.12.2/install-agraph /home/user/bin/agraph4.12.2
Installation complete.
Now running configure-agraph.
4. The script asks several questions about configuring AllegroGraph. Please see
http://www.franz.com/agraph/allegrograph/doc/server-installation.html#configscript
for more information. The configuration script asks several questions. The
default answers are usually adequate and can be reconfigured later if necessary.
Note that the last few lines of the script show how to start and stop AllegroGraph. These
lines will be similar to this example:
You can start AllegroGraph by running: /sbin/service agraph start
You can stop AllegroGraph by running: /sbin/service agraph stop
5. To verify that AllegroGraph is running correctly, it is possible to access Web View.
http://localhost:10035



## AllegroGraph 4 Configuration

The attributes data are stored in configuration/database.properties file. Defaul values are:

### Properties defining connection to AllegroGraph Server

server_url = http://localhost:10035
username = root
password = root
catalog_id = java-catalog
