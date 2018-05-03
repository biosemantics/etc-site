package edu.arizona.biosemantics.etcsite.shared.model.treegeneration;
import com.google.gwt.user.client.rpc.IsSerializable;
public enum TaskStageEnum implements IsSerializable {
	CREATE_INPUT("Create Input"),
    INPUT("Input"),
    VIEW("View");

    private String displayName;

    private TaskStageEnum(){}
    private TaskStageEnum(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() { 
    	return displayName; 
    }
}

