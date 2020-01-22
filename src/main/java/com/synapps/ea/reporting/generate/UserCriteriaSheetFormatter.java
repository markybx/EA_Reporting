package com.synapps.ea.reporting.generate;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.synapps.ea.rest.model.UserSearchCriterion;

public class UserCriteriaSheetFormatter extends AbstractSheetFormatter<List<UserSearchCriterion>> {

	public UserCriteriaSheetFormatter() {
		setTag("#searchcriteria");
	}

	@Override
	protected String formatCellContents(String cellContents, List<UserSearchCriterion> data) 
			throws JsonParseException, JsonMappingException, IOException {

		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (UserSearchCriterion searchCriterion : data) {
			if (0 < i++) {
				sb.append(" ").append(searchCriterion.getLogicOp()).append(" ");
			}
			if (Boolean.TRUE.equals(searchCriterion.getGroupBegin())) {
				sb.append("(");
			}
			if (Boolean.TRUE.equals(searchCriterion.getAnyValue())) {
				sb.append(" ANY ");
			}
			sb.append(searchCriterion.getDescription()).append(" ").append(searchCriterion.getCompareOp()).append(" ").append(searchCriterion.getRightValue());
			if (Boolean.TRUE.equals(searchCriterion.getGroupEnd())) {
				sb.append(")");
			}
		}
			
		return sb.toString();
	}
	
}
