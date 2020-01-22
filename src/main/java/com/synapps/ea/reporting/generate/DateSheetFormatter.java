package com.synapps.ea.reporting.generate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

public class DateSheetFormatter extends AbstractSheetFormatter<Date> {
	private DateFormat defaultDateFormat;
	
	public DateSheetFormatter(DateFormat dateFormat) {
		setTag("#date");
		this.defaultDateFormat = dateFormat;
	}

	@Override
	protected String formatCellContents(String cellContents, Date date) 
			throws JsonParseException, JsonMappingException, IOException {

		String modifier = cellContents.substring(getTag().length());
		DateFormat dateFormat = this.defaultDateFormat;
		if (! modifier.isEmpty()) {
			Map<String, String> map = mapper.readValue(modifier, new TypeReference<HashMap<String, String>>(){});
			String format = map.get("format");
			if (null != format) {
				dateFormat = new SimpleDateFormat(format);
			}
		}
		return dateFormat.format(date);
	}

}
