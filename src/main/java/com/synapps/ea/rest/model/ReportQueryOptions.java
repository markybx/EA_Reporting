package com.synapps.ea.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.synapps.ea.reporting.dql.xml.Column;
import com.synapps.ea.reporting.dql.xml.Condition;
import com.synapps.ea.reporting.dql.xml.Query;

/**
 * @author Mark Billingham
 *
 */
public class ReportQueryOptions implements Serializable {
	private static final long serialVersionUID = -4393729882454673440L;
	private String reportName;
	private String title;

	protected List<Column> selectOptions;
	protected List<UserSearchCriterion> conditions;

	
	public ReportQueryOptions() {
		super();	
	}
	
	public ReportQueryOptions(Query templateQuery) {
		super();
		getSelectOptions().addAll(templateQuery.getSelect().getColumns());
		title = templateQuery.getDescription();
	}
	
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Column> getSelectOptions() {
		if (null == selectOptions) {
			selectOptions = new ArrayList<Column>();
		}
		return selectOptions;
	}

	public List<UserSearchCriterion> getConditions() {
		if (null == conditions) {
			conditions = new ArrayList<UserSearchCriterion>();
		}
		return conditions;
	}

}
