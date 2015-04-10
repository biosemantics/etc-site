package edu.arizona.biosemantics.etcsite.server.rpc.user;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.client.InvocationCallback;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.etcsite.client.content.settings.SettingsPlace;
import edu.arizona.biosemantics.etcsite.server.Configuration;
import edu.arizona.biosemantics.etcsite.server.db.DAOManager;
import edu.arizona.biosemantics.etcsite.server.rpc.auth.BCrypt;
import edu.arizona.biosemantics.etcsite.shared.model.ShortUser;
import edu.arizona.biosemantics.etcsite.shared.model.Task;
import edu.arizona.biosemantics.etcsite.shared.model.User;
import edu.arizona.biosemantics.etcsite.shared.rpc.auth.AuthenticationToken;
import edu.arizona.biosemantics.etcsite.shared.rpc.auth.CaptchaException;
import edu.arizona.biosemantics.etcsite.shared.rpc.auth.RegistrationFailedException;
import edu.arizona.biosemantics.etcsite.shared.rpc.matrixGeneration.MatrixGenerationException;
import edu.arizona.biosemantics.etcsite.shared.rpc.user.CreateOTOAccountException;
import edu.arizona.biosemantics.etcsite.shared.rpc.user.IUserService;
import edu.arizona.biosemantics.etcsite.shared.rpc.user.InvalidOTOAccountException;
import edu.arizona.biosemantics.etcsite.shared.rpc.user.InvalidPasswordException;
import edu.arizona.biosemantics.etcsite.shared.rpc.user.UserAddException;
import edu.arizona.biosemantics.etcsite.shared.rpc.user.UserNotFoundException;
import edu.arizona.biosemantics.matrixreview.shared.model.Model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.common.model.CreateUserResult;

public class UserService extends RemoteServiceServlet implements IUserService {
	
	private DAOManager daoManager = new DAOManager();
	
	@Override
	protected void doUnexpectedFailure(Throwable t) {
		String message = "Unexpected failure";
		log(message, t);
	    log(LogLevel.ERROR, "Unexpected failure", t);
	    super.doUnexpectedFailure(t);
	}
	
	@Override
	public List<ShortUser> getUsers(AuthenticationToken authenticationToken, boolean includeSelf) {
		List<ShortUser> usernames;
		if(includeSelf)
			usernames = daoManager.getUserDAO().getUsers();
		else
			usernames = daoManager.getUserDAO().getUsersWithout(authenticationToken.getUserId());
		return usernames;
	}

	@Override
	public ShortUser getUser(AuthenticationToken authenticationToken) throws UserNotFoundException {
		int userId = authenticationToken.getUserId();
		ShortUser user = daoManager.getUserDAO().getShortUser(userId);
		if(user != null){
			return user;
		} else { 
			throw new UserNotFoundException();
		}
	}
	
	@Override
	public ShortUser add(String firstName, String lastName, String email, String password) throws UserAddException {
		String encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		
		if(!daoManager.getUserDAO().hasUser(email)) {
			User user = daoManager.getUserDAO().insert(new User(encryptedPassword, firstName, lastName, email, "", "", "", "", "",null));// true, true, true, true));
			if(user == null) {
				throw new UserAddException("Adding user failed");
			}
			return new ShortUser(user);
		} else 
			throw new UserAddException("Email already exists");
	}
	
	@Override
	public ShortUser add(String openIdProviderId, String string, String firstName,
			String lastName, String password) throws UserAddException {
		String encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		
		if(!daoManager.getUserDAO().hasUser(openIdProviderId)) {
			User user = daoManager.getUserDAO().insert(new User(openIdProviderId, "google", password, firstName, lastName, "", "", "", "", "", true, true, true, true));
			if(user == null) {
				throw new UserAddException("Adding user failed");
			}
			return new ShortUser(user);
		} else 
			throw new UserAddException("Email already exists");
	}

	@Override
	public boolean existsUser(String email) {
		return daoManager.getUserDAO().hasUser(email);
	}
	
