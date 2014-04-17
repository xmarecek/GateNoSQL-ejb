/*
* Project :       Master's Thesis - NoSQL Database for Storing OWL Data
* Document :      GateNosqlRemote.java
* Author :        Bc. Jakub Mareček  <xmarecek@mail.muni.cz>
* Organization: : FI MUNI <http://www.fi.muni.cz>
*/


package cz.muni.fi.gate;

import java.util.ArrayList;
import javax.ejb.Remote;



/**
 * Interface GateNosqlRemote declares prototypes of methods for managing and 
 * querying AllegroGraph database.
 * It works as a remote application interface.
 * 
 * @author Jakub Mareček (404364)
 */


@Remote
public interface GateNosqlRemote {
    
    
    
   /**
    * Returns all catalogs.
    * @return all catalogs
    */
    public ArrayList<String> getAllCatalogs();
            
    /**
     * Returns all already created repositories in java-catalog.
     * @return all repositories
     */
    public ArrayList<String> getAllRepositories();
    
    /**
     * Creates a new repository with the given unique name.
     * The name should be in the format of URI. The method sets suitable default
     * indexes for subgraph hierarchy: [i, gspoi, gsopi, gpsoi, gposi, gospi, gopsi].
     * @param repositoryId String
     * @return true if the creation succeeds or false otherwise
     */
    public boolean createRepository(String repositoryId);
    
    /**
     * Erases the given repository.
     * All subgraphs created in the repository are erased too.
     * @param repositoryId String
     * @return true if the erasure succeeds or false otherwise
     */
    public boolean deleteRepository(String repositoryId);
    
    /**
     * Returns current indexes in the given repository.
     * @param repositoryId String
     * @return current indexes
     */
    public ArrayList<String> getCurrentIndexes(String repositoryId);
    
    /**
     * Creates indexes in the given repository.
     * List of all valid indexes:
     * [spogi, spgoi, sopgi, sogpi, sgpoi, sgopi, psogi, psgoi, posgi, pogsi,
     * pgsoi, pgosi, ospgi, osgpi, opsgi, opgsi, ogspi, ogpsi, gspoi, gsopi, gpsoi, gposi,
     * gospi, gopsi, i].
     * @param repositoryId String
     * @param indexes ArrayList<String>
     * @return true if the creation succeeds or false otherwise
     */
    public boolean addIndexes(String repositoryId, ArrayList<String> indexes);
    
    /**
     * Erases already created indexes in the given repository.
     * @param repositoryId String
     * @param indexes ArrayList<String>
     * @return true if the erasure succeeds or false otherwise
     */
    public boolean dropIndexes(String repositoryId, ArrayList<String> indexes);
    
    /**
     * Returns all existing subgraphs in the given repository.
     * @param repositoryId
     * @return all subgraphs
     */
    public ArrayList<String> getAllSubgraphs(String repositoryId);
        
    /**
     * Loads ontology from the given file.
     * The file format needs to be either RDF/XML or NTtriple. The ontology is
     * stored in the given subgraph in the given repository. The subgraph either
     * exists and the ontology is added or the subgraph is created before loading
     * the ontology.
     * @param repositoryId String
     * @param subgraphId String
     * @param baseUri String
     * @param filePath String
     * @return true if the loading succeeds or false otherwise
     */
    public boolean loadOntologyFromFile(String repositoryId, String subgraphId, String baseUri, String filePath);
    
    /**
     * Loads ontology from the given URL.
     * The file format needs to be either RDF/XML or NTtriple. The ontology is
     * stored in the given subgraph in the given repository. The subgraph either
     * exists and the ontology is added or the subgraph is created before loading
     * the ontology.
     * @param repositoryId String
     * @param subgraphId String
     * @param baseUri String
     * @param filePath String
     * @return true if the loading succeeds or false otherwise
     */
    public boolean loadOntologyFromWeb(String repositoryId, String subgraphId, String baseUri, String url);
    
    /**
     * Erases the given subgraph in the repository.
     * All subgraphs created in the repository are erased too.
     * @param repositoryId String
     * @param subgraphId String
     * @return true if the erasure succeeds or false otherwise
     */
    public boolean deleteSubgraph(String repositoryId, String subgraphId);
    
    /**
     * Queries the given subgraph in the given repository.
     * The method returns queried OWL data as a list of strings.
     * @param repositoryId String
     * @param subgraphId String
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<String> querySubgraph(String repositoryId, String subgraphId, String query);
    
    /**
     * Queries the given subgraph with reasoning in the given repository.
     * The method returns queried OWL data as a list of strings.
     * @param repositoryId String
     * @param subgraphId String
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<String> queryInferencedSubgraph(String repositoryId, String subgraphId, String query);
    
    /**
     * Queries the given subgraphs in the given repository.
     * The method returns queried OWL data as a list of strings.
     * @param repositoryId String
     * @param subgraphIds ArrayList<String>
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<String> querySubgraphs(String repositoryId, ArrayList<String> subgraphIds, String query);
    
    /**
     * Queries the given subgraphs with reasoning in the given repository.
     * The method returns queried OWL data as a list of strings.
     * @param repositoryId String
     * @param subgraphIds ArrayList<String>
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<String> queryInferencedSubgraphs(String repositoryId, ArrayList<String> subgraphIds, String query);
    
    /**
     * Queries all subgraphs in the given repository.
     * The method returns queried OWL data as a list of strings.
     * @param repositoryId String
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<String> queryAllSubgraphs(String repositoryId, String query);
    
    /**
     * Queries all subgraphs with reasoning in the given repository.
     * The method returns queried OWL data as a list of strings.
     * @param repositoryId String
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<String> queryAllInferencedSubgraphs(String repositoryId, String query);
    
    /**
     * Returns ontology that is stored in the given subgraph in the given repository as a string.
     * @param repositoryId String
     * @param subgraphId String
     * @return ontology
     */
    public String exportSubgraph(String repositoryId, String subgraphId);
    
    /**
     * Returns ontologies stored in the given subgraphs in the given repository as a string.
     * @param repositoryId String
     * @param subgraphIds ArrayList<String>
     * @return ontologies
     */
    public String exportSubgraphs(String repositoryId, ArrayList<String> subgraphIds);
    
    /**
     * Eeturns ontologies stored in all subgraphs in the given repository as a string.
     * @param repositoryId String
     * @return ontologies
     */
    public String exportAllSubgraphs(String repositoryId);
}
