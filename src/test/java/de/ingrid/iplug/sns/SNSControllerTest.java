/*
 * Copyright (c) 2003 by media style GmbH
 * 
 * $Source: /cvs/SiemensPI/ms_codetemplates.xml,v $
 */

package de.ingrid.iplug.sns;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;

/**
 * SNSControllerTest
 * 
 * <p/>created on 29.09.2005
 * 
 * @version $Revision: $
 * @author sg
 * @author $Author: ${lastedit}
 * 
 */
public class SNSControllerTest extends TestCase {

    private static SNSClient fClient;

    private boolean fToStdout;

    private final static String VALID_TOPIC_ID = "http://umthes.innoq.com/_00019054";

    /**
     * @param client
     * @throws Exception
     */
    public void setSNSClient(SNSClient client) throws Exception {
        fClient = client;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        fClient = new SNSClient("ms", "m3d1asyl3", "de");
        fClient.setTimeout(180000);

        this.fToStdout = true;
    }

    /**
     * @throws Exception
     */
    public void testTopicsForTerm() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        // NOTICE: "Wasser" is LABEL topic !!!
        Topic[] topicsForTerm = controller.getTopicsForTerm("Wasser", 0, 1000, "aId", totalSize, "de", false, false);
        assertTrue(topicsForTerm.length == 7);
        for (int i = 0; i < topicsForTerm.length; i++) {
            Topic topic = topicsForTerm[i];
            if (this.fToStdout) {
                System.out.println(topic);
            }
        }

        // DESCRIPTOR topic !
        topicsForTerm = controller.getTopicsForTerm("Hydrosph\u00E4re", 0, 1000, "aId", totalSize, "de", false, false);
        assertTrue(topicsForTerm.length == 5);

        // case insensitive !!!
        topicsForTerm = controller.getTopicsForTerm("hydrosph\u00E4re", 0, 1000, "aId", totalSize, "de", false, false);
        assertTrue(topicsForTerm.length == 5);

        // NON DESCRIPTOR topic ! Here we do NOT get results !!!
        topicsForTerm = controller.getTopicsForTerm("Waldsterben", 0, 1000, "aId", totalSize, "de", false, false);
        assertTrue(topicsForTerm.length == 0);

        // TOP topic !!!
        topicsForTerm = controller.getTopicsForTerm("[Hydrosphäre - Wasser und Gewässer]", 0, 1000, "aId", totalSize, "de", false, false);
        assertTrue(topicsForTerm.length == 5);

        topicsForTerm = controller.getTopicsForTerm("no thesa topic available", 0, 1000, "aId", totalSize, "de", false, false);
        assertTrue(topicsForTerm.length == 0);
    }

    /**
     * @throws Exception
     */
    public void testGetAssociatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        
        // THESA
        Topic[] topics = controller.getTopicsForTopic("Wasser", 23, "/thesa", "aId", "de", totalSize, false);
        assertNull(topics);
        topics = controller.getTopicsForTopic(VALID_TOPIC_ID, 23, "/thesa", "aId", "de", totalSize, false);
        assertEquals(7, topics.length);
        for (int i = 0; i < topics.length; i++) {
            Topic topic = topics[i];
            if (this.fToStdout) {
                System.out.println(topic);
            }
        }

        // LOCATION
		String locationId = "http://iqvoc-gazetteer.innoq.com/GEMEINDE0641200000"; // Frankfurt am Main
        topics = controller.getTopicsForTopic(locationId, 23, "/location", "aId", "de", totalSize, false);
        assertEquals(23, topics.length);

        // LOCATION
        topics = controller.getTopicSimilarLocationsFromTopic(locationId, 23, "aId", totalSize, "de");
        assertEquals(23, topics.length);
    }

    /**
     * @throws Exception
     */
    public void testGetDocumentRelatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        String text = "Tschernobyl liegt in Halle gefunden";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertTrue(topics.length > 0);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertEquals(0, topics.length);

        // valid URL
