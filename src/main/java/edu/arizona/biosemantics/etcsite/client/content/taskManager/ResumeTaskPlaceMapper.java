package edu.arizona.biosemantics.etcsite.client.content.taskManager;

import com.google.gwt.place.shared.Place;

import edu.arizona.biosemantics.etcsite.client.content.home.HomePlace;
import edu.arizona.biosemantics.etcsite.client.content.matrixGeneration.MatrixGenerationInputPlace;
import edu.arizona.biosemantics.etcsite.client.content.matrixGeneration.MatrixGenerationOutputPlace;
import edu.arizona.biosemantics.etcsite.client.content.matrixGeneration.MatrixGenerationProcessPlace;
import edu.arizona.biosemantics.etcsite.client.content.matrixGeneration.MatrixGenerationReviewPlace;
import edu.arizona.biosemantics.etcsite.client.content.semanticMarkup.SemanticMarkupInputPlace;
import edu.arizona.biosemantics.etcsite.client.content.semanticMarkup.SemanticMarkupLearnPlace;
import edu.arizona.biosemantics.etcsite.client.content.semanticMarkup.SemanticMarkupOutputPlace;
import edu.arizona.biosemantics.etcsite.client.content.semanticMarkup.SemanticMarkupParsePlace;
import edu.arizona.biosemantics.etcsite.client.content.semanticMarkup.SemanticMarkupPreprocessPlace;
import edu.arizona.biosemantics.etcsite.client.content.semanticMarkup.SemanticMarkupReviewPlace;
import edu.arizona.biosemantics.etcsite.client.content.taxonomyComparison.TaxonomyComparisonAlignPlace;
import edu.arizona.biosemantics.etcsite.client.content.taxonomyComparison.TaxonomyComparisonInputPlace;
import edu.arizona.biosemantics.etcsite.client.content.taxonomyComparison.TaxonomyComparisonPlace;
import edu.arizona.biosemantics.etcsite.client.content.treeGeneration.TreeGenerationInputPlace;
import edu.arizona.biosemantics.etcsite.client.content.treeGeneration.TreeGenerationViewPlace;
import edu.arizona.biosemantics.etcsite.client.content.visualization.VisualizationPlace;
import edu.arizona.biosemantics.etcsite.shared.model.Task;

public class ResumeTaskPlaceMapper {
	
	public Place getPlace(Task task) {
		switch(task.getTaskType().getTaskTypeEnum()) {
		case SEMANTIC_MARKUP:
			switch(edu.arizona.biosemantics.etcsite.shared.model.semanticmarkup.TaskStageEnum.valueOf(task.getTaskStage().getTaskStage())) {
				case INPUT:
					return new SemanticMarkupInputPlace();
				case PREPROCESS_TEXT:
					return new SemanticMarkupPreprocessPlace(task);
				case LEARN_TERMS:
					return new SemanticMarkupLearnPlace(task);
				case REVIEW_TERMS:
					return new SemanticMarkupReviewPlace(task);
				/*case TO_ONTOLOGIES:
					return new SemanticMarkupToOntologiesPlace(task);
				case HIERARCHY:
					return new SemanticMarkupToOntologiesPlace(task);
				case ORDERS:
					return new SemanticMarkupToOntologiesPlace(task); */
				case PARSE_TEXT:
					return new SemanticMarkupParsePlace(task);
				case OUTPUT:
					return new SemanticMarkupOutputPlace(task);
			}
			return new SemanticMarkupInputPlace();
		case MATRIX_GENERATION:
			switch(edu.arizona.biosemantics.etcsite.shared.model.matrixgeneration.TaskStageEnum.valueOf(task.getTaskStage().getTaskStage())) {
				case INPUT:
					return new MatrixGenerationInputPlace();
				case PROCESS:
					return new MatrixGenerationProcessPlace(task);
				case REVIEW:
					return new MatrixGenerationReviewPlace(task);
				case OUTPUT:
					return new MatrixGenerationOutputPlace(task);
			}
			return new MatrixGenerationInputPlace();
		case TREE_GENERATION:
			switch(edu.arizona.biosemantics.etcsite.shared.model.treegeneration.TaskStageEnum.valueOf(task.getTaskStage().getTaskStage())) {
				case INPUT:
					return new TreeGenerationInputPlace();
				case VIEW:
					return new TreeGenerationViewPlace(task);
			}
			return new TreeGenerationInputPlace();
		case TAXONOMY_COMPARISON:
			switch(edu.arizona.biosemantics.etcsite.shared.model.taxonomycomparison.TaskStageEnum.valueOf(task.getTaskStage().getTaskStage())) {
			case INPUT:
				return new TaxonomyComparisonInputPlace();
			case ALIGN:
				return new TaxonomyComparisonAlignPlace(task);
			case ANALYZE:
				return new TaxonomyComparisonAlignPlace(task);
			case ANALYZE_COMPLETE:
				return new TaxonomyComparisonAlignPlace(task);
			default:
				break;
		}
			return new TaxonomyComparisonPlace();
		case VISUALIZATION:
			return new VisualizationPlace();
		}
		return new HomePlace();
	}
	
}	
