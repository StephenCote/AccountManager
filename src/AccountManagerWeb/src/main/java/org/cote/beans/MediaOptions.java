package org.cote.beans;

public class MediaOptions {
	private boolean thumbnail = false;
	private int thumbWidth = 100;
	private int thumbHeight = 100;
	private String mediaBase = null;
	
	public MediaOptions(){
		
	}
	public MediaOptions(String type){
		mediaBase = type;
	}
	
	public String getMediaBase() {
		return mediaBase;
	}

	public void setMediaBase(String mediaBase) {
		this.mediaBase = mediaBase;
	}

	public boolean isThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(boolean thumbnail) {
		this.thumbnail = thumbnail;
	}

	public int getThumbWidth() {
		return thumbWidth;
	}

	public void setThumbWidth(int thumbWidth) {
		this.thumbWidth = thumbWidth;
	}

	public int getThumbHeight() {
		return thumbHeight;
	}

	public void setThumbHeight(int thumbHeight) {
		this.thumbHeight = thumbHeight;
	}




	
}