//        String url = "http://www.portalu.de";
        String url = "http://www.rmv.de";
        int maxWords = 200;
        topics = controller.getTopicsForURL(url, maxWords, null, "aPlugId", "de", totalSize);
        assertNotNull(topics);
        int numAllTopics = topics.length;
		assertTrue(numAllTopics > 0);

		// only thesa
        topics = controller.getTopicsForURL(url, maxWords, "/thesa", "aPlugId", "de", totalSize);
        assertNotNull(topics);
		assertTrue(topics.length > 0);
		assertTrue(topics.length < numAllTopics);

		// only locations
        topics = controller.getTopicsForURL(url, maxWords, "/location", "aPlugId", "de", totalSize);
        assertNotNull(topics);
		assertTrue(topics.length > 0);
		assertTrue(topics.length < numAllTopics);

		// only events
        topics = controller.getTopicsForURL(url, maxWords, "/event", "aPlugId", "de", totalSize);
        assertNotNull(topics);
        // May fail due to wrong content on Site ?!!!!????
		assertTrue(topics.length > 0);
		assertTrue(topics.length < numAllTopics);

		// INVALID URL
        url = "http://www.partalu.de";
        try {
            topics = controller.getTopicsForURL(url, maxWords, "/event", "aPlugId", "de", totalSize);        	
        } catch (Exception ex) {
            System.out.println("EXPECTED exception" + ex);
        }
        url = "htp://www.portalu .de";
        try {
            topics = controller.getTopicsForURL(url, maxWords, "/event", "aPlugId", "de", totalSize);
        } catch (Exception ex) {
            System.out.println("EXPECTED exception" + ex);
        }
    }

    /**
     * @throws Exception
     */
    public void testTopicForId() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        // #legalType (EVENT)
        // ---------------
        DetailedTopic[] topicsForId = controller.getTopicForId("http://iqvoc-chronicle.innoq.com/t47098a_10220d1bc3e_4ee1", "/event", "plugId", "de", totalSize);
        assertTrue(topicsForId.length == 1);
        DetailedTopic dt = topicsForId[0];

        assertNotNull(dt);
        String[] array = dt.getDefinitions();
        /*assertEquals(1, array.length);
        System.out.println("Defs:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getDefinitionTitles();
        assertEquals(1, array.length);
        System.out.println("DefTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }*/

        /*array = dt.getSamples();
        assertEquals(2, array.length);
        System.out.println("Sam:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getSampleTitles();
        assertEquals(2, array.length);
        System.out.println("SamTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        System.out.println("Ass:");
        String bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        System.out.println(bla);
		*/
        System.out.println("Des:");
        String bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        System.out.println(bla);

        // #descriptorType (THESA) Waldschaden
        // ---------------
        topicsForId = controller.getTopicForId("http://umthes.innoq.com/_00027061", "/thesa", "plugId", "de", totalSize);
        assertTrue(topicsForId.length == 1);
        dt = topicsForId[0];

        assertNotNull(dt);
        assertEquals("http://umthes.innoq.com/_00027061", dt.getTopicID());
        assertEquals("Waldschaden", dt.getTitle());

        // ALWAYS empty definitions cause using ThesaurusService API
        array = dt.getDefinitions();
        assertEquals(0, array.length);

        // ALWAYS empty definitionTitles cause using ThesaurusService API
        array = dt.getDefinitionTitles();
        assertEquals(0, array.length);

        // ALWAYS empty samples cause using ThesaurusService API
        array = dt.getSamples();
        assertEquals(0, array.length);

        // ALWAYS empty sampleTitles cause using ThesaurusService API
        array = dt.getSampleTitles();
        assertEquals(0, array.length);

        // NO associations cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);

        // #use6Type (LOCATION) Frankfurt am Main
        // ---------------
        topicsForId = controller.getTopicForId("http://iqvoc-gazetteer.innoq.com/GEMEINDE0641200000", "/location", "plugId", "de", totalSize);
        assertTrue(topicsForId.length == 1);
        dt = topicsForId[0];

        assertNotNull(dt);
        assertEquals("http://iqvoc-gazetteer.innoq.com/GEMEINDE0641200000", dt.getTopicID());
        assertEquals("Frankfurt am Main", dt.getTitle());
        assertTrue(dt.getTopicNativeKey().indexOf("06412000") != -1);
        assertEquals("http://iqvoc-gazetteer.innoq.com/GEMEINDE0641200000", dt.getAdministrativeID());
        assertTrue(dt.getSummary().indexOf("Gemeinde") != -1);

        // ALWAYS empty definitions cause using GazetterService API
        array = dt.getDefinitions();
        assertEquals(0, array.length);

        // ALWAYS empty definitionTitles cause using GazetterService API
        array = dt.getDefinitionTitles();
        assertEquals(0, array.length);

        /*
        // ALWAYS empty samples cause using GazetterService API
        array = dt.getSamples();
        assertEquals(0, array.length);

        // ALWAYS empty sampleTitles cause using GazetterService API
        array = dt.getSampleTitles();
        assertEquals(0, array.length);

        // NO associations cause using GazetterService API
        bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);*/

        // NO descriptionOcc cause using GazetterService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);
    }

    /**
     * @throws Exception
     */
    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        Topic topic = new Topic();
        // #legalType (EVENT)
        // ---------------
        topic.setTopicID("http://iqvoc-chronicle.innoq.com/t47098a_10220d1bc3e_4ee1");

        DetailedTopic dt = controller.getTopicDetail(topic, "de");

        String[] array = dt.getDefinitions();
        /*assertEquals(1, array.length);
        System.out.println("Defs:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getDefinitionTitles();
        assertEquals(1, array.length);
        System.out.println("DefTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }*/

        array = dt.getSamples();
        assertEquals(3, array.length);
        System.out.println("Sam:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        array = dt.getSampleTitles();
        assertEquals(3, array.length);
        System.out.println("SamTit:");
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }

        System.out.println("Ass:");
        String bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        System.out.println(bla);

        System.out.println("Des:");
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        System.out.println(bla);

        // #descriptorType (THESA) Waldschaden
        // ---------------
        topic.setTopicID("http://umthes.innoq.com/_00027061");

        dt = controller.getTopicDetail(topic, "/thesa", "de");
        assertNotNull(dt);
        assertEquals("http://umthes.innoq.com/_00027061", dt.getTopicID());
        assertEquals("Waldschaden", dt.getTitle());

        // ALWAYS empty definitions cause using ThesaurusService API
        array = dt.getDefinitions();
        assertEquals(0, array.length);

        // ALWAYS empty definitionTitles cause using ThesaurusService API
        array = dt.getDefinitionTitles();
        assertEquals(0, array.length);

        // ALWAYS empty samples cause using ThesaurusService API
        array = dt.getSamples();
        assertEquals(0, array.length);

        // ALWAYS empty sampleTitles cause using ThesaurusService API
        array = dt.getSampleTitles();
        assertEquals(0, array.length);

        // NO associations cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using ThesaurusService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);

        // #use6Type (LOCATION) Frankfurt am Main
        // ---------------
        topic.setTopicID("http://iqvoc-gazetteer.innoq.com/GEMEINDE0641200000");

        dt = controller.getTopicDetail(topic, "/location", "de");
        assertNotNull(dt);
        assertEquals("http://iqvoc-gazetteer.innoq.com/GEMEINDE0641200000", dt.getTopicID());
        assertEquals("Frankfurt am Main", dt.getTitle());
        assertTrue(dt.getTopicNativeKey().indexOf("06412000") != -1);
        assertEquals("http://iqvoc-gazetteer.innoq.com/GEMEINDE0641200000", dt.getAdministrativeID());
        //assertTrue(dt.getSummary().indexOf("location-admin-use6") != -1);
        assertTrue(dt.getSummary().indexOf("Gemeinde") != -1);

        // ALWAYS empty definitions cause using GazetterService API
        array = dt.getDefinitions();
        assertEquals(0, array.length);

        // ALWAYS empty definitionTitles cause using GazetterService API
        array = dt.getDefinitionTitles();
        assertEquals(0, array.length);

        // ALWAYS empty samples cause using GazetterService API
        array = dt.getSamples();
        assertEquals(0, array.length);

        // ALWAYS empty sampleTitles cause using GazetterService API
        array = dt.getSampleTitles();
        assertEquals(0, array.length);

        // NO associations cause using GazetterService API
        bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using GazetterService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);
    }

    /**
     * @throws Exception
     */
    public void testGetAssociatedTopicsExpired() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        // WITH INTRODUCTION OF GAZETTEER API NEVER RETURNS EXPIRED ONES !!!
        Topic[] topicsForTopic = controller.getTopicSimilarLocationsFromTopic("http://iqvoc-gazetteer.innoq.com/GEMEINDE0325300005", 1000, "aId",
                totalSize, "de");
