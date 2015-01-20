/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.eventing.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import java.util.Enumeration;

public class AFMessageListener implements MessageListener {
    Log log = LogFactory.getLog(AFMessageListener.class);
    private String subscriptionId;

    public AFMessageListener(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        
    }

    @Override
    public void onMessage(Message message) {
        if (log.isDebugEnabled()) {
            if (message instanceof MapMessage) {
                try {
                    String messageBody = ((MapMessage) message).getString(TopicPublisher.MESSAGE_BODY);
                    log.debug("Received a message:" + messageBody);
                } catch (JMSException e) {
                    log.error("Error while getting message content.", e);
                }
            }
        }
        MapMessage mapMessage;
        if (message instanceof MapMessage) {
            mapMessage = (MapMessage) message;
            MessageStore.getInstance().addMessage(this.subscriptionId, mapMessage);
        } else if (message instanceof TextMessage) {
            //Todo:remove this. we only support mapMessages initially and below code is only for testing purpose.
            final TextMessage textMessage = (TextMessage) message;
            mapMessage = new MapMessage() {
                @Override
                public boolean getBoolean(String s) throws JMSException {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public byte getByte(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public short getShort(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public char getChar(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public int getInt(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public long getLong(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public float getFloat(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public double getDouble(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public String getString(String s) throws JMSException {
                    return textMessage.getText();
                }

                @Override
                public byte[] getBytes(String s) throws JMSException {
                    return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public Object getObject(String s) throws JMSException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public Enumeration getMapNames() throws JMSException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setBoolean(String s, boolean b) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setByte(String s, byte b) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setShort(String s, short i) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setChar(String s, char c) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setInt(String s, int i) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setLong(String s, long l) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setFloat(String s, float v) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setDouble(String s, double v) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setString(String s, String s2) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setBytes(String s, byte[] bytes) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setBytes(String s, byte[] bytes, int i, int i2) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setObject(String s, Object o) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public boolean itemExists(String s) throws JMSException {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public String getJMSMessageID() throws JMSException {
                    return textMessage.getJMSMessageID();
                }

                @Override
                public void setJMSMessageID(String s) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public long getJMSTimestamp() throws JMSException {
                    return textMessage.getJMSTimestamp();
                }

                @Override
                public void setJMSTimestamp(long l) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
                    return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setJMSCorrelationID(String s) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public String getJMSCorrelationID() throws JMSException {
                    return textMessage.getJMSCorrelationID();
                }

                @Override
                public Destination getJMSReplyTo() throws JMSException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setJMSReplyTo(Destination destination) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public Destination getJMSDestination() throws JMSException {
                    return textMessage.getJMSDestination();
                }

                @Override
                public void setJMSDestination(Destination destination) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public int getJMSDeliveryMode() throws JMSException {
                    return textMessage.getJMSDeliveryMode();
                }

                @Override
                public void setJMSDeliveryMode(int i) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public boolean getJMSRedelivered() throws JMSException {
                    return textMessage.getJMSRedelivered();
                }

                @Override
                public void setJMSRedelivered(boolean b) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public String getJMSType() throws JMSException {
                    return textMessage.getJMSType();
                }

                @Override
                public void setJMSType(String s) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public long getJMSExpiration() throws JMSException {
                    return textMessage.getJMSExpiration();
                }

                @Override
                public void setJMSExpiration(long l) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public int getJMSPriority() throws JMSException {
                    return textMessage.getJMSPriority();
                }

                @Override
                public void setJMSPriority(int i) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void clearProperties() throws JMSException {
                    textMessage.clearProperties();
                }

                @Override
                public boolean propertyExists(String s) throws JMSException {
                    return textMessage.propertyExists(s);
                }

                @Override
                public boolean getBooleanProperty(String s) throws JMSException {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public byte getByteProperty(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public short getShortProperty(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public int getIntProperty(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public long getLongProperty(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public float getFloatProperty(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public double getDoubleProperty(String s) throws JMSException {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public String getStringProperty(String s) throws JMSException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public Object getObjectProperty(String s) throws JMSException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public Enumeration getPropertyNames() throws JMSException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setBooleanProperty(String s, boolean b) throws JMSException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void setByteProperty(String s, byte b) throws JMSException {
                    textMessage.setByteProperty(s, b);
                }

                @Override
                public void setShortProperty(String s, short i) throws JMSException {
                    textMessage.setShortProperty(s, i);
                }

                @Override
                public void setIntProperty(String s, int i) throws JMSException {
                    textMessage.setIntProperty(s, i);
                }

                @Override
                public void setLongProperty(String s, long l) throws JMSException {
                    textMessage.setLongProperty(s, l);
                }

                @Override
                public void setFloatProperty(String s, float v) throws JMSException {
                    textMessage.setFloatProperty(s, v);
                }

                @Override
                public void setDoubleProperty(String s, double v) throws JMSException {
                    textMessage.setDoubleProperty(s, v);
                }

                @Override
                public void setStringProperty(String s, String s2) throws JMSException {
                    textMessage.setStringProperty(s, s2);
                }

                @Override
                public void setObjectProperty(String s, Object o) throws JMSException {
                    textMessage.setObjectProperty(s, o);
                }

                @Override
                public void acknowledge() throws JMSException {
                    textMessage.acknowledge();
                }

                @Override
                public void clearBody() throws JMSException {
                    textMessage.clearBody();
                }
            };
            MessageStore.getInstance().addMessage(this.subscriptionId, mapMessage);
        }

    }

	@Override
	public String toString() {
		return "AFMessageListener [subscriptionId=" + subscriptionId + "]";
	}

	
    
    
}
