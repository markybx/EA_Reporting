package com.synapps.ea.reporting.generate;

import static com.documentum.fc.client.IDfQuery.DF_READ_QUERY;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Blank;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.log4j.Logger;
import org.springframework.core.env.Environment;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapps.ea.reporting.dql.DqlQueryRenderer;
import com.synapps.ea.reporting.dql.xml.AttributeDataType;
import com.synapps.ea.reporting.dql.xml.Column;
import com.synapps.ea.reporting.dql.xml.Query;
import com.synapps.ea.reporting.query.ReportGeneratorQueryRunner;
import com.synapps.ea.reporting.util.BeanValuePlaceholderResolver;
import com.synapps.ea.rest.model.CreateReportResult;
import com.synapps.ea.rest.model.ReportQueryOptions;

/**
 * @author Mark Billingham
 *
 */
public class ReportGenerator {
	private Logger logger = Logger.getLogger(this.getClass());
	private Environment env;
	private String reportType;
	private String reportContent;
	private String aclName;
	private String folderAclName;
	private SimpleDateFormat reportLocationFormat;
	private SimpleDateFormat dateFieldFormat;
	private DateSheetFormatter dateSheetFormatter;
	private UserCriteriaSheetFormatter userCriteriaFormatter;
	private ReservoirCountSheetFormatter reservoirCountFormatter;
	private RowCountFormatter rowCountFormatter;
	private MessageCellFormatter messageCellFormatter;

	/**
	 * @param env
	 */
	public ReportGenerator(Environment env) {
		this.env = env;
		reportType = env.getProperty("report.document.type");
		reportContent = env.getProperty("report.content.type");
		aclName = env.getProperty("report.acl.name");
		folderAclName=env.getProperty("report.folder.acl.name");
		reportLocationFormat = new SimpleDateFormat(env.getProperty("report.location"));
		String dateFormat = env.getProperty("report.date.format", "dd/MM/yyyy");
		dateFieldFormat = new SimpleDateFormat(dateFormat);
		dateSheetFormatter = new DateSheetFormatter(dateFieldFormat);
		userCriteriaFormatter = new UserCriteriaSheetFormatter();
		reservoirCountFormatter = new ReservoirCountSheetFormatter();
		rowCountFormatter = new RowCountFormatter();
		messageCellFormatter = new MessageCellFormatter();
	}

