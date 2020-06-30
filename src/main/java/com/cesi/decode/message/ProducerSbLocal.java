package com.cesi.decode.message;

import javax.ejb.Local;
import javax.jms.JMSException;
import java.util.ArrayList;

@Local
public interface ProducerSbLocal {
    public void sendMessageToQueue(String secret, String appVersion, String operationVersion, String tokenApp,
                                   String tokenUser,String path, String key, String confidence) throws JMSException;
}
