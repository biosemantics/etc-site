package edu.arizona.biosemantics.etcsite.shared.rpc.auth;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.arizona.biosemantics.etcsite.shared.model.Captcha;

public interface IAuthenticationServiceAsync {
	
	public void login(String user, String password,
			AsyncCallback<AuthenticationResult> callback);
	
	public void loginWithGoogle(String googleAuthCode,
			AsyncCallback<AuthenticationResult> callback);

	public void isValidSession(AuthenticationToken authentication,
			AsyncCallback<AuthenticationResult> callback);
	
	public void requestPasswordResetCode(int captchaId, String captchaSolution,
			String email, AsyncCallback<Void> callback);

	public void resetPassword(String email, String resetCode,
			String newPassword, AsyncCallback<Void> callback);

	public void createCaptcha(AsyncCallback<Captcha> callback);

}
