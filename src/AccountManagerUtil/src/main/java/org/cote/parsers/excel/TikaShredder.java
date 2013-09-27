package org.cote.parsers.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;



public class TikaShredder {
	private static int tikaWriteLimit = 2000000;
	public static final Logger logger = Logger.getLogger(TikaShredder.class.getName());
	private static Pattern rowLineUrlPattern = Pattern.compile("^http",Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	private static Pattern rowLinePattern = Pattern.compile("^\\t{1}\\S",Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	private static Pattern sheetLinePattern = Pattern.compile("^\\S",Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	private static Pattern returnPattern = Pattern.compile("\r");
	//private Pattern rowLinePattern = Pattern.compile("^(\\t{2})\\s");
	
	public static String getExcelAsString(String path){
		String content = null;
		  InputStream input = null;
	        try {
	        	input = new FileInputStream(path);
	            Metadata metadata = new Metadata();
	            ContentHandler handler = new BodyContentHandler(tikaWriteLimit);
	            
	            ParseContext context = new ParseContext();

	            context.set(Locale.class, Locale.US);
	            AbstractParser ap = null;
	            if(path.endsWith(".xlsx") || path.endsWith(".docx")){
		            ap = new OOXMLParser();
	            }
	            else{
	            	ap = new OfficeParser();
	            }

	            ap.parse(input, handler, metadata, context);
	            content = handler.toString();

	            
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TikaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
	            if(input != null){
					try {
						input.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        }
	        return content;
	}
	
	public static WorkbookType shredTikaContent(String content,boolean hasColumnLabels){
		
		
		
		WorkbookType book = new WorkbookType();
		SheetType currentSheet = null;
		
		String[] lines = content.split("\n");
		logger.info("Shredding " + lines.length + " lines");
		RowType labels = null;
		int currentSheetRows = 0;
		for(int i = 0; i < lines.length; i++){

			
			String line = lines[i];
			if(rowLineUrlPattern.matcher(line).find()){
				logger.info("Skip extra URL data");
				continue;
			}
			Matcher rM = returnPattern.matcher(line);
			if(rM.find()) line = rM.replaceAll("");
			//logger.info("Line: '" + line + "'");
			//logger.info("Line Index: " + line.indexOf("\t"));

			Matcher sheetMatcher = sheetLinePattern.matcher(line);
			if(sheetMatcher.find()){
				currentSheet = new SheetType();
				currentSheetRows = 0;
				currentSheet.setSheetName(line.trim());
				book.getSheets().add(currentSheet);
				continue;
			}
			Matcher lineMatcher = rowLinePattern.matcher(line);
			if(lineMatcher.find()){
				if(currentSheet == null){
					logger.error("Current sheet is null.  Check the workbook format.");
				}
				RowType row = new RowType();
				if(hasColumnLabels == false || currentSheetRows > 0) currentSheet.getRows().add(row);
				else if(hasColumnLabels && currentSheetRows == 0) labels = row;

				line = line.replaceAll("^\\t{1}","");
				String[] cells = line.split("\\t");
				for(int c = 0; c < cells.length; c++){
					CellType cell = new CellType();
					cell.setCellValue(cells[c]);
					if(hasColumnLabels && currentSheetRows > 0 && labels != null){
						cell.setColumnName(labels.getCells().get(c).getCellValue());
					}
					row.getCells().add(cell);
				}
				currentSheetRows++;
				//logger.info(currentSheet.getSheetName() + ": cells: " + cells.length + " : Trim line '" + line + "'");
			}
		}
		return book;
		
	}
}
