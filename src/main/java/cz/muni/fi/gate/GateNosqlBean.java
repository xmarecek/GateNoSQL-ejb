/*
* Project :       Master's Thesis - NoSQL Database for Storing OWL Data
* Document :      GateNosqlBean.java
* Author :        Bc. Jakub Mareček  <xmarecek@mail.muni.cz>
* Organization: : FI MUNI <http://www.fi.muni.cz>
*/


package cz.muni.fi.gate;

import com.franz.agraph.http.exception.AGHttpException;
import com.franz.agraph.jena.AGGraph;
import com.franz.agraph.jena.AGGraphMaker;
import com.franz.agraph.jena.AGGraphUnion;
import com.franz.agraph.jena.AGInfModel;
import com.franz.agraph.jena.AGModel;
import com.franz.agraph.jena.AGQueryExecution;
import com.franz.agraph.jena.AGQueryExecutionFactory;
import com.franz.agraph.jena.AGQueryFactory;
import com.franz.agraph.jena.AGReasoner;
import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;



/**
 * Class GateNosqlBean implements two interfaces GateNosqlLocal and GateNosqlRemote.
 * The methods are used for managing ontologies, managing AllegroGraph database structure
 * and for querying AllegroGraph database.
 * 
 * @author Jakub Mareček (404364)
 */


@Stateless
public class GateNosqlBean implements GateNosqlLocal, GateNosqlRemote {

    
    /**
     * AGServer server is a client-side server object that accesses the AllegroGraph
     */
    private AGServer server;
    
    /**
     * AGCatalog catalog represents an access to the data hierarchy
     */
    private AGCatalog catalog;
        
    /**
     * Logger LOGGER provides logging
     */
    private static final  Logger LOGGER = Logger.getLogger(GateNosqlBean.class.getName());
      
    
    
    @Override
    public ArrayList<String> getAllCatalogs() {
        ArrayList<String> allCatalogs = null;
        server = getServer();
        try {
            allCatalogs = new ArrayList<>(server.listCatalogs());
        } catch (AGHttpException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            return allCatalogs;
        }
    }
    
    @Override
    public ArrayList<String> getAllRepositories() {
        ArrayList<String> allRepositories = null;
        try {
            catalog = getCatalog();
            allRepositories = new ArrayList<>(catalog.listRepositories());
        } catch (OpenRDFException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            return allRepositories;
        }       
    }
    
    @Override
    public boolean createRepository(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        
        boolean result = false;
        try {
            catalog = getCatalog();
            // Repository does not exist
            if (!getAllRepositories().contains(repositoryId)) {
                // Creation
                AGRepository newRepository = catalog.createRepository(repositoryId);
                newRepository.initialize();
                craeateDefaultIncices(newRepository);
                result = true;
            }
            else {
                throw new RepositoryException("Repository: "+repositoryId+" elready exists.");
            }
        } catch (RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            return result;
        }
    }
    
    @Override
    public boolean deleteRepository(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        
        boolean result = false;
        try {
            catalog = getCatalog();
            // Repository does exist
            if (getAllRepositories().contains(repositoryId)) {
                // Erasure
                catalog.deleteRepository(repositoryId);
                result = true;   
            }
            else {
                throw new RepositoryException("Repository: "+repositoryId+" does not exist.");
            }
        } catch (RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            return result;
        }
    }
    
