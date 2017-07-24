/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
//import java.util.Vector;
import java.util.ArrayList;
import java.util.Date;
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
	private long dirByteSize=0;
	private int dirKByteSize=0;
	
	private long start=0;
	private long stop=0;
	private long now=0;
	
	public DirectoryUtil(){
	
	}

	public DirectoryUtil(String path){
		init(path);
	}
	public void init(String path){
		this.path=path;
		directory=new File(path);
		if(!directory.isDirectory()){
			logger.info("DirUtil:: init: '" + path + "' is not a directory");
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
		List<File> dirList=new ArrayList<File>();
		dirCount=0;
		fileCount=0;
		start=new Date().getTime();
		now=new Date().getTime();
		_getrecursive(directory,dirList,filter,recurse);
		return dirList;
	}

	public List<File> dirs(String filter,boolean recurse){
		List<File> dirsList=new ArrayList<File>();
		dirCount=0;
		fileCount=0;
		start=new Date().getTime();
		now=new Date().getTime();
		_getrecursive(directory,null,dirsList,filter,recurse);
		return dirsList;
	}

	public long sizeof(){
		dirCount=0;
		fileCount=0;
		start=new Date().getTime();
		now=new Date().getTime();
		long size=_getsizeof(directory);
		dirByteSize=size;
		dirKByteSize=new Long(size/1000).intValue();
		return size;
	}
	private void _getrecursive(File f,List<File> dirs,String filter,boolean recurse){
		_getrecursive(f, dirs, null, filter, recurse);
	}
	private void _getrecursive(File f,List<File> dirs, List<File> dirlist, String filter,boolean recurse){
		long size=0;
		long mark=new Date().getTime();
		if((mark - now) > 1000){
			now=mark;
			logger.info("Status: current: " + f.getAbsolutePath() + ": directories: " + dirCount + ", files: " + fileCount);
		}
		if(f.isDirectory()){
			dirCount++;
			if(dirlist != null) dirlist.add(f);
			File[] list=f.listFiles();
			for(int i=0;i<list.length;i++){
				if(list[i].isDirectory() && recurse){
					_getrecursive(list[i],dirs,dirlist,filter,recurse);
				}
				else if(dirs != null){
					if(filter == null || list[i].getName().matches(filter) == true){
						fileCount++;
						dirs.add(list[i]);
					}
				}
			}
		}

	}
	private long _getsizeof(File f){
		long size=0;
		long mark=new Date().getTime();
		if((mark - now) > 1000){
			now=mark;
			logger.info("Status: current: " + f.getAbsolutePath() + ": directories: " + dirCount + ", files: " + fileCount);
		}
		if(f.isDirectory()){
			dirCount++;
			File[] list=f.listFiles();
			for(int i=0;i<list.length;i++){
				if(list[i].isDirectory()){
					size+=_getsizeof(list[i]);
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