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
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is a sample implementation of OmniSearchHandler that searches for Content Fragments.
 */
@Component
@Service
public final class ContentFragmentOmniSearchHandler implements OmniSearchHandler {

    private static final Logger log = LoggerFactory.getLogger(ContentFragmentOmniSearchHandler.class);

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public List<PredicateSuggestion> getPredicateSuggestions(ResourceResolver resourceResolver, I18n i18n, String term) {
        List<PredicateSuggestion> matchedPredicates = new ArrayList<PredicateSuggestion>();
        List<PredicateSuggestion> allPredicateSuggestions = getAllPredicateSuggestions(resourceResolver);
        for (PredicateSuggestion suggestion : allPredicateSuggestions) {
            if (i18n.getVar(suggestion.getOptionTitle()).toLowerCase().contains(term.toLowerCase())) {
                matchedPredicates.add(suggestion);
            }
        }

        return matchedPredicates;
    }

    private List<PredicateSuggestion> getAllPredicateSuggestions(ResourceResolver resourceResolver) {
        Resource configResource = getModuleConfig(resourceResolver);
        if (configResource != null) {
            String searchRailPath = configResource.getValueMap().get("searchRailPath", String.class);
            if (searchRailPath != null) {
                Resource searchRailResource = resourceResolver.getResource(searchRailPath);
                if (searchRailResource != null) {
                    String predicatePath = searchRailResource.getValueMap().get("predicatePath", String.class);
                    if (predicatePath != null) {
                        Resource predicatesResource = resourceResolver.getResource("/apps/settings/" + predicatePath + "/jcr:content/items");
                        if (predicatesResource != null) {
                            List<PredicateSuggestion> suggestions = new ArrayList<PredicateSuggestion>();
                            for (Resource child : predicatesResource.getChildren()) {
                                ValueMap props = child.getValueMap();
                                if (props.get("isSuggestable", false)) {
                                    String optionsPath = props.get("optionPaths", String.class);
                                    String type = props.get("text", String.class);
                                    if (optionsPath != null) {
                                        Resource optionsResource = resourceResolver.getResource("aem-omnisearch-content-fragments/search/predicates/contenttypes");
                                        if (optionsResource != null) {
                                            for (Resource option : optionsResource.getChildren()) {
                                                ValueMap optionsProps = option.getValueMap();
                                                String title = optionsProps.get("jcr:title", option.getName());

                                                PredicateSuggestion suggestion = new PredicateSuggestion(type, title, child.getPath(), option.getPath());
                                                Map<String, String> queryParams = new HashMap<String, String>();
                                                String baseName = props.get("listOrder", "1") + "_property";
                                                queryParams.put(baseName, props.get("name", String.class));
                                                queryParams.put(baseName + ".value", optionsProps.get("value", String.class));
                                                suggestion.setQueryParameters(queryParams);
                                                suggestions.add(suggestion);
                                            }
                                        }
                                    }
                                }
                            }
                            return suggestions;
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
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
        boolean addedPath = false;
        boolean addedType = false;
        for (Map.Entry<String, Object> param : predicateParameters.entrySet()) {
            if (param.getValue() instanceof String[]) {
                String[] values = (String[]) param.getValue();
                if (values.length == 1) {
                    if ((param.getKey().equals("path") || param.getKey().endsWith("_path"))
                        && values[0].length() > 0) {
                        addedPath = true;
                    }
                    if (param.getKey().equals("type") || param.getKey().endsWith("_type")) {
                        addedType = true;
                    }
                    predicates.put(param.getKey(), values[0]);
                }
            }
        }
        if (!addedPath) {
            predicates.put("path", DamConstants.MOUNTPOINT_ASSETS);
        }
        if (!addedType) {
            predicates.put("type", DamConstants.NT_DAM_ASSET);
        }
        predicates.put("999_property", "jcr:content/contentFragment");
        predicates.put("999_property.value", "true");

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