	@Override
	public edu.arizona.biosemantics.oto.common.model.User createOTOAccount(AuthenticationToken token, String email, String password) throws CreateOTOAccountException {
		ShortUser shortUser;
		try {
			shortUser = this.getUser(token);
		} catch (UserNotFoundException e) {
			throw new CreateOTOAccountException(e);
		}
		
		edu.arizona.biosemantics.oto.common.model.User otoUser = new edu.arizona.biosemantics.oto.common.model.User();
		otoUser.setUserEmail(email);
		otoUser.setPassword(password);
		otoUser.setAffiliation(shortUser.getAffiliation());
		otoUser.setFirstName(shortUser.getFirstName());
		otoUser.setLastName(shortUser.getLastName());
		otoUser.setBioportalUserId(shortUser.getBioportalUserId());
		otoUser.setBioportalApiKey(shortUser.getBioportalApiKey());
		try (OTOClient otoClient = new OTOClient(Configuration.otoUrl)) {
			otoClient.open();
			Future<CreateUserResult> createResult = otoClient.postUser(otoUser);
			CreateUserResult result = createResult.get();
			if(!result.isResult())
				throw new CreateOTOAccountException();
			
			
			return otoUser;
		}  catch (InterruptedException | ExecutionException e) {
			log(LogLevel.ERROR, "Problem creating OTO Account", e);
			throw new CreateOTOAccountException(e);
		}
	}
	
	//TODO: use https://code.google.com/p/google-api-java-client/w/list
	@Override
	public edu.arizona.biosemantics.oto.common.model.User createOTOAccount(AuthenticationToken token, String googleCode) throws CreateOTOAccountException {
		try {
			HttpClient httpclient = HttpClients.createDefault();
											//https://accounts.google.com/o/oauth2/auth
			HttpPost httppost = new HttpPost("https://accounts.google.com/o/oauth2/token");
	
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("code", googleCode));
			params.add(new BasicNameValuePair("client_id", Configuration.googleClientId));
			params.add(new BasicNameValuePair("client_secret", Configuration.googleSecret));
			params.add(new BasicNameValuePair("redirect_uri", Configuration.googleRedirectURI));
			params.add(new BasicNameValuePair("grant_type", "authorization_code"));
			UrlEncodedFormEntity w = new UrlEncodedFormEntity(params);
			System.out.println(w.getContentType().getName() +  " " + w.getContentType().getValue());
			httppost.setEntity(new UrlEncodedFormEntity(params));
			
			HttpResponse response = httpclient.execute(httppost);
			
			String accessToken = null;
			String tokenType = null;
			String expiresIn = null;
			String idToken = null;
			try {
				JSONObject elements = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
				accessToken = elements.getString("access_token");
				tokenType = elements.getString("token_type");
				expiresIn = elements.getString("expires_in");
				idToken = elements.getString("id_token"); 
			} catch(JSONException e) {
				log(LogLevel.ERROR, "Couldn't parse JSON", e);
			}
			if(accessToken != null) {
				HttpGet httpget = new HttpGet("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + accessToken);
				response = httpclient.execute(httpget);
				String firstName = null;
				String lastName = null;
				String email = null;
				try {
					JSONObject elements = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
					firstName = elements.getString("given_name");
					lastName = elements.getString("family_name");
					email = elements.getString("email"); 
				} catch(JSONException e) {
					log(LogLevel.ERROR, "Couldn't parse JSON", e);
				}
				if(email != null && firstName != null && lastName != null) {
					ShortUser shortUser;
					try {
						shortUser = this.getUser(token);
					} catch (UserNotFoundException e) {
						throw new CreateOTOAccountException(e);
					}
					
					edu.arizona.biosemantics.oto.common.model.User otoUser = new edu.arizona.biosemantics.oto.common.model.User();
					String dummyPassword = firstName + lastName;
					otoUser.setUserEmail(email);
					otoUser.setFirstName(firstName);
					otoUser.setLastName(lastName);
					otoUser.setPassword(dummyPassword);
					otoUser.setAffiliation(shortUser.getAffiliation());
					otoUser.setFirstName(shortUser.getFirstName());
					otoUser.setLastName(shortUser.getLastName());
					otoUser.setBioportalUserId(shortUser.getBioportalUserId());
					otoUser.setBioportalApiKey(shortUser.getBioportalApiKey());
					
					try (OTOClient otoClient = new OTOClient(Configuration.otoUrl)) {
						otoClient.open();
						Future<CreateUserResult> createResult = otoClient.postUser(otoUser);
						CreateUserResult result = createResult.get();
						if(!result.isResult())
							throw new CreateOTOAccountException();
						return otoUser;
					}  catch (InterruptedException | ExecutionException e) {
						log(LogLevel.ERROR, "Problem creating OTO Account", e);
						throw new CreateOTOAccountException(e);
					}
				}
			} 
		}catch(Exception e) {
			log(LogLevel.ERROR, "Couldn't create OTO Account using Google", e);
			throw new CreateOTOAccountException(e);
		}
		throw new CreateOTOAccountException();
	}

