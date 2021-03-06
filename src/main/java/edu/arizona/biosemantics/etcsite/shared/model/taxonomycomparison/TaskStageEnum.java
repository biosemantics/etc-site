package edu.arizona.biosemantics.etcsite.shared.model.taxonomycomparison;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum TaskStageEnum implements Serializable, IsSerializable {
	CREATE_INPUT("Create Input"),
    INPUT("Input"),
    ALIGN("Align"), 
    ANALYZE("Analyze"), 
    ANALYZE_COMPLETE("Analyze Complete");

    private String displayName;

    private TaskStageEnum() { }
    
    private TaskStageEnum(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() { 
    	return displayName; 
    }
}

