package org.cote.rest.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.BulkFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.MessageFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.SpoolStatusEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.JSONUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/wss/{objectId}")
public class WebSocketService  extends HttpServlet {
	public static final Logger logger = LogManager.getLogger(WebSocketService.class);

	// private Map<String, Map<String, UserType>> sessionMap =
	// Collections.synchronizedMap(new HashMap<>());
	//private Map<String, Session> objectIdSession = Collections.synchronizedMap(new HashMap<>());
	private Map<String, UserType> userMap = Collections.synchronizedMap(new HashMap<>());
	
	
	//private static Map<String, UserType> 

	/*
	 * private Map<String, UserType> getMap(String sessionId){
	 * if(!sessionMap.containsKey(sessionId)) { sessionMap.put(sessionId, new
	 * HashMap<>()); } return sessionMap.get(sessionId); }
	 */

	private Map<String, List<String>> buffer = Collections.synchronizedMap(new HashMap<>());

	private MessageSpoolType newMessage(Session session, String messageName, String messageContent) {

		UserType user = null;
		if(!userMap.containsKey(session.getId())) {
			return null;
		}
		user = userMap.get(session.getId());
		return newMessage(user, messageName, messageContent, null);
	}
	private MessageSpoolType newMessage(UserType user, String messageName, String messageContent, NameIdType ref) {

		List<MessageSpoolType> msgs = new ArrayList<>();
		try {
			MessageFactory mfact = Factories.getFactory(FactoryEnumType.MESSAGE);
			MessageSpoolType msg = mfact.newMessage(user);
			msg.setName(messageName);
			msg.setData(messageContent.getBytes(StandardCharsets.UTF_8));
			msg.setValueType(ValueEnumType.STRING);
			msg.setSpoolStatus(SpoolStatusEnumType.SPOOLED);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, 30);
			msg.setExpiryDate(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
			msg.setSpoolBucketName(SpoolNameEnumType.MESSAGE);
			msg.setSpoolBucketType(SpoolBucketEnumType.MESSAGE_QUEUE);
			if(ref != null) {
				msg.setReferenceType(FactoryEnumType.valueOf(ref.getNameType().toString()));
				msg.setReferenceId(ref.getId());
			}
			boolean added = mfact.addMessage(msg);
			if(!added) {
				logger.error("Failed to add message");
			}
			else {
				logger.info("Spooled " + messageName + " for " + user.getUrn());
			}
			msgs = mfact.getMessagesFromUserGroup(messageName, SpoolNameEnumType.MESSAGE, SpoolStatusEnumType.SPOOLED, null, user);
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e);
		}
		if(msgs.size() > 0) {
			return msgs.get(0);
		}
		return null;
	}
	
	private List<MessageSpoolType> getTransmitMessages(Session session){
		UserType user = null;
		if(!userMap.containsKey(session.getId())) {
			return null;
		}
		user = userMap.get(session.getId());
		List<MessageSpoolType> msgs = new ArrayList<>();
		try {
			MessageFactory mfact = Factories.getFactory(FactoryEnumType.MESSAGE);
			msgs = mfact.getMessagesFromUserGroup(null, SpoolNameEnumType.MESSAGE, SpoolStatusEnumType.SPOOLED, null, user);
			for(MessageSpoolType m : msgs) {
				m.setSpoolStatus(SpoolStatusEnumType.TRANSMITTED);
				mfact.update(m);
			}
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return msgs;
	}

	private void sendMessage(Session session) {
		if (!userMap.containsKey(session.getId())) {
			logger.warn("Session is not mapped to a key");
			return;
		}

		RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
		//asyncRemote.sendText(builder.build().toString());
		// Send pending messages
		List<MessageSpoolType> messages = getTransmitMessages(session);
		if (messages != null) {
			//messages.forEach(asyncRemote::sendText);
			asyncRemote.sendText(JSONUtil.exportObject(messages));
		}
	}

	@OnOpen
	public void onOpen(Session session, @PathParam("objectId") String objectId) {
		logger.info("Opened socket for " + session.getId() + " / " + objectId);
		// Map<String, UserType> map = getMap(session.getId());
		//if (!userMap.containsKey(objectId)) {
			// if(!map.containsKey(objectId)) {
			INameIdFactory fact;
			try {
				fact = Factories.getFactory(FactoryEnumType.USER);
				UserType user = fact.getByObjectId(objectId, 0);
				if (user != null) {
					// map.put(objectId, user);
					//objectIdSession.put(objectId, session);
					userMap.put(session.getId(), user);
					newMessage(session, "Pickup the phone", "New session started");
				}
			} catch (FactoryException | ArgumentException e) {
				logger.error(e);
			}
			sendMessage(session);

		//}
	}

	@OnMessage
	public void onMessage(String txt, Session session) throws IOException {
		UserType user = userMap.get(session.getId());
		
		MessageSpoolType message = JSONUtil.importObject(txt,  MessageSpoolType.class);
		if(user != null && message != null) {
			logger.info("Send message " + message.getName() + " to " + message.getRecipientId() + " from " + user.getUrn() + " (" + user.getObjectId() + ")");
			if(message.getRecipientId() != null && message.getRecipientType().equals(FactoryEnumType.USER)) {
				INameIdFactory fact;
				try {
					fact = Factories.getFactory(FactoryEnumType.USER);
					UserType targUser = fact.getById(message.getRecipientId(), user.getOrganizationId());
					if (targUser != null) {
						newMessage(targUser, message.getName(), new String(message.getData(), StandardCharsets.UTF_8), user);
					}
					else {
						logger.error("Did not find user #" + message.getRecipientId());
					}
				} catch (FactoryException | ArgumentException e) {
					logger.error(e);
				}
			}
			else {
				logger.warn("Handle message with no recipient");
			}
		}
		else {
			logger.error("User or message was null");
		}
		//session.getBasicRemote().sendText(txt.toUpperCase());
	}

	@OnClose
	public void onClose(CloseReason reason, Session session) {
		//logger.info(String.format("Closing a WebSocket (%s) due to %s", session.getId(), reason.getReasonPhrase()));
		//newMessage(session, "Hangup the phone", "Session ended");
		userMap.remove(session.getId());
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		logger.error(String.format("Error in WebSocket session %s%n", session == null ? "null" : session.getId()), t);
	}
		
}
