/*******************************************************************************
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *******************************************************************************/
package fr.gouv.vitam.logbook.common.server.database.collections;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;

import fr.gouv.vitam.common.database.builder.request.configuration.GlobalDatas;
import fr.gouv.vitam.common.database.server.elasticsearch.ElasticsearchAccess;
import fr.gouv.vitam.common.database.server.elasticsearch.ElasticsearchNode;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.logbook.common.server.exception.LogbookException;

/**
 * ElasticSearch model with MongoDB as main database with management of index and index entries
 */
public class LogbookElasticsearchAccess extends ElasticsearchAccess {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(LogbookElasticsearchAccess.class);

    /**
     * @param clusterName
     * @param nodes
     * @throws VitamException
     */
    public LogbookElasticsearchAccess(final String clusterName, List<ElasticsearchNode> nodes) throws VitamException {
        super(clusterName, nodes);
    }

    /**
     * Delete an index
     *
     * @param collection collection of index
     * @return True if deleted
     */
    public final boolean deleteIndex(final LogbookCollections collection) {
        LOGGER.debug("deleteIndex: " + collection.getName().toLowerCase());
        try {
            if (client.admin().indices().prepareExists(collection.getName().toLowerCase()).get().isExists()) {
                if (!client.admin().indices().prepareDelete(collection.getName().toLowerCase()).get()
                    .isAcknowledged()) {
                    LOGGER.error("Error on index delete");
                    return false;
                }
            }
            return true;
        } catch (final Exception e) {
            LOGGER.error("Error while deleting index", e);
            return false;
        }
    }

    /**
     * Add a type to an index
     *
     * @param collection collection of index
     * @return True if added
     */
    public final boolean addIndex(final LogbookCollections collection) {
        LOGGER.debug("addIndex: " + collection.getName().toLowerCase());
        if (!client.admin().indices().prepareExists(collection.getName().toLowerCase()).get().isExists()) {
            try {
                LOGGER.debug("createIndex");
                final String mapping = LogbookOperation.MAPPING;
                final String type = LogbookOperation.TYPEUNIQUE;
                LOGGER.debug("setMapping: " + collection.getName().toLowerCase() + " type: " + type + "\n\t" + mapping);
                final CreateIndexResponse response =
                    client.admin().indices().prepareCreate(collection.getName().toLowerCase())
                        .setSettings(Settings.builder().loadFromSource(DEFAULT_INDEX_CONFIGURATION))
                        .addMapping(type, mapping)
                        .get();
                if (!response.isAcknowledged()) {
                    LOGGER.error(type + ":" + response.isAcknowledged());
                    return false;
                }
            } catch (final Exception e) {
                LOGGER.error("Error while set Mapping", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Refresh an index
     *
     * @param collection collection of index
     */
    public final void refreshIndex(final LogbookCollections collection) {
        LOGGER.debug("refreshIndex: " + collection.getName().toLowerCase());
        client.admin().indices().prepareRefresh(collection.getName().toLowerCase())
            .execute().actionGet();

    }

    /**
     * Add a set of entries in the ElasticSearch index. <br>
     * Used in reload from scratch.
     *
     * @param collection collection of index
     * @param mapIdJson map of documents as json by id
     * @return the listener on bulk insert
     */
    final BulkResponse addEntryIndexes(final LogbookCollections collection,
        final Map<String, String> mapIdJson) {
        final BulkRequestBuilder bulkRequest = client.prepareBulk();

        // either use client#prepare, or use Requests# to directly build index/delete requests
        final String type = getTypeUnique(collection);
        for (final Entry<String, String> val : mapIdJson.entrySet()) {
            bulkRequest.setRefresh(true).add(client.prepareIndex(collection.getName().toLowerCase(), type,
                val.getKey()).setSource(val.getValue()));
        }
        return bulkRequest.execute().actionGet();
    }

    /**
     * Update an entry in the ElasticSearch index
     *
     * @param collection collection of index
     * @param id the id of the entry
     * @param json the entry document as a json
     * @return True if updated
     */
    final boolean updateEntryIndex(final LogbookCollections collection,
        final String id, final String json) {
        final String type = LogbookOperation.TYPEUNIQUE;
        return client.prepareUpdate(collection.getName().toLowerCase(), type, id)
            .setDoc(json).setRefresh(true).execute()
            .actionGet().getVersion() > 1;
    }

    /**
     * FIXME : adapt in #1598 pour real search
     * 
     * @param collection
     * @param query as in DSL mode "{ "fieldname" : "value" }" "{ "match" : { "fieldname" : "value" } }" "{ "ids" : { "
     *        values" : [list of id] } }"
     * @param filter the filter
     * @return a structure as SearchResponse
     * @throws LogbookException thrown of an error occured while executing the request
     */
    public final SearchResponse search(final LogbookCollections collection, final QueryBuilder query,
        final QueryBuilder filter) throws LogbookException {
        final String type = getTypeUnique(collection);
        final SearchRequestBuilder request =
            client.prepareSearch(collection.getName().toLowerCase()).setSearchType(SearchType.DEFAULT)
                .setTypes(type).setExplain(false).setSize(GlobalDatas.LIMIT_LOAD);
        if (filter != null) {
            request.setQuery(query).setPostFilter(filter);
        } else {
            request.setQuery(query);
        }
        try {
            return request.get();
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
            throw new LogbookException(e);
        }
    }


    private String getTypeUnique(LogbookCollections collection) {
        switch (collection) {
            case OPERATION:
                return LogbookOperation.TYPEUNIQUE;
            case LIFECYCLE_UNIT:
            case LIFECYCLE_UNIT_IN_PROCESS:
            case LIFECYCLE_OBJECTGROUP:
            case LIFECYCLE_OBJECTGROUP_IN_PROCESS:
            default:
                return "";
        }
    }
}
