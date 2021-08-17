/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DirectoryUtil{
	public static final Logger logger = LogManager.getLogger(DirectoryUtil.class);
	private String path;
	private File directory;
	private boolean isOk=false;
	private int dirCount=0;
	private int fileCount=0;
	private int dirKByteSize=0;
	
	public DirectoryUtil(){
	
	}

	public DirectoryUtil(String inPath){
		init(inPath);
	}
	public void init(String inPath){
		this.path=inPath;
		directory=new File(path);
		if(!directory.isDirectory()){
			logger.debug("DirUtil:: init: '" + path + "' is not a directory");
		}
		else{
			isOk=true;
		}
	}
	public int getDirCount(){
		return dirCount;
	}
	public int getFileCount(){
		return fileCount;
	}
	public int getKByteSize(){
		return dirKByteSize;
	}
	public boolean getReadyState(){
		return isOk;
	}
	public List<File> dir(){
		return dir(null,false);
	}
	public List<File> dirs(){
		return dirs(null,false);
	}

	public List<File> dir(String filter){
		return dir(filter,false);
	}

	public List<File> dir(String filter,boolean recurse){
		List<File> dirList=new ArrayList<>();
		dirCount=0;
		fileCount=0;
		getrecursive(directory,dirList,filter,recurse);
		return dirList;
	}

	public List<File> dirs(String filter,boolean recurse){
		List<File> dirsList=new ArrayList<>();
		dirCount=0;
		fileCount=0;
		getrecursive(directory,null,dirsList,filter,recurse);
		return dirsList;
	}

	public long sizeof(){
		dirCount=0;
		fileCount=0;
		long size=getSizeOf(directory);
		dirKByteSize=(int)(size/1000);
		return size;
	}
	private void getrecursive(File f,List<File> dirs,String filter,boolean recurse){
		getrecursive(f, dirs, null, filter, recurse);
	}
	private void getrecursive(File f,List<File> dirs, List<File> dirlist, String filter,boolean recurse){

		if(f.isDirectory()){
			dirCount++;
			if(dirlist != null) dirlist.add(f);
			File[] list=f.listFiles();
			for(int i=0;i<list.length;i++){
				if(list[i].isDirectory() && recurse){
					getrecursive(list[i],dirs,dirlist,filter,recurse);
				}
				else if(dirs != null && (filter == null || list[i].getName().matches(filter))){
					fileCount++;
					dirs.add(list[i]);
				}
				
			}
		}

	}
	private long getSizeOf(File f){
		long size=0;
		if(f.isDirectory()){
			dirCount++;
			File[] list=f.listFiles();
			for(int i=0;i<list.length;i++){
				if(list[i].isDirectory()){
					size+=getSizeOf(list[i]);
				}
				else{
					fileCount++;
					size+=list[i].length();
				}
			}
		}
		return size;
	}

}