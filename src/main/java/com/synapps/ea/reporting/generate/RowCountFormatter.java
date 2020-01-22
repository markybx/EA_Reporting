package com.synapps.ea.reporting.generate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

public class RowCountFormatter extends AbstractSheetFormatter <List<List<Object>>> {
	
	public RowCountFormatter() {
		super();
		setTag("#rowcount");
	}

	@Override
	protected String formatCellContents(String cellContents, List<List<Object>> data) 
			throws JsonParseException, JsonMappingException, IOException {
		
		String modifier = cellContents.substring(getTag().length());
		List<String> distinctList = null;
		if (! modifier.isEmpty()) {
			Map<String, List<String>> map = mapper.readValue(modifier, new TypeReference<HashMap<String, List<String>>>(){});
			distinctList = map.get("distinct");
		}
		List<Object> headers = data.get(0);
		int count = 0;
		Set<Object> distinctValues = new HashSet<Object>();
		for (int row = 1; row < data.size(); row++) {
			if (null != distinctList) {
				List<Object> rowValues = data.get(row);
				List<Object> valueList = new ArrayList<Object>();
				for (String distinctCol : distinctList) {
					int col = headers.indexOf(distinctCol);
					if (col > -1) {
						valueList.add(rowValues.get(col));
					}
				}
				if (! valueList.isEmpty()) {
					if (! distinctValues.contains(valueList)) {
						distinctValues.add(valueList);
						count++;
					}
				} else {
					count++;
				}
			} else {
				count++;
			}
		}
		return String.valueOf(count);
	}

}
