package edu.arizona.biosemantics.etcsite.server.rpc.matrixgeneration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.etcsite.server.enhance.EnhanceRun;
import edu.arizona.biosemantics.etcsite.shared.rpc.matrixGeneration.MatrixGenerationException;
import edu.arizona.biosemantics.matrixgeneration.CLIMain;

public class InJvmEnhance implements Enhance {
	
	private String inputDir;
	private String outputDir;
	private String inputOntology;
	private String termReviewSynonyms;
	private String termReviewTermCategorization;
	private String taxonGroup;
	
	
	private boolean executedSuccessfully = false;
	
	public InJvmEnhance(String inputDir, String outputDir, String inputOntology, 
			String termReviewTermCategorization, String termReviewSynonyms, String taxonGroup) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.inputOntology = inputOntology;
		this.termReviewTermCategorization = termReviewTermCategorization;
		this.termReviewSynonyms = termReviewSynonyms;
		this.taxonGroup = taxonGroup;
	}
	
	@Override
	public Void call() throws MatrixGenerationException {
		try {
			List<String> knowsFilePaths = (List<String>) Arrays.asList(inputOntology.split("#"));
			EnhanceRun enhance = new EnhanceRun(inputDir, outputDir, knowsFilePaths, 
					termReviewTermCategorization, termReviewSynonyms, TaxonGroup.valueOf(taxonGroup));
			enhance.run();
			
			executedSuccessfully = true;
		} catch(Throwable e) {
			log(LogLevel.ERROR, "Matrix generation:enhance failed with exception.", e);
			executedSuccessfully = false;
		}
		if(!isExecutedSuccessfully()) {
			throw new MatrixGenerationException();
		}
		return null;
	}

	@Override
	public void destroy() { }

	@Override
	public boolean isExecutedSuccessfully() {
		return executedSuccessfully;
	}

	/**/
	public static void main(String[] args) throws Exception {
		//MatrixGeneration mg = new MatrixGeneration("C:/test/users/1070/input_2", "C:/test/temp/matrixGeneration/124/Matrix.mx");
		//InJvmEnhance mg = new InJvmEnhance("C:/etcsitebase/etcsite/data/users/4/smicropie_demo_output_by_TC_task_micropiedemo", "C:/test/Test_mmm.mx", "",
		//		"", "", "PROKARYOTES");
		String inputDir= "C:/Users/hongcui/Lorena/Farjon_parsed0712_hong_local_parse";
		String outputDir="C:/Users/hongcui/Lorena/Farjon_parsed0712_hong_local_parse_enhance";
		String inputOntology=""; 
		String termReviewTermCategorization="C:/Users/hongcui/Lorena/category_term-task-Farjon.csv";
		String termReviewSynonyms="C:/Users/hongcui/Lorena/category_mainterm_synonymterm-task-Farjon.csv";
		String taxonGroup = "PLANT";
		InJvmEnhance mg = new InJvmEnhance(inputDir, outputDir, inputOntology, termReviewTermCategorization, termReviewSynonyms, taxonGroup);
		mg.call();
		
	}
}