//                totalSize, false, "de");
        assertNotNull(topicsForTopic);
        assertEquals(1, topicsForTopic.length);

        // WITH INTRODUCTION OF GAZETTEER API NEVER RETURNS EXPIRED ONES !!!
        /*topicsForTopic = controller.getTopicSimilarLocationsFromTopic("http://iqvoc-gazetteer.innoq.com/GEMEINDE0325300005", 1000, "aId", totalSize,
                "de");
//                true, "de");
        assertNotNull(topicsForTopic);
        */
    }

    /**
     * @throws Exception
     */
    public void testGetHierachy() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");

        // toplevel
        String topicID = "toplevel";
        int[] totalSize = new int[1];
        Topic[] topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 1, "down", false, "de",
                topicID, false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        System.out.println(topicsHierachy[0].getTopicID());
        // printHierachy(topicsHierachy[0].getSuccessors(), 1);

        // up
        topicID = "http://umthes.innoq.com/_00040282";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 5, "up", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        List<String> resultList = new ArrayList<String>();
        resultList.add(topicsHierachy[0].getTopicID());
        resultList.add(topicsHierachy[0].getTopicName());
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("[Atmosph\u00E4re und Klima]"));
        assertTrue(resultList.contains("Luft"));
        assertTrue(resultList.contains("http://umthes.innoq.com/_00049251"));
        assertTrue(resultList.contains("http://umthes.innoq.com/_00040282"));

        // top node up
        topicID = "http://umthes.innoq.com/_00049251";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 5, "up", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        // return value is null !!!?
        assertNull(topicsHierachy[0]);

        // down
        topicID = "http://umthes.innoq.com/_00049251";
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 2, "down", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        resultList = new ArrayList<String>();
        resultList.add(topicsHierachy[0].getTopicID());
        resultList.add(topicsHierachy[0].getTopicName());
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("[Atmosph\u00E4re und Klima]"));
        assertTrue(resultList.contains("Luft"));
        assertTrue(resultList.contains("http://umthes.innoq.com/_00049251"));
        assertTrue(resultList.contains("http://umthes.innoq.com/_00040282"));

        // leaf down
        topicID = "http://umthes.innoq.com/de/concepts/_00040787"; // Kleinmenge
        topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 2, "down", false, "de", topicID,
                false, "pid");
        assertNotNull(topicsHierachy);
        assertEquals(1, topicsHierachy.length);
        // return value is null !!!?
        assertNull(topicsHierachy[0]);
    }

    public void testGetHierachyIncludeSiblings() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");

		// PATH OF SUB TERM in german
		// NOTICE: has 2 paths to top !
		// 1. uba_thes_13093 / uba_thes_47403 / uba_thes_47404 / uba_thes_49276
		// 2. uba_thes_13093 / uba_thes_13133 / uba_thes_49268
		String topicID = "http://umthes.innoq.com/_00013093"; // Immissionsdaten
