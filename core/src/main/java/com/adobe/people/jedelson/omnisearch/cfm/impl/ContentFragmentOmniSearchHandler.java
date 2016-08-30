package com.adobe.people.jedelson.omnisearch.cfm.impl;

import com.adobe.granite.omnisearch.api.suggestion.PredicateSuggestion;
import com.adobe.granite.omnisearch.spi.core.OmniSearchHandler;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.i18n.I18n;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Service
public final class ContentFragmentOmniSearchHandler implements OmniSearchHandler {

    private static final Logger log = LoggerFactory.getLogger(ContentFragmentOmniSearchHandler.class);

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public List<PredicateSuggestion> getPredicateSuggestions(ResourceResolver resourceResolver, I18n i18n, String term) {
        return null;
    }

    @Override
    public Query getSpellCheckQuery(ResourceResolver resourceResolver, String term) {
        try {
            final String queryStr = "SELECT [rep:spellcheck()] FROM [dam:Asset] as s WHERE [jcr:path] = '/' AND SPELLCHECK($term)";
            final Query query = createQuery(resourceResolver, term, queryStr);
            return query;
        } catch (RepositoryException e) {
            log.error("Error while creating spell query", e);

        }
        return null;
    }

    @Override
    public Query getSuggestionQuery(ResourceResolver resourceResolver, String term) {
        // would be nice to have 'AND [jcr:content/contentFragment] = true' in this query, but that doesn't work currently
        String queryStr = "SELECT [rep:suggest()] FROM [dam:Asset] as s WHERE SUGGEST($term) AND ISDESCENDANTNODE([/content/dam])";
        try {
            Query query = createQuery(resourceResolver, term, queryStr);
            return query;
        } catch (RepositoryException e) {
            log.error("Unable to create suggestions query", e);
            return null;
        }
    }

    private Query createQuery(ResourceResolver resourceResolver, String term, String queryStr) throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(queryStr, Query.JCR_SQL2);
        ValueFactory vf = session.getValueFactory();
        query.bindValue("term", vf.createValue(term));
        return query;
    }

    @Override
    public Resource getModuleConfig(ResourceResolver resourceResolver) {
        return resourceResolver.getResource("/apps/aem-omnisearch-content-fragments/content/metadata");
    }

    @Override
    public SearchResult getResults(ResourceResolver resourceResolver, Map<String, Object> predicateParameters, long limit, long offset) {
        log.info("parameters {}", predicateParameters);

        Map<String, String> predicates = new HashMap<String, String>();
        predicates.put("path", DamConstants.MOUNTPOINT_ASSETS);
        predicates.put("type", DamConstants.NT_DAM_ASSET);
        predicates.put("property", "jcr:content/contentFragment");
        predicates.put("property.value", "true");

        if (predicateParameters.containsKey("fulltext")) {
            String[] ft = (String[]) predicateParameters.get("fulltext");
            predicates.put("fulltext", ft[0]);
        }

        PredicateGroup predicatesGroup = PredicateGroup.create(predicates);
        com.day.cq.search.Query query = queryBuilder.createQuery(predicatesGroup, resourceResolver.adaptTo(Session.class));
        if (limit != 0) {
            query.setHitsPerPage(limit);
        }
        if(offset != 0) {
            query.setStart(offset);
        }
        SearchResult queryResult = query.getResult();
        return queryResult;
    }

    @Override
    public String getID() {
        return "custom-cfm";
    }
}
