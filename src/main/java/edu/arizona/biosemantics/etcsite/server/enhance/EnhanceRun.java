package edu.arizona.biosemantics.etcsite.server.enhance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import au.com.bytecode.opencsv.CSVReader;
import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.ling.know.ICharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.IGlossary;
import edu.arizona.biosemantics.common.ling.know.SingularPluralProvider;
import edu.arizona.biosemantics.common.ling.know.Term;
import edu.arizona.biosemantics.common.ling.know.lib.GlossaryBasedCharacterKnowledgeBase;
import edu.arizona.biosemantics.common.ling.know.lib.InMemoryGlossary;
import edu.arizona.biosemantics.common.ling.know.lib.WordNetPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.IInflector;
import edu.arizona.biosemantics.common.ling.transform.ITokenizer;
import edu.arizona.biosemantics.common.ling.transform.lib.SomeInflector;
import edu.arizona.biosemantics.common.ling.transform.lib.WhitespaceTokenizer;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.etcsite.server.rpc.matrixgeneration.Enhance;
import edu.arizona.biosemantics.etcsite.server.rpc.matrixgeneration.InJvmEnhance;
import edu.arizona.biosemantics.oto.client.oto.OTOClient;
import edu.arizona.biosemantics.oto.model.GlossaryDownload;
import edu.arizona.biosemantics.oto.model.TermCategory;
import edu.arizona.biosemantics.oto.model.TermSynonym;
import edu.arizona.biosemantics.oto.model.lite.Decision;
import edu.arizona.biosemantics.oto.model.lite.Download;
import edu.arizona.biosemantics.oto.model.lite.Synonym;
import edu.arizona.biosemantics.semanticmarkup.enhance.config.Configuration;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.KnowsSynonyms.SynonymSet;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.CSVKnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.CSVKnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.JustKnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.OWLOntologyKnowsPartOf;
import edu.arizona.biosemantics.semanticmarkup.enhance.know.lib.OWLOntologyKnowsSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.run.Run;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.AbstractTransformer;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseBiologicalEntities;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseBiologicalEntityToName;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseCharacterToValue;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CollapseCharacters;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.CreateOrPopulateWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.MoveNegationOrAdverbBiologicalEntityConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.OrderBiologicalEntityConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveDuplicateValues;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByBackwardConnectors;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByForwardConnectors;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByPassedParents;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveNonSpecificBiologicalEntitiesByRelations;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveOrphanRelations;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveUselessCharacterConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RemoveUselessWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.RenameCharacter;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.ReplaceTaxonNameByWholeOrganism;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SimpleRemoveSynonyms;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SortBiologicalEntityNameWithDistanceCharacter;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SplitCompoundBiologicalEntitiesCharacters;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.SplitCompoundBiologicalEntity;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.StandardizeCount;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.StandardizeQuantityPresence;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.StandardizeUnits;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.MoveCharacterToStructureConstraint;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeStructureNameBySyntax;
import edu.arizona.biosemantics.semanticmarkup.enhance.transform.old.StandardizeTerminology;

public class EnhanceRun {