//        String topicID = "uba_thes_27118";
        int[] totalSize = new int[1];
        Topic[] topicsHierachy = controller.getTopicHierachy(totalSize, "narrowerTermAssoc", 200, "up", true, "de",
                topicID, false, "pid");
        assertNotNull(topicsHierachy);
        // NOT VALID ANYMORE ! NEVER ADD SIBLINGS ! 
//        assertEquals(83, topicsHierachy.length);
        assertEquals(1, topicsHierachy.length);
        assertEquals(3, topicsHierachy[0].getSuccessors().size());
        List<String> resultList = new ArrayList<String>();
        fill(topicsHierachy[0].getSuccessors(), resultList);

        assertTrue(resultList.contains("Messergebnis [benutze Unterbegriffe]"));
        assertTrue(resultList.contains("Immissionssituation"));
        assertTrue(resultList.contains("http://umthes.innoq.com/_00047403"));
        assertTrue(resultList.contains("http://umthes.innoq.com/_00013133"));
    }

    /**
     * @throws Exception
     */
    public void testGetSimilarTerms() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        int[] totalSize = new int[1];
        Topic[] topicsForTopic = controller.getSimilarTermsFromTopic("Abfall", 200, "pid", totalSize, "de");
        assertNotNull(topicsForTopic);
        assertEquals(4, topicsForTopic.length);
        // for (int i = 0; i < topicsForTopic.length; i++) {
        // System.out.println(topicsForTopic[i].getTopicID());
        // }
    }

    /**
     * @throws Exception
     */
    public void testGetTopicFromText() throws Exception {
    	// test terms
        SNSController controller = new SNSController(fClient, "agsNotation");
        String text = "Waldsterben Weser Explosion";
        int[] totalSize = new int[1];
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "/thesa", "aPlugId", "de", totalSize, false);        
        assertEquals(2, totalSize[0]);
        assertNotNull(topics);
        assertEquals(2, topics.length);
        assertEquals("Explosion", topics[0].getTitle());
        assertEquals("Waldschaden", topics[1].getTitle());

    	// test locations
        topics = controller.getTopicsForText(text, 100, "/location", "aPlugId", "de", totalSize, false);
        assertEquals(2, totalSize[0]);
        assertNotNull(topics);
        assertEquals(2, topics.length);
        assertEquals("http://iqvoc-gazetteer.innoq.com/FLUSS4", topics[0].getTopicNativeKey());
        assertEquals("http://iqvoc-gazetteer.innoq.com/WASSEREINZUGSGEBIET496", topics[1].getTopicNativeKey());

        // BUG: https://github.com/innoq/iqvoc_gazetteer/issues/13
        topics = controller.getTopicsForText("Frankfurt", 100, "/location", "aPlugId", "de", new int[1], false);
        assertNotNull(topics);
        assertEquals(4, topics.length);
        assertEquals("06412000", topics[0].getTopicNativeKey());
        assertEquals("12053000", topics[1].getTopicNativeKey());

    	// test events
        // -> NOT SUPPORTED WITH INNOQ-SNS
        // topics = controller.getTopicsForText(text, 100, "/event", "aPlugId", "de", totalSize, false);
        // assertTrue(totalSize[0] > 0);
        // assertNotNull(topics);
        // assertTrue(topics.length > 0);
