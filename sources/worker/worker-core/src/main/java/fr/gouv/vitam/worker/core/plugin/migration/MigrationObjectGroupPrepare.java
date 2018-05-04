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
package fr.gouv.vitam.worker.core.plugin.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.parser.request.multiple.SelectParserMultiple;
import fr.gouv.vitam.common.database.utils.ScrollSpliterator;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamDBException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.IngestWorkflowConstants;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.metadata.api.exception.MetaDataClientServerException;
import fr.gouv.vitam.metadata.api.exception.MetaDataDocumentSizeException;
import fr.gouv.vitam.metadata.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.metadata.client.MetaDataClient;
import fr.gouv.vitam.metadata.client.MetaDataClientFactory;
import fr.gouv.vitam.metadata.core.database.configuration.GlobalDatasDb;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.parameter.WorkerParameters;
import fr.gouv.vitam.worker.common.HandlerIO;
import fr.gouv.vitam.worker.core.handler.ActionHandler;

import java.io.File;
import java.util.stream.StreamSupport;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.exists;
import static fr.gouv.vitam.common.json.JsonHandler.createObjectNode;
import static fr.gouv.vitam.worker.core.plugin.migration.MigrationHelper.checkNumberOfResultQuery;
import static fr.gouv.vitam.worker.core.plugin.migration.MigrationHelper.getSelectMultiQuery;

/**
 * MigrationUnitPrepare class
 */
public class MigrationObjectGroupPrepare extends ActionHandler {
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(MigrationObjectGroupPrepare.class);

    private static String MIGRATION_OBJECT_GROUPS_LIST = "MIGRATION_OBJECT_GROUPS_LIST";
    private MetaDataClientFactory metaDataClientFactory;

    @VisibleForTesting
    public MigrationObjectGroupPrepare(MetaDataClientFactory metaDataClientFactory) {
        this.metaDataClientFactory = metaDataClientFactory;
    }

    public MigrationObjectGroupPrepare() {
        metaDataClientFactory = MetaDataClientFactory.getInstance();
    }

    @Override public ItemStatus execute(WorkerParameters param, HandlerIO handler) throws ProcessingException {
        ItemStatus itemStatus = new ItemStatus(MIGRATION_OBJECT_GROUPS_LIST);

        try (MetaDataClient client = metaDataClientFactory.getClient()) {

            SelectMultiQuery selectMultiQuery = getSelectMultiQuery(handler);

            ScrollSpliterator<JsonNode> scrollRequest = new ScrollSpliterator<>(selectMultiQuery,
                query -> {
                    try {
                        JsonNode jsonNode = client.selectObjectGroups(query.getFinalSelect());
                        return RequestResponseOK.getFromJsonNode(jsonNode);
                    } catch (MetaDataExecutionException | MetaDataDocumentSizeException | MetaDataClientServerException | InvalidParseOperationException e) {
                        throw new IllegalStateException(e);
                    }
                }, GlobalDatasDb.DEFAULT_SCROLL_TIMEOUT, GlobalDatasDb.LIMIT_LOAD);
            StreamSupport.stream(scrollRequest, false).forEach(
                objectGroup -> {
                    File file = null;

                    try {
                        String identifier = objectGroup.get("#id").asText();
                        file = handler.getNewLocalFile(identifier);
                        JsonHandler.writeAsFile(objectGroup, file);
                        handler.transferFileToWorkspace(IngestWorkflowConstants.OBJECT_GROUP_FOLDER + "/" + identifier, file, true, false);

                    } catch (InvalidParseOperationException | ProcessingException e) {
                        LOGGER.error(e);
                        throw new IllegalStateException(e);
                    }

                });

            if (checkNumberOfResultQuery(itemStatus, scrollRequest.estimateSize())) {
                return new ItemStatus(MIGRATION_OBJECT_GROUPS_LIST)
                    .setItemsStatus(MIGRATION_OBJECT_GROUPS_LIST, itemStatus);
            }
        } catch (InvalidParseOperationException | InvalidCreateOperationException e) {
            LOGGER.error(e);
            return itemStatus.increment(StatusCode.FATAL);
        }

        itemStatus.increment(StatusCode.OK);
        return new ItemStatus(MIGRATION_OBJECT_GROUPS_LIST).setItemsStatus(MIGRATION_OBJECT_GROUPS_LIST, itemStatus);
    }



    @Override public void checkMandatoryIOParameter(HandlerIO handler) throws ProcessingException {
        //nothing
    }
}
