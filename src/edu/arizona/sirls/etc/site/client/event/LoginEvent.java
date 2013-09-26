package edu.arizona.sirls.etc.site.client.event;

import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.sirls.etc.site.client.HistoryState;

public class LoginEvent extends GwtEvent<LoginEventHandler> implements ETCSiteEvent {

	public static Type<LoginEventHandler> TYPE = new Type<LoginEventHandler>();
	private String username;
	private String sessionID;

	
	public LoginEvent(String username, String sessionID) {
		this.username = username;
		this.sessionID = sessionID;
	}

	@Override
	public Type<LoginEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(LoginEventHandler handler) {
		handler.onLogin(this);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	@Override
	public boolean requiresLogin() {
		return false;
	}

	@Override
	public HistoryState getHistoryState() {
		return null;
	}

	@Override
	public GwtEvent<?> getGwtEvent() {
		return this;
	}
	
}
