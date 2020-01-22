package com.synapps.ea.reporting.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.synapps.ea.reporting.generate.ReportGenerator.ColumnRow;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class SheetUtils {
	public static List<Cell> findCells(Sheet sheet, String patternStr, List<Cell> cellList) {
		if (null == cellList) {
			cellList = new ArrayList<Cell>();
		}
		Pattern pattern = Pattern.compile(patternStr);
		final int numCols = sheet.getColumns();
		final int numRows = sheet.getRows();
		for (int curRow = 0; curRow <= numRows; curRow++) {
			for (int curCol = 0; curCol <= numCols; curCol++) {
				Cell cell = sheet.getCell(curCol, curRow);
				if (cell.getType() != CellType.EMPTY) {
					Matcher m = pattern.matcher(cell.getContents());
					if (m.matches()) {
						cellList.add(cell);
					}
				}
			}
		}

		return cellList;
	}

	public static ColumnRow copyCells(WritableSheet fromSheet, WritableSheet toSheet, ColumnRow start, ColumnRow maxExtent)
			throws RowsExceededException, WriteException {
		int nRows = fromSheet.getRows();
		int nCols = fromSheet.getColumns();

		for (int row = 0; row < nRows; row++) {

			for (int col = 0; col < nCols; col++) {
				WritableCell cell;
				cell = (WritableCell) fromSheet.getCell(col, row);
				int toCol = col + start.getColumn();
				int toRow = row + start.getRow(); 
				WritableCell newCell = cell.copyTo(toCol, toRow);
				if (toCol > maxExtent.getColumn()) {
//					toSheet.insertColumn(toCol);
					maxExtent.setColumn(toCol);
				}
				if (toRow > maxExtent.getRow()) {
					toSheet.insertRow(toRow);
					maxExtent.setRow(toRow);
				}
				toSheet.addCell(newCell);
			}
		}
		return maxExtent;
	}


}
