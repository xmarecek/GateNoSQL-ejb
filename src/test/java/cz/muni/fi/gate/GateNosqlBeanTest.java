/*
* Project :       Master's Thesis - NoSQL Database for Storing OWL Data
* Document :      GateNosqlBeanTest.java
* Author :        Bc. Jakub Mareček  <xmarecek@mail.muni.cz>
* Organization: : FI MUNI <http://www.fi.muni.cz>
*/


package cz.muni.fi.gate;

import com.hp.hpl.jena.rdf.model.RDFNode;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;



/**
 * Class GateNosqlBeanTest is used for testing.
 * 
 * @author Jakub Mareček (404364)
 */


public class GateNosqlBeanTest {
    
    
    private static EJBContainer container;
    
    private static GateNosqlRemote remoteService;
    
    private static GateNosqlLocal localService;
    
    private static final String FILE_PATH_ONTOLOGY_TEST_1 = "src/test/resources/test_ontology1.rdf";
    
    private static final String FILE_PATH_ONTOLOGY_TEST_2 = "src/test/resources/test_ontology2.rdf";
    
    private static final String URL_ONTOLOGY_TEST = "http://www.w3.org/TR/owl-guide/wine.rdf";
    
    private static final String REPOSITORY_ID_TEST = "gate-test";
    
    private static final String REPOSITORY_ID_TEST_NOT_EXISTING = "repository-not-existing";
    
    private static final String SUBGRAPH_ID_TEST_1 = "http://example.org#gate-context1";

    private static final String SUBGRAPH_ID_TEST_2 = "http://example.org#gate-context2";
    
    private static final String BASE_IRI_TEST = "http://example.org/example/local";
    
    private static final String QUERY_TEST = "SELECT ?s ?p ?o WHERE {?s <http://example.org/ontology/fatherOf> ?o .}";
    
            
            
    public GateNosqlBeanTest() {
    }
    

    @BeforeClass
    public static void setUpClass() throws Exception {
        container = EJBContainer.createEJBContainer();
    }
    
    
    @AfterClass
    public static void tearDownClass() {
        if (container != null) {
            container.close();
        }
    }
    
