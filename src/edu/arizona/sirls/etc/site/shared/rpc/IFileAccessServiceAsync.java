package edu.arizona.sirls.etc.site.shared.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.arizona.sirls.etc.site.client.AuthenticationToken;
import edu.arizona.sirls.etc.site.shared.rpc.file.FileType;
import edu.arizona.sirls.etc.site.shared.rpc.file.search.Search;

public interface IFileAccessServiceAsync {
	
	public void setFileContent(AuthenticationToken authenticationToken, String target, String content, AsyncCallback<Boolean> callback);

	public void getFileContent(AuthenticationToken authenticationToken, String target, AsyncCallback<String> callback);

	public void getFileContent(AuthenticationToken authenticationToken, String target, FileType fileType, AsyncCallback<String> callback);

	public void getFileContentHighlighted(AuthenticationToken authenticationToken, String target, FileType taxonDescription, AsyncCallback<String> asyncCallback);

	
}
