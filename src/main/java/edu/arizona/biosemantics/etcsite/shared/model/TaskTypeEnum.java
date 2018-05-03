package edu.arizona.biosemantics.etcsite.shared.model;

import java.io.Serializable;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum TaskTypeEnum implements Serializable, IsSerializable {
	SEMANTIC_MARKUP("Text Capture"),
	ONTOLOGIZE("Ontology Building"),
	MATRIX_GENERATION("Matrix Generation"),
	TREE_GENERATION("Key Generation"),
	TAXONOMY_COMPARISON("Taxonomy Comparison"), 
	VISUALIZATION("Visualization");
	
	private String displayName;

	TaskTypeEnum() { }
	
	TaskTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() { 
    	return displayName; 
    }
}