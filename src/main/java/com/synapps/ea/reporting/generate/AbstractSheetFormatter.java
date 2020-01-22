package com.synapps.ea.reporting.generate;

import java.io.IOException;
import java.util.List;

import jxl.Cell;
import jxl.write.Label;
import jxl.write.WritableSheet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractSheetFormatter<T> {
	protected String tag;
	protected ObjectMapper mapper = new ObjectMapper();

	public void format(WritableSheet sheet, T data) 
			throws JsonParseException, JsonMappingException, IOException {

		String searchExpr = getTag() + ".*";
		List<Cell> cells = SheetUtils.findCells(sheet, searchExpr, null);
		for (Cell cell : cells) {
			if (cell instanceof Label) {
				Label label = (Label) cell;
				String cellContents = label.getContents();
				String formattedContents = formatCellContents(cellContents, data);
				label.setString(formattedContents);
			}
		}

	}
	
	protected abstract String formatCellContents(String cellContents, T data) 
			throws JsonParseException, JsonMappingException, IOException;

	protected void setTag(String tag) {
		this.tag = tag;
	}

	protected String getTag() {
		return this.tag;
	}
	
}
