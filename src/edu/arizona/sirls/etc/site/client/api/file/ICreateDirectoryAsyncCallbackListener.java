package edu.arizona.sirls.etc.site.client.api.file;

public interface ICreateDirectoryAsyncCallbackListener {

	public void notifyResult(Boolean result);

	public void notifyException(Throwable caught);

}
