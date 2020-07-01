package com.cesi.decode.message;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class ProducerSb implements ProducerSbLocal {

    @Resource(mappedName = "jms/responseQueue")
    private Queue responseQueue;
    @Resource(mappedName = "jms/responseQueuePoolFactory")
    private ConnectionFactory responseQueuePoolFactory;

    /**
     * Create a TextMessage Message to put in the queue.
     *
     * @param session - The session created to access the JMS queue.
     * @param secret - The secret to find in the file(s).
     * @param appVersion - The version of the app.
     * @param operationVersion - The version of the operation.
     * @param tokenApp - The token of the application.
     * @param tokenUser - The token of the user.
     * @param path - The path of the file containing the secret message.
     * @param key - The key to decrypt the file.
     * @param confidence - The probability of the file to be in french.
     * @return The newly created TextMessage.
     * @throws JMSException The JMSException thrown if something goes wrong.
     */
    private Message createJMSMessageForJmsResponseQueue(Session session, String secret, String appVersion,
                                                        String operationVersion, String tokenApp, String tokenUser,
                                                        String path, String key, String confidence) throws JMSException {
        TextMessage tm = session.createTextMessage();
        tm.setText(secret);
        tm.setStringProperty("path", path);
        tm.setStringProperty("key", key);
        tm.setStringProperty("confidence", confidence);
        tm.setStringProperty("appVersion", appVersion);
        tm.setStringProperty("operationVersion", operationVersion);
        tm.setStringProperty("tokenApp", tokenApp);
        tm.setStringProperty("tokenUser", tokenUser);
        return tm;
    }

    /**
     * Send a Message to the jms/fileQueue.
     *
     * @param secret - The secret to find in the file(s).
     * @param appVersion - The version of the app.
     * @param operationVersion - The version of the operation.
     * @param tokenApp - The token of the application.
     * @param tokenUser - The token of the user.
     * @param path - The path of the file containing the secret message.
     * @param key - The key to decrypt the file.
     * @param confidence - The probability of the file to be in french.
     * @throws JMSException The JMSException thrown if something goes wrong.
     */
    private void sendJmsMessageToResponseQueue(String secret, String appVersion, String operationVersion,
                                               String tokenApp, String tokenUser,String path, String key,
                                               String confidence) throws JMSException {
        Connection connection = null;
        Session session = null;
        try {
            connection = responseQueuePoolFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(responseQueue);
            messageProducer.send(createJMSMessageForJmsResponseQueue(session, secret, appVersion, operationVersion,
                    tokenApp, tokenUser, path, key, confidence));
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Method used to call the methods above.
     *
     * @param secret - The secret to find in the file(s).
     * @param appVersion - The version of the app.
     * @param operationVersion - The version of the operation.
     * @param tokenApp - The token of the application.
     * @param tokenUser - The token of the user.
     * @param path - The path of the file containing the secret message.
     * @param key - The key to decrypt the file.
     * @param confidence - The probability of the file to be in french.
     * @throws JMSException The JMSException thrown if something goes wrong.
     */
    @Override
    public void sendMessageToQueue(String secret, String appVersion, String operationVersion, String tokenApp,
                                   String tokenUser,String path, String key, String confidence) throws JMSException {
        sendJmsMessageToResponseQueue(secret, appVersion, operationVersion, tokenApp, tokenUser, path, key, confidence);
    }
}
