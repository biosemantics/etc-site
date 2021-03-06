package edu.arizona.biosemantics.etcsite.shared.model;

import java.io.Serializable;
import java.util.Date;

import edu.arizona.biosemantics.etcsite.shared.model.taxonomycomparison.TaskStageEnum;
import com.google.gwt.user.client.rpc.IsSerializable;
public class TaxonomyComparisonTaskStage extends TaskStage implements Serializable, IsSerializable {

	private TaskStageEnum taskStage;

	public TaxonomyComparisonTaskStage() { }
	
	public TaxonomyComparisonTaskStage(int id, TaskType taskType, Date created, TaskStageEnum taskStage) {
		super(id, taskType, created);
		this.taskStage = taskStage;
	}

	public void setTaskStage(TaskStageEnum taskStage) {
		this.taskStage = taskStage;
	}
	
	@Override
	public int getTaskStageNumber() {
		int j=0;
		for (TaskStageEnum step : TaskStageEnum.values()) {
			j++;
			if (step.equals(this.taskStage)) {
				return j;
			}
		}
		return -1;
	}

	@Override
	public int getMaxTaskStageNumber() {
		return TaskStageEnum.values().length;
	}
	
	public TaskStageEnum getTaskStageEnum() {
		return taskStage;
	}

	@Override
	public String getTaskStage() {
		return taskStage.toString();
	}
	
	@Override
	public String getDisplayName() {
		return taskStage.displayName();
	}
	
	@Override
	public int compareTo(TaskStage o) {
		if(o instanceof TaxonomyComparisonTaskStage) 
			return this.taskStage.compareTo(((TaxonomyComparisonTaskStage)o).taskStage);
		return 0;
	}
}
