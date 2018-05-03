package edu.arizona.biosemantics.etcsite.client.common.files;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

import edu.arizona.biosemantics.etcsite.shared.model.file.DescriptionEntry;
import edu.arizona.biosemantics.etcsite.shared.model.file.TaxonIdentificationEntry;

public interface ICreateSemanticMarkupFilesView extends IsWidget {

	public interface Presenter {

		ICreateSemanticMarkupFilesView getView();

		void onCreate(boolean requireAuthorityDate, boolean isProkaryote);

		void setDestinationFilePath(String destinationFilePath);
		
		int getFilesCreated();

		void init();

		void onBatch(String text);

		void setPreviewText(LinkedHashMap<String, String> map, String text);

	}

	void setPresenter(ICreateSemanticMarkupFilesView.Presenter presenter);

	String getAuthor();
	
	String getPAuthor();

	String getYear();
	
	String getPYear();

	String getTitleText();
	
	String getPTitleText();

	List<TaxonIdentificationEntry> getTaxonIdentificationEntries();
	
	List<TaxonIdentificationEntry> getPTaxonIdentificationEntries();
	
	List<DescriptionEntry> getDescriptionsList();
	
	String getPDescription();
	
	String getStrainNumber(); 
	
	String getEqStrainNumbers(); 

	String getStrainAccession();
	
	String getStrainGenomeAccession();
	
	String getAlternativeTaxonomy();
	
	String getPAlternativeTaxonomy();

	void removeAddtionalTaxonRanks();
	
	void removePAddtionalTaxonRanks();

	String getDOI();
	
	String getPDOI();

	String getFullCitation();
	
	String getPFullCitation();

	void updateProgress(double value);

	void hideProgress();

	void showProgress();

	boolean isCopyCheckBox();

	void resetDescriptions();
	
	void resetPDescriptions();

	void setPreviewText(String text);

	void clearBatchText();

	void resetStrain();


	
}