	/**
	 * @param dfSession
	 * @param templateName
	 * @param templateQueryModel
	 * @param reportTitle
	 * @param result
	 * @throws DfException
	 * @throws JAXBException
	 * @throws WriteException
	 * @throws IOException
	 */
	public void generate(
			IDfSession dfSession, 
			String templateName, 
			Query templateQueryModel, 
			ReportQueryOptions userQueryOptions, 
			String reportTitle, 
			CreateReportResult result) 
					throws DfException, JAXBException, WriteException, IOException {

		SimpleDateFormat reportNameFormat = new SimpleDateFormat("'" + reportTitle + "_'HHmm");
		Date date = new Date();
		String reportLocation = reportLocationFormat.format(date);
		String reportName = reportNameFormat.format(date);
		Query reportQuery = new Query();
		reportQuery.getSelect().setDistinct(templateQueryModel.getSelect().getDistinct());
		reportQuery.getSelect().setColumns(userQueryOptions.getSelectOptions());
		reportQuery.getFrom().getTypes().addAll(templateQueryModel.getFrom().getTypes());
		reportQuery.getWhere().getConditons().addAll(templateQueryModel.getWhere().getConditons());
		reportQuery.getWhere().getConditons().addAll(userQueryOptions.getConditions());
		reportQuery.getOrderBy().getOrderBy().addAll(templateQueryModel.getOrderBy().getOrderBy());

		ReportGeneratorQueryRunner qr = new ReportGeneratorQueryRunner();

		DqlQueryRenderer renderer = new DqlQueryRenderer();
		String reportQueryDql = renderer.renderQuery(reportQuery);
		logger.info("Report generation query:\n" + reportQueryDql);
		List<List<Object>> reportData = qr.execute(dfSession, reportQueryDql, DF_READ_QUERY, new ArrayList<List<Object>>());
		// Insert Display Values for Headers
		List<Column> columns = reportQuery.getSelect().getColumns();
		List<Object> headers = reportData.get(0);
		for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
			Column column = columns.get(colIdx);
			String colDesc = column.getDescription();
			if (null != colDesc && ! colDesc.isEmpty()) {
				headers.set(colIdx, colDesc);
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Workbook workbookTemplate = getTemplateWorkbook(baos, dfSession, templateName);
		
		IDfDocument reportDoc = (IDfDocument) dfSession.newObject(reportType);
		try {
			if (null != workbookTemplate) {
				// Create report from template
				createWorkbookFromTemplate(workbookTemplate, baos, reportName, date, userQueryOptions, columns, reportData);
			} else {
				// Create a new vanilla report
				createReportWorkbook(baos, reportName, null, reportData);
			}
			createLocation(dfSession, reportLocation);
			reportDoc.setObjectName(reportName);
			reportDoc.setContentEx(baos, reportContent, 0);
		} finally {
			baos.close();
		}
		if (null != aclName && ! aclName.isEmpty()) {
			reportDoc.setACLName(aclName);
			reportDoc.setACLDomain("dm_dbo");
		}
		reportDoc.link(reportLocation);
		reportDoc.save();
		result.setName(reportName);
		result.setObjectType(reportType);
		result.setObjectId(reportDoc.getObjectId().getId());
		String viewerLocation = env.getProperty("report.viewer.location");
		if (null != viewerLocation) {
			String contentLocation = BeanValuePlaceholderResolver.resolvePlaceholders(viewerLocation, result);
			result.setLocation(contentLocation);
		}
	}

	private Workbook getTemplateWorkbook(OutputStream os, IDfSession dfSession, String templateName) throws DfException, IOException {
		String templateType = env.getProperty("output.template.type");
		String templateQual = templateType + " WHERE object_name='" + templateName + "'";
		IDfSysObject excelObj = (IDfSysObject) dfSession.getObjectByQualification(templateQual);
		Workbook workbook = null;
		if (null != excelObj) {
			InputStream is = excelObj.getContent();
			try {
				workbook = Workbook.getWorkbook(is);
			} catch (BiffException | IOException e) {
				logger.error("Error fetching template workbook", e);
			} finally {
				is.close();
			}
		}
		return workbook;
	}

	private WritableWorkbook createReportWorkbook(OutputStream os, String reportName, List<Column> columns, List<List<Object>> reportData)
			throws IOException, WriteException {
		WritableWorkbook workbook = null;

		try {
			workbook = Workbook.createWorkbook(os);
			WritableSheet sheet = workbook.createSheet(reportName, 0);
			List<Object> headers = reportData.get(0);
			// Write Header row
			int row = 0;
			for (int col=0; col < headers.size(); col++) {
				Label label = new Label(col, row,  headers.get(col).toString()); 
				sheet.addCell(label);
			};
			for (row = 1; row < reportData.size(); row++) {
				List<Object> values = reportData.get(row);

				for (int col = 0; col < values.size(); col++) {
					AttributeDataType dataType = columns.get(col).getDataType();
					//					Label label = new Label(col, row,  values.get(col).toString()); 
					WritableCell newCell = createCell(col, row, dataType, headers.get(col), sheet.getColumnView(col).getFormat());
					sheet.addCell(newCell);
				};
			}
			workbook.write(); 
			workbook.close();

		} catch (IOException e) {
			logger.error("Error creating workbook", e);
			throw e;
		} catch (RowsExceededException e) {
			logger.error("Error creating workbook", e);
			throw e;
		} catch (WriteException e) {
			logger.error("Error creating workbook", e);
			throw e;
		}
		return workbook;
	}

	private void createWorkbookFromTemplate(
			Workbook templateWorkbook, 
			OutputStream os, 
			String reportName, 
			Date creationDate, 
			ReportQueryOptions userQueryOptions, 
			List<Column> columns, 
			List<List<Object>> reportData) {
		Sheet templateSheet = templateWorkbook.getSheet(0);

		if (null == templateSheet) {
			return;
		}
		try {
			/* Fix for Index */
			WorkbookSettings wbSettings = new WorkbookSettings();
			wbSettings.setRationalization(false);
			WritableWorkbook workbook = Workbook.createWorkbook(os, wbSettings);
			WritableSheet writableSheet = workbook.importSheet(reportName, 0, templateSheet);
			populateSheet(writableSheet, creationDate, userQueryOptions, columns, reportData);
			//			int lastCol = writableSheet.getColumns();
			//			int lastRow = writableSheet.getRows();
			//			Pattern pattern = Pattern.compile("#template.*$");
			//			Cell templateCell = writableSheet.findCell(pattern, 0, 0, lastCol, lastRow, false);
			List<Cell> templateCells = SheetUtils.findCells(writableSheet, "#template.*$", null);
			//			if (null != templateCell) {
			ColumnRow maxExtent = new ColumnRow(0, 0);
			for (Cell templateCell : templateCells) {
				String templateSpec = templateCell.getContents().substring("#template".length());
				ObjectMapper mapper = new ObjectMapper();
				Map<String, String> templateMap = mapper.readValue(templateSpec, new TypeReference<HashMap<String,String>>(){});
				String sheetName = templateMap.get("sheet");
				templateSheet = templateWorkbook.getSheet(sheetName);
				if (null != templateSheet) {
					List<Object> columnHeaders = reportData.get(0);
					String keyAttribute = templateMap.get("forEach");
					int templateRow = templateCell.getRow();
					int templateCol = templateCell.getColumn();
					writableSheet.addCell(new Blank(templateCol, templateRow));
					if (templateRow > maxExtent.getRow()) {
						maxExtent.setRow(templateRow);
					}
					if (templateCol > maxExtent.getColumn()) {
						maxExtent.setColumn(templateCol);
					}
					if (null != keyAttribute) {
						int keyIndex = columnHeaders.indexOf(keyAttribute);
						if (keyIndex >= 0) {
							List<List<Object>> subReportData = new ArrayList <List<Object>>();
							subReportData.add(columnHeaders);
							Object keyValue = null;
							for (int dataRow = 1; dataRow < reportData.size(); dataRow++) {
								List<Object> rowData = reportData.get(dataRow);
								if (dataRow == 1) {
									keyValue = rowData.get(keyIndex);
								}

								if (! keyValue.equals(rowData.get(keyIndex))) {
									WritableSheet tempSheet = workbook.importSheet(sheetName, 1, templateSheet);
									populateSheet(tempSheet, creationDate, userQueryOptions, columns, subReportData);
									SheetUtils.copyCells(tempSheet, writableSheet, new ColumnRow(templateCol, templateRow), maxExtent);
									workbook.removeSheet(1);
									templateRow += tempSheet.getRows();
									subReportData.clear();
									subReportData.add(columnHeaders);
									keyValue = rowData.get(keyIndex);
								}
								subReportData.add(rowData);
							}
							WritableSheet tempSheet = workbook.importSheet(sheetName, 1, templateSheet);
							populateSheet(tempSheet, creationDate, userQueryOptions, columns, subReportData);
							SheetUtils.copyCells(tempSheet, writableSheet, new ColumnRow(templateCol, templateRow), maxExtent);
							workbook.removeSheet(1);
						}
					} else {
						WritableSheet tempSheet = workbook.importSheet(sheetName, 1, templateSheet);
						populateSheet(tempSheet, creationDate, userQueryOptions, columns, reportData);
						SheetUtils.copyCells(tempSheet, writableSheet, new ColumnRow(templateCol, templateRow), maxExtent);
						workbook.removeSheet(1);
						templateRow += tempSheet.getRows();
					}
				}
			}
			dateSheetFormatter.format(writableSheet, creationDate);
			userCriteriaFormatter.format(writableSheet, userQueryOptions.getConditions());

			reservoirCountFormatter.format(writableSheet, getUniqueReservoirCount(reportData));
			
			rowCountFormatter.format(writableSheet, reportData);
			messageCellFormatter.format(writableSheet, reportData);
			workbook.write();
			workbook.close();
		} catch (IOException | WriteException e) {
			logger.error("Error creating workbook", e);
		}
	}

	private String getUniqueReservoirCount(List<List<Object>> reportData) {

		int resCount = 0;
		String count;
		String previous = "";
		List<Object> headerRow = reportData.get(0);
		//Find the reservoir
		//Look for reference number, followed by reservoir number then public name
		// This is not a good way of doing it. There should be an attribute in the XML to identify the correct column
		
		if (!headerRow.isEmpty()) {
			int column = headerRow.indexOf("Reference Number");
			if (column == -1) {
				column = headerRow.indexOf("Reservoir Number");
			}
			if (column == -1) {
				column = headerRow.indexOf("Public Name");
			}
			if (column>=0) {
				for (int c=1; c<=reportData.size()-1;c++) {
					List<Object> row = reportData.get(c);
					String ref= (String) row.get(column);
					if (previous.equals("")) {
						resCount ++;
						previous = ref;
					}
					else if (!previous.equals(ref)) {
						resCount++;
						previous=ref;
					}
				}
			}
		}
		count=""+resCount;
		return count;
	}

	private void populateSheet(WritableSheet sheet, Date creationDate, ReportQueryOptions userQueryOptions, List<Column> columns, List<List<Object>> reportData) 
			throws RowsExceededException, WriteException, JsonParseException, JsonMappingException, IOException {

//		dateSheetFormatter.format(sheet, creationDate);
//		userCriteriaFormatter.format(sheet, userQueryOptions.getConditions());
//
//		reservoirCountFormatter.format(sheet, getUniqueReservoirCount(reportData));
//		
//		rowCountFormatter.format(sheet, reportData);

		List<Cell> headerMarkers = SheetUtils.findCells(sheet, "#columnheaders.*", null);
		headerMarkers = SheetUtils.findCells(sheet, "#rowheaders.*", headerMarkers);
		Collections.sort(headerMarkers, new CellPositionComparator());
		List<Object> headers = reportData.get(0);
		int endCol = 0;
		int endRow = 0;
		ObjectMapper mapper = new ObjectMapper();

		for (Cell headerCell : headerMarkers) {
			CellFormat cellFormat = headerCell.getCellFormat();
			int startCol = headerCell.getColumn();
			int startRow = headerCell.getRow();
			if (startCol > endCol) endCol = startCol;
			if (startRow > endRow) endRow = startRow;
			int cellCol;
			int cellRow;

			String cellContents = headerCell.getContents();
			boolean byColumn = false;
			String headerModifier;

			if (cellContents.startsWith("#columnheaders")) {
				headerModifier = cellContents.substring("#columnheaders".length());
			} else {
				byColumn = true;
				headerModifier = cellContents.substring("#rowheaders".length());
			}

			List<String> includes = null;
			List<String> excludes = null;
			if (null != headerModifier && ! headerModifier.isEmpty()) {
				Map<String, List<String>> headerMap = mapper.readValue(headerModifier, new TypeReference<HashMap<String,List<String>>>(){});
				includes = headerMap.get("includes");
				excludes = headerMap.get("excludes");
			}
			List<String> includedHeaders = new ArrayList<String>();
			for (Object header : headers) {
				if (null == includes || includes.contains(header)) {
					includedHeaders.add(header.toString());
				}
			}
			if (excludes != null) {
				includedHeaders.removeAll(excludes);
			}
			boolean insertCells = hasAdjacentDataCells(headerCell.getColumn(), headerCell.getRow(), sheet, byColumn);
			cellCol = startCol;
			cellRow = startRow;
			WritableCell blankCell = new Blank(startCol, startRow);
			sheet.addCell(blankCell);

			for (int dataCol = 0; dataCol < headers.size(); dataCol++) {
				String headerContents = headers.get(dataCol).toString();
				if (includedHeaders.contains(headerContents)) {
					if (cellCol > startCol && cellRow > startRow) {
						if (! byColumn && cellCol > endCol) {
							if (insertCells) sheet.insertColumn(cellCol);
							endCol = cellCol;
							//							CellView cellview = sheet.getColumnView(cellCol);
							//							cellview.setAutosize(true);
							//						    sheet.setColumnView(cellCol, cellview);
						} else if (byColumn && cellRow > endRow) {
							if (insertCells) sheet.insertRow(cellRow);
							endRow = cellRow;
						}
					}
					Label label = new Label(cellCol, cellRow, headerContents);
					label.setCellFormat(cellFormat);
					sheet.addCell(label);

					if (byColumn) {
						cellRow++;
					} else {
						cellCol++;
					}
				}
			}

			if (byColumn) {
				startCol++;
			} else {
				startRow++;
			}
			Cell dataCell = sheet.getCell(startCol, startRow);
			String dataTag = byColumn ? "#rowdata" : "#columndata";
			cellContents = dataCell.getContents();
			cellFormat = dataCell.getCellFormat();

			if (null != cellContents && cellContents.startsWith(dataTag)) {
				if (endCol < startCol) endCol = startCol;
				if (endRow < startRow) endRow = startRow;
				cellRow = startRow;
				cellCol = startCol;
				String dataModifier = cellContents.substring(dataTag.length());
				boolean distinctOnly = false;
				boolean nonNullOnly = false;
				if (! dataModifier.isEmpty()) {
					Map<String, List<String>> dataMap = mapper.readValue(dataModifier, new TypeReference<HashMap<String, Object>>(){});
					distinctOnly = Boolean.TRUE.equals(dataMap.get("distinct"));
					nonNullOnly = Boolean.TRUE.equals(dataMap.get("notNull"));
				}
				List<Integer> distinctHashes = null;
				if (distinctOnly) {
					distinctHashes = new ArrayList<Integer>();
				}

				for (int dataRow = 1; dataRow < reportData.size(); dataRow++) {
					if (byColumn) {
						cellRow = startRow;
					} else {
						cellCol = startCol;
					}
					List<Object> values = reportData.get(dataRow);
					if (distinctOnly) {
						List<Object> distinctVals = new ArrayList<Object>();
						for (int dataCol = 0; dataCol < values.size(); dataCol++) {
							String headerContents = headers.get(dataCol).toString();
							if (includedHeaders.contains(headerContents)) {
								distinctVals.add(values.get(dataCol));
							}
						}

						Integer hashValue = new Integer(distinctVals.hashCode());
						if (distinctHashes.contains(hashValue)) {
							continue;
						} else {
							distinctHashes.add(hashValue);
						}
					}
					if (nonNullOnly) {
						boolean nullRow = true;
						for (int dataCol = 0; dataCol < values.size(); dataCol++) {
							String headerContents = headers.get(dataCol).toString();
							if (includedHeaders.contains(headerContents)) {
								nullRow = nullRow && (null == values.get(dataCol) || values.get(dataCol).toString().isEmpty());
								if (! nullRow) break;
							}
						}
						if (nullRow) {
							continue;
						}
					}

					if (! byColumn && cellRow > endRow) {
						sheet.insertRow(cellRow);
						endRow = cellRow;
					} else if (byColumn && cellCol > endCol) {
						sheet.insertColumn(cellCol);
						endCol = cellCol;
					}

					for (int dataCol = 0; dataCol < values.size(); dataCol++) {
						String headerContents = headers.get(dataCol).toString();
						if (includedHeaders.contains(headerContents)) {
							AttributeDataType dataType = columns.get(dataCol).getDataType();
							WritableCell newCell = createCell(cellCol, cellRow, dataType, values.get(dataCol), cellFormat);
							sheet.addCell(newCell);
							if (byColumn) {
								cellRow++;
							} else {
								cellCol++;
							}
						}
					}
					if (byColumn) {
						cellCol++;
					} else {
						cellRow++;
					}
				}
				if (cellRow == startRow && cellCol == startCol) {
					Blank blankDataCell = new Blank(startCol, startRow);
					sheet.addCell(blankDataCell);
				}
			}
		}
	}

	private boolean hasAdjacentDataCells(int col, int row, Sheet sheet, boolean down) {
		final int numRows = sheet.getRows();
		final int numCols = sheet.getColumns();
		if (down) {
			for (int curRow = row + 1; curRow <= numRows; curRow++) {
				Cell testCell = sheet.getCell(col, curRow);

				if (testCell.getType() != CellType.EMPTY) {
					return true;
				}
			}

		} else {
			for (int curCol = col + 1; curCol <= numCols; curCol++) {
				Cell testCell = sheet.getCell(curCol, row);

				if (testCell.getType() != CellType.EMPTY) {
					return true;
				}
			}

		}
		return false;
	}

	private WritableCell createCell(int col, int row, AttributeDataType dataType, Object content, CellFormat format) {
		final Integer trueInteger = Integer.valueOf(1);
		WritableCell newCell;
		switch (dataType) {
		case BOOLEAN:
			boolean boolVal;
			if (content instanceof Boolean) {
				boolVal = ((Boolean) content).booleanValue();
			} else if (content instanceof Integer) {
				boolVal = trueInteger.equals(content) ? true : false;
			} else {
				boolVal = false;
			}
			String yesNoVal = boolVal ? "Yes" : "No";
			newCell = new jxl.write.Label(col, row, yesNoVal, format);
			break;
		case INTEGER:
			int intVal;
			if (content instanceof Integer) {
				intVal = ((Integer) content).intValue();
			} else {
				intVal = 0;
			}
			newCell = new jxl.write.Number(col, row, intVal, format);
			break;
		case STRING:
		case ID:
		case UNDEFINED:
			String stringVal = null;
			if (content instanceof String) {
				stringVal = (String) content;
			} else if (null != content) {
				stringVal = content.toString();
			}
			if (null != stringVal) {
				newCell = new jxl.write.Label(col, row, stringVal, format);
			} else {
				newCell = new jxl.write.Blank(col, row, format);
			}
			break;
		case TIME:
			Date dateVal = null;
			stringVal = null;
			if (content instanceof Date) {
				dateVal = (Date) content;
				stringVal = dateFieldFormat.format(dateVal);
			}
			if (null != stringVal) {
				newCell = new jxl.write.Label(col, row, stringVal, format);
			} else {
				newCell = new jxl.write.Blank(col, row, format);
			}
			break;
		case DOUBLE:
			Double doubleVal;
			if (content instanceof Double) {
				doubleVal = ((Double) content).doubleValue();
			} else {
				doubleVal = 0D;
			}
			newCell = new jxl.write.Number(col, row, doubleVal, format);
			break;
		default:
			newCell = new jxl.write.Blank(col, row, format);
		}
		return newCell;
	}

	private void createLocation(IDfSession dfSession, String location) throws DfException {
		String[] folders = location.split("/");
		String folderName = folders[folders.length - 1];
		StringBuilder pathBuilder = new StringBuilder();
		for (int i = 1; i < folders.length - 1; i++) {
			pathBuilder.append("/").append(folders[i]);
		}
		String parentPath = pathBuilder.toString();
		IDfId folderId = dfSession.getIdByQualification("dm_folder WHERE folder('" + parentPath + "') AND object_name='" + folderName + "'");
		if (folderId.isNull()) {
			createLocation(dfSession, parentPath);
			String folderType = env.getProperty("report.folder.type", "dm_folder");
			IDfFolder dfFolder = (IDfFolder) dfSession.newObject(folderType);
			dfFolder.setObjectName(folderName);
			dfFolder.link(parentPath);
			dfFolder.setACLName(folderAclName);
			dfFolder.setACLDomain("dm_dbo");
			dfFolder.save();
		}
	}

	static class CellPositionComparator implements Comparator<Cell> {

		@Override
		public int compare(Cell c1, Cell c2) {
			int pos1;
			int pos2;

			if (c1.getRow() == c2.getRow()) {
				pos1 = c1.getColumn();
				pos2 = c2.getColumn();
			} else {
				pos1 = c1.getRow();
				pos2 = c2.getRow();
			}

			if (pos1 < pos2) {
				return -1;
			} else if (pos1 > pos2) {
				return +1;
			}
			return 0;
		}		
	};

	static class ColumnRow {
		private int column;
		private int row;
		public ColumnRow(int column, int row) {
			super();
			this.column = column;
			this.row = row;
		}
		public int getColumn() {
			return column;
		}
		public void setColumn(int column) {
			this.column = column;
		}
		public int getRow() {
			return row;
		}
		public void setRow(int row) {
			this.row = row;
		}
	}
}
