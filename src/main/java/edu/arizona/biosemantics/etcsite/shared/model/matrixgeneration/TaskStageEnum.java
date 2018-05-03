package edu.arizona.biosemantics.etcsite.shared.model.matrixgeneration;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum TaskStageEnum implements IsSerializable{
	CREATE_INPUT("Create/Select Input"),
    INPUT("Input"),
    PROCESS("Process"),
    REVIEW("Review"),
    OUTPUT("Output");

    private String displayName;
    
    private TaskStageEnum () {}

    private TaskStageEnum(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() { 
    	return displayName; 
    }
}
