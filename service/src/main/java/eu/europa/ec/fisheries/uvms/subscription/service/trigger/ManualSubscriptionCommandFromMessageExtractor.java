/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.filter.AreaFilterComponent;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;

/**
 * Implementation of {@link SubscriptionCommandFromMessageExtractor} for manual subscription triggered messages.
 */
@ApplicationScoped
public class ManualSubscriptionCommandFromMessageExtractor extends SubscriptionBasedCommandFromMessageExtractor {

    private static final String SOURCE = "manual";

    @Inject
    public ManualSubscriptionCommandFromMessageExtractor(SubscriptionFinder subscriptionFinder,
                                                         TriggerCommandsFactory triggerCommandsFactory,
                                                         DatatypeFactory datatypeFactory,
                                                         DateTimeService dateTimeService, AssetSender assetSender, AreaFilterComponent areaFilterComponent) {
        super(subscriptionFinder, triggerCommandsFactory, datatypeFactory, dateTimeService, assetSender, areaFilterComponent);
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    ManualSubscriptionCommandFromMessageExtractor() {
        super();
    }

    @Override
    public String getEligibleSubscriptionSource() {
        return SOURCE;
    }
}