	private String negWords = "no|not|never";
	private String advModifiers = "at least|at first|at times";
	private String stopWords = "a|about|above|across|after|along|also|although|amp|an|and|are|as|at|be|because|become|becomes|becoming|been|before|being|"
			+ "beneath|between|beyond|but|by|ca|can|could|did|do|does|doing|done|for|from|had|has|have|hence|here|how|however|if|in|into|inside|inward|is|it|its|"
			+ "may|might|more|most|near|of|off|on|onto|or|out|outside|outward|over|should|so|than|that|the|then|there|these|this|those|throughout|"
			+ "to|toward|towards|up|upward|was|were|what|when|where|which|why|with|within|without|would";
	private String units = "(?:(?:pm|cm|mm|dm|ft|m|meters|meter|micro_m|micro-m|microns|micron|unes|µm|μm|um|centimeters|centimeter|millimeters|millimeter|transdiameters|transdiameter)[23]?)"; //squared or cubed
	private TaxonGroup taxonGroup;
	private WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase = new WordNetPOSKnowledgeBase(Configuration.wordNetDirectory, false);
	private IGlossary glossary = new InMemoryGlossary();
	private HashMap<String, String> renames;
	private SingularPluralProvider singularPluralProvider = new SingularPluralProvider();
	private IInflector inflector = new SomeInflector(wordNetPOSKnowledgeBase, singularPluralProvider.getSingulars(), singularPluralProvider.getPlurals());
	private ITokenizer tokenizer = new WhitespaceTokenizer();
	private Set<String> lifeStyles;
	private GlossaryBasedCharacterKnowledgeBase characterKnowledgeBase;
	private Set<String> possessionTerms = getWordSet("with|has|have|having|possess|possessing|consist_of");
	private Set<String> durations;
	private String input;
	private String output;
	private List<String> filePath2KnowsPartOf; //.owl or .csv
	private String termReviewTermCategorization;
	private String termReviewSynonyms;
	
	public EnhanceRun(String input, String output, List<String> filePath2KnowsPartOf, 
			String termReviewTermCategorization, String termReviewSynonyms, TaxonGroup taxonGroup) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		this.input = input;
		this.output = output;
		this.filePath2KnowsPartOf = filePath2KnowsPartOf;
		this.termReviewTermCategorization = termReviewTermCategorization;
		this.termReviewSynonyms = termReviewSynonyms;
		this.taxonGroup = taxonGroup;
		
		initGlossary(glossary, inflector, taxonGroup, termReviewTermCategorization, termReviewSynonyms);
		
		renames = new HashMap<String, String>();
		renames.put("count", "quantity");
		renames.put("atypical_count", "atypical_quantity");
		renames.put("color", "coloration");
		
		lifeStyles = glossary.getWordsInCategory("life_style");
		lifeStyles.addAll(glossary.getWordsInCategory("growth_form"));
		durations = glossary.getWordsInCategory("duration");
		
