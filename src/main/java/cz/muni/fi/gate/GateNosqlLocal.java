/*
* Project :       Master's Thesis - NoSQL Database for Storing OWL Data
* Document :      GateNosqlLocal.java
* Author :        Bc. Jakub Mareček  <xmarecek@mail.muni.cz>
* Organization: : FI MUNI <http://www.fi.muni.cz>
*/


package cz.muni.fi.gate;

import com.hp.hpl.jena.rdf.model.RDFNode;
import java.util.ArrayList;
import javax.ejb.Local;



/**
 * Interface GateNosqlLocal declares prototypes of methods for querying AllegroGraph
 * database. It extends GateNosqlRemote interface.
 * It works as a local application interface.
 * 
 * @author Jakub Mareček (404364)
 */


@Local
public interface GateNosqlLocal extends GateNosqlRemote {
    
    
    
    /**
     * Queries the given subgraph in the given repository.
     * The method returns queried OWL data as a list of RDFNodes.
     * @param repositoryId String
     * @param subgraphId String
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<RDFNode> querySubgraphL(String repositoryId, String subgraphId, String query);
    
    /**
     * Queries the given subgraph with reasoning in the given repository.
     * The method returns queried OWL data as a list of RDFNodes.
     * @param repositoryId String
     * @param subgraphId String
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<RDFNode> queryInferencedSubgraphL(String repositoryId, String subgraphId, String query);

 
    /**
     * Queries the given subgraphs in the given repository.
     * The method returns queried OWL data as a list of RDFNodes.
     * @param repositoryId String
     * @param subgraphIds ArrayList<String>
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<RDFNode> querySubgraphsL(String repositoryId, ArrayList<String> subgraphIds, String query);
    
    /**
     * Queries the given subgraphs with reasoning in the given repository.
     * The method returns queried OWL data as a list of RDFNodes.
     * @param repositoryId String
     * @param subgraphIds ArrayList<String>
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<RDFNode> queryInferencedSubgraphsL(String repositoryId, ArrayList<String> subgraphIds, String query);
    
    /**
     * Queries all subgraphs in the given repository.
     * The method returns queried OWL data as a list of RDFNodes.
     * @param repositoryId String
     * @param query String
     * @return all queried OWL data
     */
    public ArrayList<RDFNode> queryAllSubgraphsL(String repositoryId, String query);
    
    /**
     * Queries all subgraphs with reasoning in the given repository.
     * The method returns queried OWL data as a list of RDFNodes.
     * @param repositoryId String
     * @param query String
     * @return all queried OWL data 
     */
    public ArrayList<RDFNode> queryAllInferencedSubgraphsL(String repositoryId, String query);
}
