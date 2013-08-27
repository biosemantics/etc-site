package edu.arizona.sirls.etc.site.shared.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.arizona.sirls.etc.site.client.AuthenticationToken;
import edu.arizona.sirls.etc.site.client.api.fileFormat.ValidGlossaryAsyncCallback;

public interface IFileFormatServiceAsync {
	
	public void isValidTaxonDescription(AuthenticationToken authenticationToken, String target, AsyncCallback<Boolean> callback);

	public void isValidGlossary(AuthenticationToken authenticationToken, String target, AsyncCallback<Boolean> callback);
}