    @Before
    public void setUp() {
        try {
            remoteService = (GateNosqlRemote) container.getContext().lookup("java:global/classes/GateNosqlBean!cz.muni.fi.gate.GateNosqlRemote");
            localService = (GateNosqlLocal) container.getContext().lookup("java:global/classes/GateNosqlBean!cz.muni.fi.gate.GateNosqlLocal");
        } catch (NamingException ex) {
            Logger.getLogger(GateNosqlBeanTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {
    }

    
    
    /**
     * Test of getAllCatalogs method, of class GateNosqlBean.
     * We test that "java-catalog" is presented (this catalog is entirely used just by Java clients). It also tests
     * if a connection is established to the AllegroGraph database.
     */
    @Test
    public void testGetAllCatalogs() throws Exception {
        System.out.println("getAllCatalogs");
        ArrayList<String> result = remoteService.getAllCatalogs();
        assertTrue(result.contains("java-catalog"));
    }

    
    /**
     * Test of getAllRepositories method, of class GateNosqlBean.
     * We test that the database is empty. Otherwise some test methods might not run correctly. In order to run the
     * tests correctly, please empty the database.
     */
    @Test
    public void testGetAllRepositories() throws Exception {
        System.out.println("getAllRepositories");
        ArrayList<String> result = remoteService.getAllRepositories();
        assertTrue("The AllegroGraph database is not empty. Please, empty the database and run the again.", result.isEmpty());
    }

    
    /**
     * Test of createRepository method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if the argument is null. Afterwards, we test
     * successful creation of repository "gate-test", failure of creation of the same repository "gate-test"
     * and finally that the database contains exactly one repository "gate-test".
     */
    @Test
    public void testCreateRepository() throws Exception {
        System.out.println("createRepository");
        
        // Null arguments
        try {
            remoteService.createRepository(null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));

        // Creation of the same repository "gate-test" fails and componet throws RepositoryException (check log)
        assertFalse(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        ArrayList<String> expRepositories = new ArrayList<>();
        expRepositories.add(REPOSITORY_ID_TEST);
        ArrayList<String> result = remoteService.getAllRepositories();
        // AllegroGraph database contains exactly one repository "gate-test"
        assertEquals(expRepositories.size(), result.size());
        for (String repository : expRepositories) {
            result.contains(repository);
        }
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    
    /**
     * 
     * Test of deleteRepository method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if the argument is null. Afterwards, we test
     * successful erasure of repository "gate-test" and that erasure of not existing repository fails.
     */
    @Test
    public void testDeleteRepository() throws Exception {
        System.out.println("deleteRepository");
        // Null arguments
        try {
            remoteService.deleteRepository(null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Creation of "gate-test" repository succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Erasure of "gate-test" repository succeeds
        assertTrue(remoteService.deleteRepository(REPOSITORY_ID_TEST));
        
        ArrayList<String> result = remoteService.getAllRepositories();
        // AllegroGraph database is empty
        assertTrue(result.isEmpty());
        
        // Erasure of not existing repository fails and componet throws RepositoryException (check log)
        assertFalse(remoteService.deleteRepository(REPOSITORY_ID_TEST_NOT_EXISTING));
    }


    /**
     * Test of getCurrentIndices method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if the argument is null. Afterwards, we test
     * that the newly created repository "gate-test" contains just default indexes.
     */
    @Test
    public void testGetCurrentIndexes() {
        System.out.println("getCurrentIndices");
        // Null arguments
        try {
            remoteService.getCurrentIndexes(null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        ArrayList<String> currentIndexes = remoteService.getCurrentIndexes(REPOSITORY_ID_TEST);
        // Repository "gate-test" contains just default indexes
        assertEquals(returnDefaultIndexes().size(), currentIndexes.size());
        for (String index : returnDefaultIndexes()) {
            assertTrue(currentIndexes.contains(index));
        }
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    
    /**
     * Test of addIndices method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the added indexes are built in "gate-test" repository and insertion of wrong indexes fails.
     */
    @Test
    public void testAddIndexes() {
        System.out.println("addIndices");
        // Null arguments
        try {
            remoteService.addIndexes("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.addIndexes(null, new ArrayList<String>());
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        ArrayList<String> currentIndexes = remoteService.getCurrentIndexes(REPOSITORY_ID_TEST);
        assertEquals(this.returnDefaultIndexes().size(), currentIndexes.size());
        for (String index : this.returnDefaultIndexes()) {
            assertTrue(currentIndexes.contains(index));
        }
        
        ArrayList<String> toBeAddedWrongIndexes = new ArrayList<>();
        toBeAddedWrongIndexes.add("not-existing");
        // Insertion of wrong indices fails and componet throws RepositoryException (check log)
        assertFalse(remoteService.addIndexes(REPOSITORY_ID_TEST, toBeAddedWrongIndexes));
        
        ArrayList<String> toBeAddedCorrectIndexes = new ArrayList<>();
        toBeAddedCorrectIndexes.add("pgsoi");
        toBeAddedCorrectIndexes.add("pgosi");
        ArrayList<String> expectedIndexes = this.returnDefaultIndexes();
        expectedIndexes.add("pgsoi");
        expectedIndexes.add("pgosi");
        // Insertion of correct indices successes
        assertTrue(remoteService.addIndexes(REPOSITORY_ID_TEST, toBeAddedCorrectIndexes));
        
        currentIndexes = remoteService.getCurrentIndexes(REPOSITORY_ID_TEST);
        // Repository "gate-test" contains default and newly added indexes
        assertEquals(expectedIndexes.size(), currentIndexes.size());
        for (String index : expectedIndexes) {
            assertTrue(expectedIndexes.contains(index));
        }
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    
    /**
     * Test of dropIndices method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the dropped indexes are removed from "gate-test" repository and erasure of wrong indexes fails.
     */
    @Test
    public void testDropIndexes() {
        System.out.println("dropIndices");
        // Null arguments
        try {
            remoteService.dropIndexes("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.dropIndexes(null, new ArrayList<String>());
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
         // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        ArrayList<String> currentIndexes = remoteService.getCurrentIndexes(REPOSITORY_ID_TEST);
        assertEquals(this.returnDefaultIndexes().size(), currentIndexes.size());
        for (String index : this.returnDefaultIndexes()) {
            assertTrue(currentIndexes.contains(index));
        }
        
        ArrayList<String> toBeDroppedWrongIndexes = new ArrayList<>();
        toBeDroppedWrongIndexes.add("not-existing");
        // Erasure of wrong indices fails and componet throws RepositoryException (check log)
        assertFalse(remoteService.dropIndexes(REPOSITORY_ID_TEST, toBeDroppedWrongIndexes));
        
        ArrayList<String> toBeDroppedCorrectIndexes = new ArrayList<>();
        toBeDroppedCorrectIndexes.add("posgi");
        toBeDroppedCorrectIndexes.add("spogi");
        ArrayList<String> expectedIndexes = this.returnDefaultIndexes();
        expectedIndexes.remove("posgi");
        expectedIndexes.remove("spogi");
        // Erasure of correct indices successes
        assertTrue(remoteService.dropIndexes(REPOSITORY_ID_TEST, toBeDroppedCorrectIndexes));
        
        currentIndexes = remoteService.getCurrentIndexes(REPOSITORY_ID_TEST);
        // Repository "gate-test" contains default minus dropped indexes
        assertEquals(expectedIndexes.size(), currentIndexes.size());
        for (String index : expectedIndexes) {
            assertTrue(expectedIndexes.contains(index));
        }
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    
    /**
     * Test of loadOntology method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the ontology is successfully loaded from a file.
     */
    @Test
    public void testLoadOntologyFromFile() throws Exception {
        System.out.println("loadOntologyFromFile");
        // Null arguments
        try {
            remoteService.loadOntologyFromFile(null, "", "", "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.loadOntologyFromFile("", null, "", "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.loadOntologyFromFile("", "", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.loadOntologyFromFile("", "", "", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of loadOntologyFromWeb method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the ontology is successfully loaded from a web.
     */
    @Test
    public void tesLoadOntologyFromWeb() {
        System.out.println("loadOntologyFromWeb");
        // Null arguments
        try {
            remoteService.loadOntologyFromWeb(null, "", "", "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.loadOntologyFromWeb("", null, "", "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.loadOntologyFromWeb("", "", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.loadOntologyFromWeb("", "", "", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load correct ontology
        assertTrue(remoteService.loadOntologyFromWeb(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, URL_ONTOLOGY_TEST));
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of getAllSubgraphs method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the repository "gate-test" contains exactly created subgraphs.
     */
    @Test
    public void testGetAllSubgraphs() {
        System.out.println("getAllSubgraphs");
        // Null arguments
        try {
            remoteService.getAllSubgraphs(null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        // Load second correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_2, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        ArrayList<String> expectedSubgraphs = new ArrayList<>();
        expectedSubgraphs.add(SUBGRAPH_ID_TEST_1);
        expectedSubgraphs.add(SUBGRAPH_ID_TEST_2);
        ArrayList<String> currentSugraphs = remoteService.getAllSubgraphs(REPOSITORY_ID_TEST);
        // Repository "gate-test" contains exactly these subgraphs
        assertEquals(expectedSubgraphs.size(), currentSugraphs.size());
        for (String subgraph : expectedSubgraphs) {
            assertTrue(expectedSubgraphs.contains(subgraph));
        }
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of deleteSubgraph method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the repository "gate-test" contains exactly created subgraphs minus deleted subgraphs.
     */
    @Test
    public void testDeleteSubgraph() {
        System.out.println("deleteSubgraph");
        // Null arguments
        try {
            remoteService.deleteSubgraph(null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.deleteSubgraph("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        // Load second correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_2, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        ArrayList<String> expectedSubgraphs = new ArrayList<>();
        expectedSubgraphs.add(SUBGRAPH_ID_TEST_1);
        expectedSubgraphs.add(SUBGRAPH_ID_TEST_2);
        ArrayList<String> currentSugraphs = remoteService.getAllSubgraphs(REPOSITORY_ID_TEST);
        // Repository "gate-test" contains just those subgraphs
        assertEquals(expectedSubgraphs.size(), currentSugraphs.size());
        for (String subgraph : expectedSubgraphs) {
            assertTrue(expectedSubgraphs.contains(subgraph));
        }
        
        expectedSubgraphs.remove(SUBGRAPH_ID_TEST_1);
        // Erasure of existing subgraph succeeds
        assertTrue(remoteService.deleteSubgraph(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1));
        expectedSubgraphs.remove(SUBGRAPH_ID_TEST_1);
        currentSugraphs = remoteService.getAllSubgraphs(REPOSITORY_ID_TEST);
        // Repository "gate-test" contains just one subgraph after erasure
        assertEquals(expectedSubgraphs.size(), currentSugraphs.size());
        for (String subgraph : expectedSubgraphs) {
            assertTrue(expectedSubgraphs.contains(subgraph));
        }
        
        // Erasure of not existing subgraph fails and componet throws RepositoryException (check log)
        assertFalse(remoteService.deleteSubgraph(REPOSITORY_ID_TEST, "http://example.org#gate-not-existing"));
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of querySubgraph method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the response contains exactly one result with respect to the loaded ontology.
     */
    @Test
    public void testQuerySubgraph() throws Exception {
        System.out.println("querySubgraph");
        // Null arguments
        try {
            remoteService.querySubgraph(null, "", "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.querySubgraph("", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.querySubgraph("", "", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add("{http://example.org/people/bob null http://example.org/people/bobby}");
        ArrayList<String> currentResult = remoteService.querySubgraph(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, QUERY_TEST);
        // Response contains exactly one result
        assertEquals(expectedResult.size(), currentResult.size());
        for (String result : currentResult) {
            assertTrue(expectedResult.contains(result));
        }
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of querySubgraph method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the response contains exactly two results with respect to the loaded ontology.
     */
    @Test
    public void testQueryInferencedSubgraph() throws Exception {
        System.out.println("querySubgraph");
        // Null arguments
        try {
            remoteService.queryInferencedSubgraph(null, "", "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.queryInferencedSubgraph("", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.queryInferencedSubgraph("", "", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add("{http://example.org/people/bob null http://example.org/people/bobby}");
        expectedResult.add("{http://example.org/people/employee null http://example.org/people/bobby}");
        ArrayList<String> currentResult = remoteService.queryInferencedSubgraph(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, QUERY_TEST);
        // Response contains exactly two results
        assertEquals(expectedResult.size(), currentResult.size());
        for (String result : currentResult) {
            assertTrue(expectedResult.contains(result));
        }
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }

    /**
     * Test of querySubgraphs method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the response contains exactly two results with respect to the loaded ontologies.
     */
    @Test
    public void testGuerySubgraphs() {
        System.out.println("querySubgraphs");
        // Null arguments
        try {
            remoteService.querySubgraphs(null, new ArrayList<String>(), "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.querySubgraphs("", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.querySubgraphs("", new ArrayList<String>(), null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
       // Load second correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_2, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_2));        
        
        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add("{http://example.org/people/bob null http://example.org/people/bobby}");
        expectedResult.add("{http://example.org/people/paul null http://example.org/people/paula}");
        ArrayList<String> subgraphs = new ArrayList<>();
        subgraphs.add(SUBGRAPH_ID_TEST_1);
        subgraphs.add(SUBGRAPH_ID_TEST_2);
        ArrayList<String> currentResult = remoteService.querySubgraphs(REPOSITORY_ID_TEST, subgraphs, QUERY_TEST);
        // Response contains exactly two results
        assertEquals(expectedResult.size(), currentResult.size());
        for (String result : currentResult) {
            assertTrue(expectedResult.contains(result));
        }
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }

    /**
     * Test of queryInferencedSubgraphs method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the response contains exactly three results with respect to the loaded ontologies.
     */
    @Test
    public void testQueryInferencedSubgraphs() {
        System.out.println("queryInferencedSubgraphs");
        // Null arguments
        try {
            remoteService.queryInferencedSubgraphs(null, new ArrayList<String>(), "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.queryInferencedSubgraphs("", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.queryInferencedSubgraphs("", new ArrayList<String>(), null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
       // Load second correct ontlogy
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_2, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_2));        
        
        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add("{http://example.org/people/bob null http://example.org/people/bobby}");
        expectedResult.add("{http://example.org/people/paul null http://example.org/people/paula}");
        expectedResult.add("{http://example.org/people/employee null http://example.org/people/bobby}");
        ArrayList<String> subgraphs = new ArrayList<>();
        subgraphs.add(SUBGRAPH_ID_TEST_1);
        subgraphs.add(SUBGRAPH_ID_TEST_2);
        ArrayList<String> currentResult = remoteService.queryInferencedSubgraphs(REPOSITORY_ID_TEST, subgraphs, QUERY_TEST);
        // Response contains exactly three results
        assertEquals(expectedResult.size(), currentResult.size());
        for (String result : currentResult) {
            assertTrue(expectedResult.contains(result));
        }
       
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of queryAllSubgraphs method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null.
     */
    @Test
    public void testQueryAllSubgraphs() {
        System.out.println("queryAllSubgraphs");
        // Null arguments
        try {
            remoteService.queryAllSubgraphs(null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.queryAllSubgraphs("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
    }
    
    /**
     * Test of queryAllInferencedSubgraphs method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null.
     */
    @Test
    public void testQueryAllInferencedSubgraphs() {
        System.out.println("queryAllInferencedSubgraphs");
        // Null arguments
        try {
            remoteService.queryAllInferencedSubgraphs(null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.queryAllInferencedSubgraphs("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
    }
    
    /**
     * Test of querySubgraphL method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the response is not empty.
     */
    @Test
    public void testQuerySubgraphL() throws Exception {
        System.out.println("querySubgraphL");
        // Null arguments
        try {
            localService.querySubgraphL(null, "", "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.querySubgraphL("", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.querySubgraphL("", "", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        ArrayList<RDFNode> currentResult = localService.querySubgraphL(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, QUERY_TEST);
        // Response is not empty
        assertNotNull(currentResult);
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of querySubgraphL method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the response is not empty.
     */
    @Test
    public void testQueryInferencedSubgraphL() throws Exception {
        System.out.println("queryInferencedSubgraphL");
        // Null arguments
        try {
            localService.queryInferencedSubgraphL(null, "", "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.queryInferencedSubgraphL("", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.queryInferencedSubgraphL("", "", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        

        ArrayList<RDFNode> currentResult = localService.queryInferencedSubgraphL(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, QUERY_TEST);
        // Response is not empty
        assertNotNull(currentResult);
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }

    /**
     * Test of querySubgraphsL method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the response is not empty.
     */
    @Test
    public void testGuerySubgraphsL() {
        System.out.println("querySubgraphsL");
        // Null arguments
        try {
            localService.querySubgraphsL(null, new ArrayList<String>(), "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.querySubgraphsL("", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.querySubgraphsL("", new ArrayList<String>(), null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
       // Load second ontlogy
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_2, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_2));        
        
        ArrayList<String> subgraphs = new ArrayList<>();
        subgraphs.add(SUBGRAPH_ID_TEST_1);
        subgraphs.add(SUBGRAPH_ID_TEST_2);
        ArrayList<RDFNode> currentResult = localService.querySubgraphsL(REPOSITORY_ID_TEST, subgraphs, QUERY_TEST);
        // Response is not empty
        assertNotNull(currentResult);
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }

    /**
     * Test of queryInferencedSubgraphsL method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the response is not empty.
     */
    @Test
    public void testQueryInferencedSubgraphsL() {
        System.out.println("queryInferencedSubgraphs");
        // Null arguments
        try {
            localService.queryInferencedSubgraphsL(null, new ArrayList<String>(), "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.queryInferencedSubgraphsL("", null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.queryInferencedSubgraphsL("", new ArrayList<String>(), null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
       // Load second ontlogy
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_2, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_2));        
        
        ArrayList<String> subgraphs = new ArrayList<>();
        subgraphs.add(SUBGRAPH_ID_TEST_1);
        subgraphs.add(SUBGRAPH_ID_TEST_2);
        ArrayList<RDFNode> currentResult = localService.queryInferencedSubgraphsL(REPOSITORY_ID_TEST, subgraphs, QUERY_TEST);
        // Response is not empty
        assertNotNull(currentResult);
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of queryAllSubgraphsL method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null.
     */
    @Test
    public void testQueryAllSubgraphsL() {
        System.out.println("queryAllSubgraphsL");
        // Null arguments
        try {
            localService.queryAllSubgraphsL(null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.queryAllSubgraphsL("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
    }
    
    /**
     * Test of queryAllInferencedSubgraphsL method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null.
     */
    @Test
    public void testQueryAllInferencedSubgraphsL() {
        System.out.println("queryAllInferencedSubgraphsL");
        // Null arguments
        try {
            localService.queryAllInferencedSubgraphsL(null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            localService.queryAllInferencedSubgraphsL("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
    }
    
    /**
     * Test of exportSubgraph method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the exported result is not empty.
     */
    @Test
    public void testExportSubgraph() {
        System.out.println("exportSubgraph");
        // Null arguments
        try {
            remoteService.exportSubgraph(null, "");
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.exportSubgraph("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
        String currentResult = null;
        currentResult = remoteService.exportSubgraph(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1);
        // Exported result is not empty
        assertNotNull(currentResult);
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    /**
     * Test of exportSubgraphs method, of class GateNosqlBean.
     * We test that the method returns IllegalArgumentException if any of arguments is null. Afterwards, we test
     * that the exported result is not empty.
     */
    @Test
    public void testExportSubgraphs() {
        System.out.println("exportSubgraphs");
        // Null arguments
        try {
            remoteService.exportSubgraphs(null, new ArrayList<String>());
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        try {
            remoteService.exportSubgraphs("", null);
            fail("IllegalArgumentException expected");
        } catch (Exception ex) {
            if (ex.getCause() instanceof IllegalArgumentException)
                ;
            // OK
        }
        
        // Erasure
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
        
        // Creation of repository "gate-test" succeeds
        assertTrue(remoteService.createRepository(REPOSITORY_ID_TEST));
        
        // Load first correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_1, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_1));
        
       // Load second correct ontology
        assertTrue(remoteService.loadOntologyFromFile(REPOSITORY_ID_TEST, SUBGRAPH_ID_TEST_2, BASE_IRI_TEST, FILE_PATH_ONTOLOGY_TEST_2));        
        
        ArrayList<String> subgraphs = new ArrayList<>();
        subgraphs.add(SUBGRAPH_ID_TEST_1);
        subgraphs.add(SUBGRAPH_ID_TEST_2);
        String currentResult = null;
        currentResult = remoteService.exportSubgraphs(REPOSITORY_ID_TEST, subgraphs);
        // Exported result is not empty
        assertNotNull(currentResult);
        
        // Clean database after test
        remoteService.deleteRepository(REPOSITORY_ID_TEST);
    }
    
    
    
    private ArrayList<String> returnDefaultIndexes() {
        // Default indexes are: i, gospi, gposi, gspoi, ospgi, posgi, spogi
        ArrayList<String> defaulIndices = new ArrayList<String>();
            defaulIndices.add("i");
            defaulIndices.add("gspoi");
            defaulIndices.add("gsopi");
            defaulIndices.add("gpsoi");
            defaulIndices.add("gposi");
            defaulIndices.add("gospi");
            defaulIndices.add("gopsi");
        return defaulIndices;
    }
    
}
