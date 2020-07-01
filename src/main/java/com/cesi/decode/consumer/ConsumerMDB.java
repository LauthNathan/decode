package com.cesi.decode.consumer;

import com.cesi.decode.message.ProducerSbLocal;
import com.cesi.decode.services.DecodeServiceLocal;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = "jms/fileQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/fileQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ConsumerMDB implements MessageListener {

    @EJB
    private ProducerSbLocal producerSb;
    @EJB
    private DecodeServiceLocal decodeService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String contentByte = textMessage.getText();
            String fileContent = decodeService.byteToString(contentByte);

            String path = textMessage.getStringProperty("path");
            String key = textMessage.getStringProperty("key");
            String appVersion = textMessage.getStringProperty("appVersion");
            String operationVersion = textMessage.getStringProperty("operationVersion");
            String tokenApp = textMessage.getStringProperty("tokenApp");
            String tokenUser = textMessage.getStringProperty("tokenUser");
            if (decodeService.isFrench(fileContent)) {
                String mySecret = decodeService.searchSecret(fileContent);
                if (!mySecret.equals("false")) {
                    producerSb.sendMessageToQueue(mySecret, appVersion, operationVersion, tokenApp, tokenUser, path, key, "20");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
