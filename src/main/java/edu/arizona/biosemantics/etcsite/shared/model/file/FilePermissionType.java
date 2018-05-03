package edu.arizona.biosemantics.etcsite.shared.model.file;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum FilePermissionType implements IsSerializable{
	OWNER, SHARED_WITH, NONE, ADMIN;
	
	private FilePermissionType(){}
}