    @Override
    public ArrayList<String> getCurrentIndexes(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        
        ArrayList<String> indices = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            indices = new ArrayList<>(connection.listIndices());
        } catch(OpenRDFException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return indices;
            }
        }
    }
    
    @Override
    public boolean addIndexes(String repositoryId, ArrayList<String> indexes) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (indexes == null) {
            throw new IllegalArgumentException("Argument indexes can not be null.");
        }
        
        boolean result = false;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            for (String index : indexes) {
                connection.addIndex(index);
            }
            connection.commit();
            result = true;
        } catch(RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.rollback();
                closeConnection(connection);
            } catch (RepositoryException ex) {
               LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public boolean dropIndexes(String repositoryId, ArrayList<String> indexes) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (indexes == null) {
            throw new IllegalArgumentException("Argument indices can not be null.");
        }
        
        boolean result = false;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            for (String index : indexes) {
                connection.dropIndex(index);
            }
            connection.commit();
            result = true;
        } catch(RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.rollback();
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;                
            }
        }
    }
    
    @Override
    public ArrayList<String> getAllSubgraphs(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        
        ArrayList<String> allSubgraphs = new ArrayList();
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            List<org.openrdf.model.Resource> allResources = connection.getContextIDs().asList();
            for (org.openrdf.model.Resource r : allResources) {
                allSubgraphs.add(r.toString());
            }
        } catch (RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return allSubgraphs;
            }
        }
    }
    
    @Override
    public boolean loadOntologyFromFile(String repositoryId, String subgraphId, String baseUri, String filePath) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphId == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (baseUri == null) {
            throw new IllegalArgumentException("Argument baseUri can not be null.");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("Argument filePath can not be null.");
        }
        
        boolean result = false;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            List<String> allSubgraphs = getAllSubgraphs(repositoryId);
            AGModel model = null;
            // Subgaph does not exist yet
            if (!allSubgraphs.contains(subgraphId)) {
                // Create subgraph and load onlogy
                model =  createSubgraphModel(connection, subgraphId);
            }
            else {
                // Load ontology to existing subgraph
                model = getSubgraphModel(connection, repositoryId, subgraphId);
            }
            model.read(new FileInputStream(filePath), baseUri);
            connection.commit();
            result = true;
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.rollback();
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    
    @Override
    public boolean loadOntologyFromWeb(String repositoryId, String subgraphId, String baseUri, String url) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphId == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (baseUri == null) {
            throw new IllegalArgumentException("Argument baseUri can not be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Argument url can not be null.");
        }
        
        boolean result = false;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            List<String> allSubgraphs = getAllSubgraphs(repositoryId);
            AGModel model = null;
            // Subgaph does not exist yet
            if (!allSubgraphs.contains(subgraphId)) {
                // Create subgraph and load onlogy
                model =  createSubgraphModel(connection, subgraphId);
            }
            else {
                // Load ontology to existing subgraph
                model = getSubgraphModel(connection, repositoryId, subgraphId);
            }
            model.read(new URL(url).openStream(), baseUri);
            connection.commit();
            result = true;
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.rollback();
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public boolean deleteSubgraph(String repositoryId, String subgraphId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphId == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        
        boolean result = false;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getSubgraphModel(connection, repositoryId, subgraphId);
            model.removeAll();
            connection.commit();
            result = true;
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.rollback();
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public ArrayList<String> querySubgraph(String repositoryId, String subgraphId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphId == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<String> result = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getSubgraphModel(connection, repositoryId, subgraphId);
            result = queryModel(model, query);
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public ArrayList<String> queryInferencedSubgraph(String repositoryId, String subgraphId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphId == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<String> result = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getSubgraphModel(connection, repositoryId, subgraphId);
            AGReasoner reasoner = new AGReasoner();
            InfModel infmodel = new AGInfModel(reasoner, model);
            result = queryModel((AGModel) infmodel, query);
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public ArrayList<String> querySubgraphs(String repositoryId, ArrayList<String> subgraphIds, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphIds == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<String> result = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getUnitedSubgraphsModel(connection, subgraphIds);
            result = queryModel(model, query);
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public ArrayList<String> queryInferencedSubgraphs(String repositoryId, ArrayList<String> subgraphIds, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphIds == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<String> result = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getUnitedSubgraphsModel(connection, subgraphIds);
            AGReasoner reasoner = new AGReasoner();
            InfModel infmodel = new AGInfModel(reasoner, model);
            result = queryModel((AGModel) infmodel, query);
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public ArrayList<String> queryAllSubgraphs(String repositoryId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<String> subgraphIds = getAllSubgraphs(repositoryId);
        return querySubgraphs(repositoryId, subgraphIds, query);
    }
    
    @Override
    public ArrayList<String> queryAllInferencedSubgraphs(String repositoryId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<String> subgraphIds = getAllSubgraphs(repositoryId);
        return queryInferencedSubgraphs(repositoryId, subgraphIds, query);
    }
    
    @Override
    public ArrayList<RDFNode> querySubgraphL(String repositoryId, String subgraphId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphId == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<RDFNode> result = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getSubgraphModel(connection, repositoryId, subgraphId);
            result = queryModelL(model, query);
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }

    @Override
    public ArrayList<RDFNode> queryInferencedSubgraphL(String repositoryId, String subgraphId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphId == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<RDFNode> result = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getSubgraphModel(connection, repositoryId, subgraphId);
            AGReasoner reasoner = new AGReasoner();
            InfModel infmodel = new AGInfModel(reasoner, model);
            result = queryModelL((AGModel) infmodel, query);
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public ArrayList<RDFNode> querySubgraphsL(String repositoryId, ArrayList<String> subgraphIds, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphIds == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<RDFNode> result = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getUnitedSubgraphsModel(connection, subgraphIds);
            result = queryModelL(model, query);
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        }
    }
    
    @Override
    public ArrayList<RDFNode> queryInferencedSubgraphsL(String repositoryId, ArrayList<String> subgraphIds, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphIds == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<RDFNode> result = null;
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model = getUnitedSubgraphsModel(connection, subgraphIds);
            AGReasoner reasoner = new AGReasoner();
            InfModel infmodel = new AGInfModel(reasoner, model);
            result = queryModelL((AGModel) infmodel, query);
        } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result;
            }
        } 
    }
    
    @Override
    public ArrayList<RDFNode> queryAllSubgraphsL(String repositoryId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<String> subgraphIds = getAllSubgraphs(repositoryId);
        return querySubgraphsL(repositoryId, subgraphIds, query);   
    }
    
    @Override
    public ArrayList<RDFNode> queryAllInferencedSubgraphsL(String repositoryId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (query == null) {
            throw new IllegalArgumentException("Argument query can not be null.");
        }
        
        ArrayList<String> subgraphIds = getAllSubgraphs(repositoryId);
        return queryInferencedSubgraphsL(repositoryId, subgraphIds, query);
    }
    
    @Override
    public String exportSubgraph(String repositoryId, String subgraphId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphId == null) {
            throw new IllegalArgumentException("Argument subgraphId can not be null.");
        }

        StringBuilder result = new StringBuilder("");
        OutputStream output = new ByteArrayOutputStream();
        AGRepositoryConnection connection = null;
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getSubgraphModel(connection, repositoryId, subgraphId);
            model.write(output);
            result.append(output);
            output.close();
        } catch (IOException | RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result.toString();
            }
        }
    }
    
    @Override
    public String exportSubgraphs(String repositoryId, ArrayList<String> subgraphIds) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }
        if (subgraphIds == null) {
            throw new IllegalArgumentException("Argument subgraphIds can not be null.");
        }
        
        StringBuilder result = new StringBuilder("");
        OutputStream output = new ByteArrayOutputStream();
        AGRepositoryConnection connection = getConnection(repositoryId);
        try {
            connection = getConnection(repositoryId);
            connection.setAutoCommit(false);
            AGModel model =  getUnitedSubgraphsModel(connection, subgraphIds);
            model.write(output);
            model.write(output);
            result.append(output);
            output.close();
        } catch (IOException | RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.setAutoCommit(true);
                closeConnection(connection);
            } catch (RepositoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                return result.toString();
            }
        }
    }
    
    @Override
    public String exportAllSubgraphs(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Argument repositoryId can not be null.");
        }

        ArrayList<String> subgraphIds = getAllSubgraphs(repositoryId);
        return exportSubgraphs(repositoryId, subgraphIds);
    }
    
    
    
    private AGRepositoryConnection getConnection(String repositoryId) {
        AGRepositoryConnection connection = null;
        try {
           AGRepository repository = getRepository(repositoryId);
           connection = repository.getConnection();
           } catch (RepositoryException ex) {
               LOGGER.log(Level.SEVERE, null, ex);
        }
        return connection;
    }
    
    private void closeConnection(AGRepositoryConnection connection) {
        try {
            connection.close();
        } catch (RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private AGServer getServer() {
        if (server == null) {
            try {
                Properties proporites = new Properties();
                proporites.load(new FileInputStream("configuration/database.properties"));
                server = new AGServer(proporites.getProperty("server_url"),
                                      proporites.getProperty("username"),
                                      proporites.getProperty("password"));
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }  catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return server;
    }
    
    private AGCatalog getCatalog() {
        if (catalog == null) {
            server = getServer();
            try {
                Properties proporites = new Properties();
                proporites.load(new FileInputStream("configuration/database.properties"));
                catalog = server.getCatalog(proporites.getProperty("catalog_id"));
            } catch (IOException | AGHttpException ex) {
               LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return catalog;
    }
    
    private AGRepository getRepository(String repositoryId) {
        AGRepository repository = null;
        try {
            catalog = getCatalog();
            repository = catalog.openRepository(repositoryId);
        } catch (RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            return repository;
        }
    }

    private void craeateDefaultIncices(AGRepository repositoryId) {
        try {
            for (String index : repositoryId.getConnection().listIndices()) {
                if (index.equals("i")){
                    continue;
                }
                repositoryId.getConnection().dropIndex(index);
            }
            repositoryId.getConnection().addIndex("gspoi");
            repositoryId.getConnection().addIndex("gsopi");
            repositoryId.getConnection().addIndex("gpsoi");
            repositoryId.getConnection().addIndex("gposi");
            repositoryId.getConnection().addIndex("gopsi");
            repositoryId.getConnection().addIndex("gospi");
        } catch (OpenRDFException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private AGModel createSubgraphModel(AGRepositoryConnection connection, String subgraphId) {
        AGGraphMaker maker = new AGGraphMaker(connection);
        AGGraph graph = maker.createGraph(subgraphId);
        AGModel model = new AGModel(graph);
        return model;
    }
    
    private AGModel getSubgraphModel(AGRepositoryConnection connection, String repositoryId, String subgraphId) throws RepositoryException {
        List<String> allSubgraphs = getAllSubgraphs(repositoryId);
        AGModel model = null;
        if (allSubgraphs.contains(subgraphId)) {
            AGGraphMaker maker = new AGGraphMaker(connection);
            AGGraph graph = maker.openGraph(subgraphId);
            model = new AGModel(graph);
        }
        else {
            throw new RepositoryException("Subgraph: "+subgraphId+ " in repository: "+repositoryId+" does not exist.");
        }
        return model;        
    }
    
    private AGGraph getGraph(AGRepositoryConnection connection, String subgraphId) {
        AGGraphMaker maker = new AGGraphMaker(connection);
        AGGraph graph = maker.openGraph(subgraphId);
        return graph;
    }
    
    private AGModel getUnitedSubgraphsModel(AGRepositoryConnection connection, ArrayList<String> subgraphIds) {
        AGGraphMaker maker = new AGGraphMaker(connection);
        List<AGGraph> graphs = new ArrayList<>();
        for (String subgraph : subgraphIds) {
            graphs.add(getGraph(connection, subgraph));
        }
        AGGraphUnion union = maker.createUnion(graphs.toArray(new AGGraph[graphs.size()]));
        AGModel model = new AGModel(union);
        return model;
    }
    
    private ArrayList<String> queryModel(AGModel model, String query) {
        ArrayList<String> result =  new ArrayList<>();
        com.franz.agraph.jena.AGQuery spargl = AGQueryFactory.create(query);
        AGQueryExecution qe = AGQueryExecutionFactory.create(spargl, model);
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution ontology = results.next();
            RDFNode s = ontology.get("s");
            RDFNode p = ontology.get("p");
            RDFNode o = ontology.get("o");
            result.add("{" + s + " " + p + " "+ o + "}");
        }
        return result;
    }

    private ArrayList<RDFNode> queryModelL(AGModel model, String query) {
        ArrayList<RDFNode> result =  new ArrayList<>();
        com.franz.agraph.jena.AGQuery spargl = AGQueryFactory.create(query);
        AGQueryExecution qe = AGQueryExecutionFactory.create(spargl, model);
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution ontology = results.next();
            result.add(ontology.get("s"));
            result.add(ontology.get("p"));
            result.add(ontology.get("o"));
        }
        return result;
    }
}
