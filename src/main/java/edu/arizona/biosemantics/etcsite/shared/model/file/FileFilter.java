package edu.arizona.biosemantics.etcsite.shared.model.file;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum FileFilter implements IsSerializable{
	TAXON_DESCRIPTION, 
	MARKED_UP_TAXON_DESCRIPTION,
	OWL_ONTOLOGY,
	MATRIX,
	ALL, 
	DIRECTORY,
	FILE, 
	CLEANTAX,
	MATRIX_GENERATION_SERIALIZED_MODEL;
	
	private FileFilter(){}
}