/*
        assertEquals("Chemieexplosion in Toulouse", topics[0].getTitle());
        assertEquals("Explosion im Stickstoffwerk Oppau", topics[1].getTitle());
        assertEquals("Kyschtym-Unfall von Majak", topics[2].getTitle());
*/
    	// test ALL TOPICS
        topics = controller.getTopicsForText(text, 100, null, "aPlugId", "de", totalSize, false);
        assertTrue(totalSize[0] >= 4);
        assertNotNull(topics);
        assertTrue(topics.length >= 4);
    }


    /**
     * @throws Exception
     */
    public void testGetTopicFromTextNoNativeKey() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        String text = "Weser";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "de", new int[1], false);
        assertNotNull(topics);
        assertEquals(2, topics.length);
        assertEquals("http://iqvoc-gazetteer.innoq.com/FLUSS4", topics[0].getTopicNativeKey());
        assertEquals("http://iqvoc-gazetteer.innoq.com/WASSEREINZUGSGEBIET496", topics[1].getTopicNativeKey());
    }

    /**
     * @throws Exception
     */
    public void testGetSimilarLocationsFromTopicNativeKeyHasLawaPrefix() throws Exception {
        SNSController controller = new SNSController(fClient, "agsNotation");
        String topicId = "http://iqvoc-gazetteer.innoq.com/NATURRAUM583";
        Topic[] topics = controller.getTopicSimilarLocationsFromTopic(topicId, 100, "aPlugId", new int[1], "de");
        assertNotNull(topics);
        assertEquals(82, topics.length);
        for (int i = 0; i < topics.length; i++) {
            assertTrue("Does contain 'lawa:'.", !topics[i].getTopicNativeKey().startsWith("lawa:"));
        }
    }

    private void printHierachy(Set successors, int tab) {
		for (Object object : successors) {
			for (int j = 0; j < tab; j++) {
				System.out.print(' ');
			}
			Topic topic = (Topic) object;
			System.out.println(topic.getTopicID());
			printHierachy(topic.getSuccessors(), tab + 1);
		}
	}

    private void fill(Set<Topic> topicsHierachy, List<String> resultList) {
    	for (Topic topic : topicsHierachy) {
			resultList.add(topic.getTopicID());
			resultList.add(topic.getTopicName());
			fill(topic.getSuccessors(), resultList);
		}
    }
}
