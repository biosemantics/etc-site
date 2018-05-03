package edu.arizona.biosemantics.etcsite.shared.model.file;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum FileSource implements Serializable, IsSerializable {

	SYSTEM, OWNED, SHARED, PUBLIC;
	
}