package edu.arizona.biosemantics.etcsite.shared.model.semanticmarkup;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum TaskStageEnum implements IsSerializable {
	CREATE_INPUT("Create Input"),
    INPUT("Input"),
    PREPROCESS_TEXT("Preprocess Text"),
    LEARN_TERMS("Learn Terms"),
    REVIEW_TERMS("Review Terms"),
    PARSE_TEXT("Parse Text"),
    OUTPUT("Output");

    private String displayName;
    
    private TaskStageEnum(){}

    private TaskStageEnum(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() { 
    	return displayName; 
    }
    
    public static TaskStageEnum fromDisplayName(String displayName) {
    	for(TaskStageEnum value : values()) {
    		if(value.displayName().equals(displayName))
    			return value;
    	}
    	return null;
    }
}