	@Override
	public void saveOTOAccount(AuthenticationToken authenticationToken, boolean share, String email, String password) throws InvalidOTOAccountException {
		User user = daoManager.getUserDAO().getUser(authenticationToken.getUserId());
				
		if(share) {
			try (OTOClient otoClient = new OTOClient(Configuration.otoUrl)) {
				otoClient.open();
				edu.arizona.biosemantics.oto.common.model.User otoUser = new edu.arizona.biosemantics.oto.common.model.User();
				otoUser.setUserEmail(email);
				otoUser.setPassword(password);
				Future<String> tokenResult = otoClient.getUserAuthenticationToken(otoUser);
				String token = tokenResult.get();
				if(token != null && !token.isEmpty()) {
					user.setOtoAccountEmail(otoUser.getUserEmail());
					user.setOtoAuthenticationToken(token);
					daoManager.getUserDAO().update(user);
				} else {
					user.setOtoAccountEmail("");
					user.setOtoAuthenticationToken("");
					daoManager.getUserDAO().update(user);
					throw new InvalidOTOAccountException();
				}
			} catch (InterruptedException | ExecutionException e) {
				log(LogLevel.ERROR, "Problem saving OTO Account", e);
			}
		} else {
			user.setOtoAccountEmail("");
			user.setOtoAuthenticationToken("");
			daoManager.getUserDAO().update(user);
		}
	}

	@Override
	public ShortUser update(AuthenticationToken authenticationToken, ShortUser shortUser) throws UserNotFoundException {
		User user = daoManager.getUserDAO().getUser(authenticationToken.getUserId());
		if(user == null)
			throw new UserNotFoundException();			
		
		user.setAffiliation(shortUser.getAffiliation());
		user.setBioportalAPIKey(shortUser.getBioportalApiKey());
		user.setBioportalUserId(shortUser.getBioportalUserId());
		user.setEmail(shortUser.getEmail());
		user.setFirstName(shortUser.getFirstName());
		user.setLastName(shortUser.getLastName());
		/*user.setTextCaptureEmail(shortUser.isTextCaptureEmail());
		user.setMatrixGenerationEmail(shortUser.isMatrixGenerationEmail());
		user.setTreeGenerationEmail(shortUser.isTreeGenerationEmail());
		user.setTaxonomyComparisonEmail(shortUser.isTaxonomyComparisonEmail());
		*/
		daoManager.getUserDAO().update(user);
		return daoManager.getUserDAO().getShortUser(authenticationToken.getUserId());
	}

	@Override
	public ShortUser update(AuthenticationToken authenticationToken, String oldPassword, String newPassword) throws UserNotFoundException, InvalidPasswordException { 
		User user = daoManager.getUserDAO().getUser(authenticationToken.getUserId());
		if(user == null)
			throw new UserNotFoundException();			
		
		if(!BCrypt.checkpw(oldPassword, user.getPassword())) {
			throw new InvalidPasswordException();
		}
		
		String encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
		user.setPassword(encryptedPassword);
		
		daoManager.getUserDAO().update(user);
		return daoManager.getUserDAO().getShortUser(authenticationToken.getUserId());
	}
	
	public void setPopupPreference(AuthenticationToken token,String type,boolean dontShowPopup)
	{
		User user = daoManager.getUserDAO().getUser(token.getUserId());
		
		if(user.getProfile()==null)
		{
			Map<String, Boolean> UserProfile= new HashMap<String, Boolean>(); 
			user.setProfile(UserProfile);
		}
		user.setProfileValue(type, dontShowPopup);
		daoManager.getUserDAO().update(user);
	
	}

//	@Override
//	public void log(LogLevel arg0, String arg1) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void log(LogLevel arg0, String arg1, Throwable arg2) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public boolean isProfile(AuthenticationToken token, String type) {
		
		boolean isProfileSet=false;
		
		User user = daoManager.getUserDAO().getUser(token.getUserId());
		if(user.getProfile()!=null)
		{
			isProfileSet= user.getProfileValue(type);
			
		}
		
		return isProfileSet;
	}

//	@Override
//	public Boolean isProfile(String type) {
//		daoManager.getUserDAO().g
//	}


	
}
