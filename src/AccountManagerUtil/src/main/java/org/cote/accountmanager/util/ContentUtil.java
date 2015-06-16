/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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

public class ContentUtil {
	public static String GetMimeType(String extension)
	{
		/*
		if (extension == null || extension.length() == 0) return "application/ocelot-stream";
		switch (extension.toLowerCase())
		{
			case ".rtf": return "text/richtext";
			case ".xsl":
			case ".xslt":
			case ".xml": return "text/xml";
			case ".htm":
			case ".html":
				return "text/html";
			case ".aif": return "audio/x-aiff";
			case ".wav": return "audio/wav";
			case ".mp3": return "audio/mpeg";
			case ".gif": return "image/gif";
			case ".jpg":
			case ".jpeg": return "image/jpeg";
			case ".png": return "image/x-png";
			case ".bmp": return "image/bmp";
			case ".css": return "text/css";
			case ".log":
			case ".txt": return "text/plain";
			case ".js": return "text/javascript";
			default:
				return extension;
		}
		*/
		return null;
	}
}
