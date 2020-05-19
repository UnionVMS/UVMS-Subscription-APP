package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.user.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.wsdl.asset.module.AssetModuleMethod;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidRequest;
import eu.europa.ec.fisheries.wsdl.asset.module.FindVesselIdsByAssetHistGuidResponse;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;

/**
 * Implementation of {@link AssetClient}
 */
@ApplicationScoped
public class AssetClientImpl implements AssetClient{

    private SubscriptionAssetProducerBean subscriptionAssetProducerBean;
    private SubscriptionUserConsumerBean subscriptionUserConsumer;

    /**
     * Injection constructor.
     *
     * @param subscriptionAssetProducerBean The (JMS) producer bean for this module
     * @param subscriptionUserConsumer The user queue
     */
    @Inject
    public AssetClientImpl(SubscriptionAssetProducerBean subscriptionAssetProducerBean, SubscriptionUserConsumerBean subscriptionUserConsumer) {
        this.subscriptionAssetProducerBean = subscriptionAssetProducerBean;
        this.subscriptionUserConsumer = subscriptionUserConsumer;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    AssetClientImpl() {
        // NOOP
    }

    @Override
    public Map<SubscriptionVesselIdentifier, String> findAsset(String assetHistGuid) {
        FindVesselIdsByAssetHistGuidRequest request = new FindVesselIdsByAssetHistGuidRequest();
        request.setMethod(AssetModuleMethod.FIND_VESSEL_IDS_BY_ASSET_HIST_GUID);
        request.setAssetHistoryGuid(assetHistGuid);
        Map<SubscriptionVesselIdentifier, String> identifiers = new HashMap<>();
        try {
            String correlationID = subscriptionAssetProducerBean.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request),
                    subscriptionAssetProducerBean.getDestination(),
                    subscriptionUserConsumer.getDestination());

            if(correlationID != null) {
                TextMessage message = subscriptionUserConsumer.getMessage(correlationID, TextMessage.class );
                FindVesselIdsByAssetHistGuidResponse response = JAXBUtils.unMarshallMessage( message.getText() , FindVesselIdsByAssetHistGuidResponse.class);
                if(response.getCfr() != null) {
                    identifiers.put(SubscriptionVesselIdentifier.CFR, response.getCfr());
                }
                if(response.getIrcs() != null) {
                    identifiers.put(SubscriptionVesselIdentifier.IRCS, response.getIrcs());
                }
                if(response.getIccat() != null) {
                    identifiers.put(SubscriptionVesselIdentifier.ICCAT, response.getIccat());
                }
                if(response.getExtMark() != null) {
                    identifiers.put(SubscriptionVesselIdentifier.EXT_MARK, response.getExtMark());
                }
                if(response.getUvi() != null) {
                    identifiers.put(SubscriptionVesselIdentifier.UVI, response.getUvi());
                }
            }
        } catch (MessageException | ModelMarshallException | JMSException | JAXBException e) {
            throw new ApplicationException(e);
        }
        return identifiers;
    }
}
