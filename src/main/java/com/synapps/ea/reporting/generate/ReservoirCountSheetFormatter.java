package com.synapps.ea.reporting.generate;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.synapps.ea.rest.model.UserSearchCriterion;

public class ReservoirCountSheetFormatter extends AbstractSheetFormatter<String> {

	public ReservoirCountSheetFormatter() {
		setTag("#reservoircount");
	}

	@Override
	protected String formatCellContents(String cellContents, String data) 
			throws JsonParseException, JsonMappingException, IOException {

		//StringBuilder sb = new StringBuilder();		
		//return sb.toString();
		
		return data;
	}
	
}

