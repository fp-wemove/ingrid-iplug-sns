/*
 * Copyright (c) 2010 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.sns;

import junit.framework.TestCase;
import de.ingrid.external.sns.SNSClient;
import de.ingrid.iplug.sns.utils.DetailedTopic;
import de.ingrid.iplug.sns.utils.Topic;

/**
 * Tests of GSSoil implementations of Thesaurus/Gazetteer/FullClassify APi !!!
 */
public class GsSoilGazetteer1TestLocal extends TestCase {

    private static SNSClient fClient;

    private boolean fToStdout;

    private final static String VALID_TOPIC_ID = "http://www.eionet.europa.eu/gemet/concept/8167";

    public void setSNSClient(SNSClient client) throws Exception {
        fClient = client;
    }

    protected void setUp() throws Exception {
        super.setUp();

        fClient = new SNSClient("ms", "m3d1asyl3", "de");
        fClient.setTimeout(180000);

        this.fToStdout = true;
    }

    public void testGetDetails() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        Topic topic = new Topic();

        // GSSoil (LOCATION)
        // ---------------
        topic.setTopicID("Berlin");

        DetailedTopic dt = controller.getTopicDetail(topic, "/location", "de");
        assertNotNull(dt);
        assertTrue(dt.getTopicID().indexOf("Berlin") != -1);
        assertTrue(dt.getTitle().indexOf("Berlin") != -1);

        // ALWAYS empty definitions cause using GazetterService API
        String[] array = dt.getDefinitions();
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
        String bla = (String) dt.get(DetailedTopic.ASSOCIATED_OCC);
        assertNull(bla);

        // NO descriptionOcc cause using GazetterService API
        bla = (String) dt.get(DetailedTopic.DESCRIPTION_OCC);
        assertNull(bla);
    }

    public void testGetAssociatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];        
        Topic[] topics = null;
        
        // LOCATION
		String locationId = "Berlin";
        topics = controller.getTopicsForTopic(locationId, 100, "/location", "aId", "de", totalSize, false);
        assertNotNull(topics);
        assertTrue(topics.length > 0);

        // LOCATION
        topics = controller.getTopicSimilarLocationsFromTopic(locationId, 100, "aId", totalSize, "de");
        assertTrue(topics.length > 0);
    }

    public void testGetDocumentRelatedTopics() throws Exception {
        SNSController controller = new SNSController(fClient, "ags:");
        int[] totalSize = new int[1];
        String text = "soil water sun berlin";
        DetailedTopic[] topics = controller.getTopicsForText(text, 100, "aPlugId", "en", totalSize, false);
        assertNotNull(topics);
//        assertEquals(0, topics.length);
//        assertEquals(2, topics.length);

        text = "yyy xxx zzz";
        topics = controller.getTopicsForText(text, 100, "aPlugId", "de", totalSize, false);
        assertNotNull(topics);
        assertEquals(0, topics.length);

        // valid URL
        String url = "http://www.portalu.de";
        int maxWords = 200;
        topics = controller.getTopicsForURL(url, maxWords, null, "aPlugId", "de", totalSize);
        assertNotNull(topics);
        int numAllTopics = topics.length;
		assertTrue(numAllTopics > 0);

		// only locations
        topics = controller.getTopicsForURL(url, maxWords, "/location", "aPlugId", "de", totalSize);
        assertNotNull(topics);
		assertTrue(topics.length > 0);
		assertTrue(topics.length < numAllTopics);
    }

    public void testGetTopicFromText() throws Exception {
    	// test terms
        SNSController controller = new SNSController(fClient, "ags:");
        String text = "Berlin";
        int[] totalSize = new int[1];
        DetailedTopic[] topics;

    	// test locations, "de"
        topics = controller.getTopicsForText(text, 100, "/location", "aPlugId", "de", totalSize, false);
        assertTrue(topics.length > 0);

    	// test locations, "en"
        topics = controller.getTopicsForText(text, 100, "/location", "aPlugId", "en", totalSize, false);
        // no english ?
        assertTrue(topics.length == 0);

    	// test ALL TOPICS
        topics = controller.getTopicsForText(text, 100, null, "aPlugId", "de", totalSize, false);
        assertTrue(topics.length > 0);
    }
}