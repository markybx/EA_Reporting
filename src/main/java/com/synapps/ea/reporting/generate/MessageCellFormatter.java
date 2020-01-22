package com.synapps.ea.reporting.generate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MessageCellFormatter extends AbstractSheetFormatter<List<List<Object>>> {

	public MessageCellFormatter() {
		super();
		setTag("#message");
	}

	@Override
	protected String formatCellContents(String cellContents, List<List<Object>> data)
			throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		String message = "";
		
		String modifier = cellContents.substring(getTag().length());
		if (! modifier.isEmpty()) {
			Map<String, String> map = mapper.readValue(modifier, new TypeReference<HashMap<String, String>>(){});
			String show = map.get("show");
			String hide = map.get("hide");
			boolean matchFound = false;
			
			String condition = null;
			if (null != show) {
				condition = show;
			} else if (null != hide) {
				condition =  hide;
			}
			if (null != show || null != hide) {
				
				StringTokenizer st = new StringTokenizer(condition, "'", false);
				
				String[] tokens = new String[3];
				
				for (int tokenIdx = 0; st.hasMoreTokens() && tokenIdx < 3; tokenIdx++) {
					tokens[tokenIdx] = st.nextToken();
				}
				String colName = tokens[0];
				String operator = null != tokens[1] ? tokens[1].trim() : null;
				String value = tokens[2];
				List<Object> headers = data.get(0);
				int colIdx = headers.indexOf(colName);
				
				if (colIdx > -1) {
					for (int rowIdx = 1; rowIdx < data.size(); rowIdx++) {
						List<Object> rowData = data.get(rowIdx);
						Object cellData = rowData.get(colIdx);
						if (getMatches(cellData, operator, value)) {
							matchFound =  true;
							break;
						}
					}
				}
			}

			if (map.containsKey("text")) {
				
				if (null != show && matchFound) {
					message = map.get("text");
				} else if (null != hide && ! matchFound) {
					message = map.get("text");
				}
			}
		}
		
		return message;
	}

	private boolean getMatches(Object cellData, String operator, String value) {
		if ("EQUALS".equalsIgnoreCase(operator)) {
			return cellData.equals(value);
			
		} else if ("CONTAINS".equalsIgnoreCase(operator)) {
			Pattern pat = Pattern.compile(value);
			Matcher mat = pat.matcher(cellData.toString());
			return mat.find();
		}
		return false;
	}


}