		characterKnowledgeBase = new GlossaryBasedCharacterKnowledgeBase(glossary, negWords, advModifiers, stopWords, units, inflector);
		
	}
	
	public void run() throws OWLOntologyCreationException {
		//System.out.println("run --"+"doenhance");
		//System.out.println("ontology --"+filePath2KnowsPartOf);
		Run run = new Run();
		ArrayList<String> ontologies = new ArrayList<String>();
		ArrayList<String> csvs = new ArrayList<String>();
		try{
			//when knowledge entities can be constructed, use them for certain enhancement transformations
			for(String filePath: filePath2KnowsPartOf){
				if(filePath.endsWith(".owl")){
					ontologies.add(filePath);
				}else if(filePath.endsWith(".csv")){
					csvs.add(filePath);
				}
			}
			
			KnowsSynonyms knowsSynonyms = new CSVKnowsSynonyms(termReviewSynonyms, inflector);
			KnowsPartOf knowsPartOf = new JustKnowsPartOf(csvs, ontologies, knowsSynonyms, inflector); 
			
			RemoveNonSpecificBiologicalEntitiesByRelations transformer1 = new RemoveNonSpecificBiologicalEntitiesByRelations(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByBackwardConnectors transformer2 = new RemoveNonSpecificBiologicalEntitiesByBackwardConnectors(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByForwardConnectors transformer3 = new RemoveNonSpecificBiologicalEntitiesByForwardConnectors(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName());
			RemoveNonSpecificBiologicalEntitiesByPassedParents transformer4 = new RemoveNonSpecificBiologicalEntitiesByPassedParents(
					knowsPartOf, knowsSynonyms, tokenizer, new CollapseBiologicalEntityToName(), inflector);
			
			run.addTransformer(new SimpleRemoveSynonyms(knowsSynonyms));
			run.addTransformer(transformer1);
			run.addTransformer(transformer2);
			run.addTransformer(transformer3);
			run.addTransformer(transformer4);
		}catch(Exception e){
			//when they are not available.
			log(LogLevel.DEBUG, "Reduced enhancement due to unavailability of knowledge entities for advanced enhancements:", e);
		}	

		run.addTransformer(new SplitCompoundBiologicalEntity(inflector));
		run.addTransformer(new SplitCompoundBiologicalEntitiesCharacters(inflector));
		run.addTransformer(new RemoveUselessWholeOrganism());
		run.addTransformer(new RemoveUselessCharacterConstraint());
		run.addTransformer(new RenameCharacter(renames));
		run.addTransformer(new MoveCharacterToStructureConstraint());
		run.addTransformer(new MoveNegationOrAdverbBiologicalEntityConstraint(wordNetPOSKnowledgeBase));
		run.addTransformer(new ReplaceTaxonNameByWholeOrganism());
		run.addTransformer(new CreateOrPopulateWholeOrganism(lifeStyles, "growth_form"));
		run.addTransformer(new CreateOrPopulateWholeOrganism(durations, "duration"));
		run.addTransformer(new StandardizeQuantityPresence());
		run.addTransformer(new StandardizeCount());
		run.addTransformer(new SortBiologicalEntityNameWithDistanceCharacter());
		run.addTransformer(new OrderBiologicalEntityConstraint());
		run.addTransformer(new StandardizeStructureNameBySyntax(characterKnowledgeBase, possessionTerms));
		//run.addTransformer(new StandardizeStructureNameTest(characterKnowledgeBase, possessionTerms));
		run.addTransformer(new StandardizeTerminology(characterKnowledgeBase));
		run.addTransformer(new RemoveOrphanRelations());
		run.addTransformer(new RemoveDuplicateValues());
		run.addTransformer(new StandardizeUnits());
		run.addTransformer(new CollapseBiologicalEntityToName());
		run.addTransformer(new CollapseCharacterToValue());
		run.addTransformer(new CollapseBiologicalEntities());
		run.addTransformer(new CollapseCharacters());

		
		run.run(new File(input), new File(output));
	}



	private static Set<String> getWordSet(String regexString) {
		Set<String> set = new HashSet<String>();
		String[] wordsArray = regexString.split("\\|");
		for (String word : wordsArray)
			set.add(word.toLowerCase().trim());
		return set;
	}

	private void initGlossary(IGlossary glossary, IInflector inflector, TaxonGroup taxonGroup, String termReviewTermCategorization, String termReviewSynonyms) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		addPermanentGlossary(glossary, inflector, taxonGroup);
		addTermReviewGlossary(glossary, inflector, termReviewTermCategorization, termReviewSynonyms);
	}

	private void addTermReviewGlossary(IGlossary glossary,	IInflector inflector, String termReviewTermCategorization,	String termReviewSynonyms) throws IOException {
		List<Synonym> synonyms = new LinkedList<Synonym>();
		Set<String> hasSynonym = new HashSet<String>();
		
		if(termReviewSynonyms != null && new File(termReviewSynonyms).exists()) {
			try(CSVReader reader = new CSVReader(new FileReader(termReviewSynonyms))) {
				List<String[]> lines = reader.readAll();
				int i=0;
				for(String[] line : lines) {
					synonyms.add(new Synonym(String.valueOf(i), line[1], line[0], line[2]));
					hasSynonym.add(line[1]);
				}	
			}
		}
		
		if(termReviewTermCategorization != null && new File(termReviewTermCategorization).exists()) {
			try(CSVReader reader = new CSVReader(new FileReader(termReviewTermCategorization))) {
				List<String[]> lines = reader.readAll();
				List<Decision> decisions = new LinkedList<Decision>();
				int i=0;
				for(String[] line : lines) {
					decisions.add(new Decision(String.valueOf(i), line[1], line[0], hasSynonym.contains(line[1]), ""));
				}
				Download download = new Download(true, decisions, synonyms);
				
				//add syn set of term_category
				HashSet<Term> dsyns = new HashSet<Term>();
				if(download != null) {
					for(Synonym termSyn: download.getSynonyms()){
						//Hong TODO need to add category info to synonym entry in OTOLite
						//if(termSyn.getCategory().compareTo("structure")==0){
						if(termSyn.getCategory().matches("structure|taxon_name|substance")){
							//take care of singular and plural forms
							String syns = ""; 
							String synp = "";
							String terms = "";
							String termp = "";
							if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){
								synp = termSyn.getSynonym().replaceAll("_",  "-");
								syns = inflector.getSingular(synp);					
							}else{
								syns = termSyn.getSynonym().replaceAll("_",  "-");
								synp = inflector.getPlural(syns);
							}
				
							if(inflector.isPlural(termSyn.getTerm().replaceAll("_",  "-"))){
								termp = termSyn.getTerm().replaceAll("_",  "-");
								terms = inflector.getSingular(termp);					
							}else{
								terms = termSyn.getTerm().replaceAll("_",  "-");
								termp = inflector.getPlural(terms);
							}
							//glossary.addSynonym(syns, termSyn.getCategory(), terms);
							//glossary.addSynonym(synp, termSyn.getCategory(), termp);
							//dsyns.add(new Term(syns, termSyn.getCategory());
							//dsyns.add(new Term(synp, termSyn.getCategory());
							glossary.addSynonym(syns, termSyn.getCategory(), terms);
							glossary.addSynonym(synp,termSyn.getCategory(), termp);
							dsyns.add(new Term(syns, termSyn.getCategory()));
							dsyns.add(new Term(synp, termSyn.getCategory()));
						}else{//forking_1 and forking are syns 5/5/14 hong test, shouldn't _1 have already been removed?
							glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
							dsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory()));
						}					
					}
				
					//term_category from OTO, excluding dsyns
					for(Decision decision : download.getDecisions()) {
						if(!dsyns.contains(new Term(decision.getTerm().replaceAll("_",  "-"), decision.getCategory())))//calyx_tube => calyx-tube
							glossary.addEntry(decision.getTerm().replaceAll("_",  "-"), decision.getCategory());  
					}
				}
			}
		}
	}

	private void addPermanentGlossary(IGlossary glossary, IInflector inflector, TaxonGroup taxonGroup) throws InterruptedException,IOException, ClassNotFoundException, ExecutionException {
		/*OTOClient otoClient = new OTOClient("http://biosemantics.arizona.edu:8080/OTO");
		GlossaryDownload glossaryDownload = new GlossaryDownload();		
		String glossaryVersion = "latest";
		otoClient.open();
		Future<GlossaryDownload> futureGlossaryDownload = otoClient.getGlossaryDownload(taxonGroup.getDisplayName(), glossaryVersion);
		glossaryDownload = futureGlossaryDownload.get();
		otoClient.close();*/
				
		ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(Configuration.glossariesDownloadDirectory + File.separator +
				"GlossaryDownload." + taxonGroup.getDisplayName() + ".ser"));
		GlossaryDownload glossaryDownload = (GlossaryDownload) objectIn.readObject();
		objectIn.close();
		
		//add the syn set of the glossary
		HashSet<Term> gsyns = new HashSet<Term>();
		for(TermSynonym termSyn : glossaryDownload.getTermSynonyms()) {
		
			//if(termSyn.getCategory().compareTo("structure")==0){
			if(termSyn.getCategory().matches("structure|taxon_name|substance")) {
				//take care of singular and plural forms
				String syns = ""; 
				String synp = "";
				String terms = "";
				String termp = "";
				if(inflector.isPlural(termSyn.getSynonym().replaceAll("_",  "-"))){ //must convert _ to -, as matching entity phrases will be converted from leg iii to leg-iii in the sentence.
					synp = termSyn.getSynonym().replaceAll("_",  "-");
					syns = inflector.getSingular(synp);					
				} else {
					syns = termSyn.getSynonym().replaceAll("_",  "-");
					synp = inflector.getPlural(syns);
				}
		
				if(inflector.isPlural(termSyn.getTerm().replaceAll("_",  "-"))){
					termp = termSyn.getTerm().replaceAll("_",  "-");
					terms = inflector.getSingular(termp);					
				}else{
					terms = termSyn.getTerm().replaceAll("_",  "-");
					termp = inflector.getPlural(terms);
				}
				glossary.addSynonym(syns, termSyn.getCategory(), terms);
				glossary.addSynonym(synp, termSyn.getCategory(), termp);
				gsyns.add(new Term(syns, termSyn.getCategory()));
				gsyns.add(new Term(synp, termSyn.getCategory()));
			} else {
				//glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), "arrangement", termSyn.getTerm());
				glossary.addSynonym(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory(), termSyn.getTerm());
				gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), termSyn.getCategory()));
				//gsyns.add(new Term(termSyn.getSynonym().replaceAll("_",  "-"), "arrangement"));
			}
		}
	
		//the glossary, excluding gsyns
		for(TermCategory termCategory : glossaryDownload.getTermCategories()) {
			if(!gsyns.contains(new Term(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory())))
				glossary.addEntry(termCategory.getTerm().replaceAll("_", "-"), termCategory.getCategory()); //primocane_foliage =>primocane-foliage Hong 3/2014
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, ClassNotFoundException, OWLOntologyCreationException {
		//String inputDir = "C:/etcsitebase/etcsite/data/users/4/plant_output_by_TC_task_Plant3files_07-03-2017_1";
		//String enhanceDir = "C:/etcsitebase/etcsite/data/matrixGeneration/432/enhance";
		//String inputOntology = "C:/Users/rodenhausen.CATNET/Desktop/etcsite/data/users/1/1_output_by_TC_task_2_output_by_OB_task_3/3.owl";
		//String termReviewTermCategorization = "C:/Users/rodenhausen.CATNET/Desktop/etcsite/data/users/1/1_TermsReviewed_by_TC_task_2/category_term-task-2.csv";
		//String termReviewSynonyms = "C:/Users/rodenhausen.CATNET/Desktop/etcsite/data/users/1/1_TermsReviewed_by_TC_task_2/category_mainterm_synonymterm-task-2.csv";
		
		String inputDir = "C:\\Users\\hongcui\\Documents\\etc-development\\enhance\\in";
		String enhanceDir ="C:\\Users\\hongcui\\Documents\\etc-development\\enhance\\out";
		String inputOntology = "C:\\Users\\hongcui\\Documents\\etc-development\\enhance\\Structure224.owl";
		String partOfCSV = "C:\\Users\\hongcui\\Documents\\etc-development\\enhance\\part_of.csv";
		String termReviewTermCategorization = "C:\\Users\\hongcui\\Documents\\etc-development\\enhance\\terms.csv";
		String termReviewSynonyms = "C:\\Users\\hongcui\\Documents\\etc-development\\enhance\\synonym.csv";
		
		ArrayList<String> knowsPartOf = new ArrayList<String>();
		knowsPartOf.add(inputOntology);
		knowsPartOf.add("C:/Users/hongcui/Documents/etcsite/resources/shared/ontologies/po.owl");
		knowsPartOf.add(partOfCSV);

		String taxonGroup = TaxonGroup.PLANT.toString();
		
		EnhanceRun enhance = new EnhanceRun(inputDir, enhanceDir, knowsPartOf, 
				termReviewTermCategorization, termReviewSynonyms, TaxonGroup.valueOf(taxonGroup));
		enhance.run();
	}
}
