/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.sns;

import de.ingrid.iplug.IPlug;
import de.ingrid.iplug.PlugDescription;
import de.ingrid.iplug.sns.utils.Topic;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.IDataTypes;

/**
 */
public class SnsPlug implements IPlug {

    private static final String REQUEST_TYPE = "sns_request_type";

    private static final String TOPIC_FROM_TERM = "topicFromTerm";

    private static final String TOPIC_FROM_TEXT = "topicFromText";

    private static final String TOPIC_FROM_TOPIC = "topicFromTopic";

    private SNSController fSnsController;

    private int fMaximalAnalyzedWord;

    private String fPlugId;

    private String fUserName;

    private String fPassWord;

    private String fLanguage;

    /**
     * @throws Exception
     */
    public SnsPlug() throws Exception {
        this.fSnsController = new SNSController(new SNSClient(this.fUserName, this.fPassWord, this.fLanguage));
    }

    /**
     * @see de.ingrid.iplug.IPlug#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    public IngridHits search(IngridQuery query, int start, int length) {
        int count = 0;
        IngridHit[] hits = new IngridHit[0];

        if (query.getDataType().equals(IDataTypes.SNS)) {
            Topic[] topic = new Topic[0];
            String type = (String) query.get(REQUEST_TYPE);

            // FIXME: By TOPIC FROM TOPIC i get the range by the other i must select my range. Is this correct?
            try {
                if (type.equals(TOPIC_FROM_TERM)) {
                    topic = this.fSnsController.getTopicsForTerm(getSearchTerm(query), start, length);
                } else if (type.equals(TOPIC_FROM_TEXT)) {
                    topic = this.fSnsController
                            .getTopicsForText((String) query.getContent(), this.fMaximalAnalyzedWord);
                } else if (type.equals(TOPIC_FROM_TOPIC)) {
                    topic = this.fSnsController.getTopicsForTopic(getSearchTerm(query), length);
                }

                count = topic.length;
                int max = 0;
                final int countMinusStart = topic.length - start;
                if (countMinusStart >= 0) {
                    max = Math.min(length, countMinusStart);
                }

                hits = translateTopicToHit(topic, start, max);
            } catch (Exception e) {
                // TODO: log/react
            }
        }

        return new IngridHits(this.fPlugId, count, hits);
    }

    private IngridHit[] translateTopicToHit(Topic[] topic, int start, int max) {
        IngridHit[] hits = new IngridHit[max];
        for (int i = start; i < (max + start); i++) {
            // FIXME: What is the id?
            final int id = 1;
            // FIXME: What is the score?
            final float score = 1;
            // FIXME: What is the datasource ID?
            final int dataSourceId = 1;

            IngridHit ingridHit = new IngridHit(this.fPlugId, id, dataSourceId, score);
            hits[i - start] = ingridHit;
        }

        return hits;
    }

    private String getSearchTerm(IngridQuery query) {
        TermQuery[] terms = query.getTerms();
        if (terms.length > 1) {
            throw new IllegalArgumentException("only one term per query is allowed");
        }
        String searchTerm = terms[0].getTerm();
        return searchTerm;
    }

    public void configure(PlugDescription plugDescription) throws Exception {
        this.fPlugId = plugDescription.getPlugId();
        this.fUserName = (String) plugDescription.get("username");
        this.fPassWord = (String) plugDescription.get("password");
        this.fLanguage = (String) plugDescription.get("language");
        this.fMaximalAnalyzedWord = plugDescription.getInt("maxWordForAnalyzing");
    }

    public IngridHitDetail getDetails(IngridHit hit, IngridQuery query) throws Exception {
        return null;
    }
}
