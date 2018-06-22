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
package fr.gouv.vitam.worker.core.plugin.reclassification;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.guid.GUID;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.i18n.VitamLogbookMessages;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.parameter.ParameterHelper;
import fr.gouv.vitam.logbook.common.parameters.LogbookLifeCycleUnitParameters;
import fr.gouv.vitam.logbook.common.parameters.LogbookParameterName;
import fr.gouv.vitam.logbook.common.parameters.LogbookTypeProcess;

import static fr.gouv.vitam.logbook.common.parameters.LogbookParametersFactory.newLogbookLifeCycleUnitParameters;

/**
 * Basic helper methods for reclassification plugins
 */
class ReclassificationPluginHelper {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ReclassificationPluginHelper.class);

    public static ItemStatus buildItemStatus(String action, StatusCode statusCode,
        ReclassificationEventDetails eventDetails) {
        ItemStatus itemStatus = new ItemStatus();
        itemStatus.increment(statusCode);
        if (eventDetails != null) {
            try {
                itemStatus.setEvDetailData(JsonHandler.unprettyPrint(JsonHandler.toJsonNode(eventDetails)));
            } catch (InvalidParseOperationException e1) {
                LOGGER.error("Could not serialize event details" + eventDetails);
            }
        }
        return new ItemStatus(action).setItemsStatus(action, itemStatus);
    }

    public static LogbookLifeCycleUnitParameters createParameters(GUID eventIdentifierProcess,
        StatusCode logbookOutcome, GUID objectIdentifier, String action, ReclassificationEventDetails eventDetails) {

        final LogbookTypeProcess eventTypeProcess = LogbookTypeProcess.RECLASSIFICATION;
        final GUID updateGuid = GUIDFactory.newEventGUID(ParameterHelper.getTenantParameter());
        LogbookLifeCycleUnitParameters parameters = newLogbookLifeCycleUnitParameters(updateGuid,
            VitamLogbookMessages.getEventTypeLfc(action),
            eventIdentifierProcess,
            eventTypeProcess, logbookOutcome,
            VitamLogbookMessages.getOutcomeDetailLfc(action, logbookOutcome),
            VitamLogbookMessages.getCodeLfc(action, logbookOutcome), objectIdentifier);

        if (eventDetails != null) {
            try {
                parameters.putParameterValue(LogbookParameterName.eventDetailData,
                    (JsonHandler.unprettyPrint(JsonHandler.toJsonNode(eventDetails))));
            } catch (InvalidParseOperationException e1) {
                LOGGER.error("Could not serialize event details" + eventDetails);
            }
        }
        return parameters;
    }
}