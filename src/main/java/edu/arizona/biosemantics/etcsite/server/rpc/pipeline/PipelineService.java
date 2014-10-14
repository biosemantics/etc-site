package edu.arizona.biosemantics.etcsite.server.rpc.pipeline;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.arizona.biosemantics.etcsite.shared.log.LogLevel;
import edu.arizona.biosemantics.etcsite.shared.model.Task;
import edu.arizona.biosemantics.etcsite.shared.rpc.auth.AuthenticationToken;
import edu.arizona.biosemantics.etcsite.shared.rpc.pipeline.IPipelineService;

public class PipelineService extends RemoteServiceServlet implements IPipelineService {
	
	@Override
	protected void doUnexpectedFailure(Throwable t) {
		String message = "Unexpected failure";
		log(message, t);
	    log(LogLevel.ERROR, "Unexpected failure", t);
	    super.doUnexpectedFailure(t);
	}
	
	@Override
	public Task getLatestResumable(AuthenticationToken authenticationToken) {
		return null;
	}

	@Override
	public void cancel(AuthenticationToken authenticationToken, Task task) {
		
	}


}
