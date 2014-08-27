package edu.arizona.biosemantics.etcsite.server.rpc;

import java.io.File;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.arizona.biosemantics.etcsite.server.Configuration;
import edu.arizona.biosemantics.etcsite.shared.model.RPCResult;
import edu.arizona.biosemantics.etcsite.shared.model.Setup;
import edu.arizona.biosemantics.etcsite.shared.rpc.ISetupService;

public class SetupService extends RemoteServiceServlet implements ISetupService {
	
	@Override
	public RPCResult<Setup> getSetup() {		
		String seperator = File.separator;
		Setup setup = new Setup();
		setup.setSeperator(seperator);
		setup.setFileBase(Configuration.fileBase);
		setup.setGoogleClientId(Configuration.googleClientId);
		setup.setGoogleRedirectURI(Configuration.googleRedirectURI);
		setup.setDeploymentUrl(Configuration.deploymentUrl);
		return new RPCResult<Setup>(true, setup);
	}

}
