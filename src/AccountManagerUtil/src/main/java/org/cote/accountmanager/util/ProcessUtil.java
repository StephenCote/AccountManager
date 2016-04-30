package org.cote.accountmanager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ProcessUtil {
	public static final Logger logger = Logger.getLogger(ProcessUtil.class.getName());
	
	public static List<String> runProcess(String basePath, String[] command){
		List<String> output = new ArrayList<String>();
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		pb.directory(new File(basePath + "/"));
		boolean processCompleted = false;
		try{
			final Process proc = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			logger.debug("Running Process (" + basePath + ") : " + String.join(" ", command));
			while((line = br.readLine()) != null){
				logger.debug(line);
				output.add(line);
			}

			br.close();
			processCompleted = true;
		}
		catch(IOException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			if(processCompleted == false){
				logger.error("Process did not complete as expected");
			}
			
		}
		return output;
	}
	
}
