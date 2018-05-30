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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GraphicsUtil {
	
	/// Adapted from earlier 2004 lib I created,
	/// And updated based on http://stackoverflow.com/questions/1069095/how-do-you-create-a-thumbnail-image-out-of-a-jpeg-in-java
	public static final Logger logger = LogManager.getLogger(GraphicsUtil.class);
	public static final String IMAGE_FORMAT_JPG = "jpg";
	public static final String IMAGE_FORMAT_PNG = "png";
	public static final String IMAGE_FORMAT = IMAGE_FORMAT_PNG;
	
	
	public static byte[] createThumbnail(byte[] sourceBytes, int maximumWidth, int maximumHeight) throws IOException {
		
		byte[] outBytes = new byte[0];
		
		Image image = ImageIO.read(new ByteArrayInputStream(sourceBytes));
		if(image == null) return outBytes;
		
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		
		/* nothing to do, the image is smaller */
		if(width < maximumWidth && height < maximumHeight){
			System.out.println("Invalid dimensions");
			return outBytes;
		}
		
		double scale = (double)maximumHeight/(double)height;


		if (width > height){
			scale = (double)maximumWidth/(double)width;
		}

		int scaleWidth = (int)(scale * width);
		int scaleHeight = (int)(scale * height);

		BufferedImage imageOut = new BufferedImage(scaleWidth, scaleHeight,BufferedImage.TYPE_INT_ARGB);

		AffineTransform at = new AffineTransform();

		if (scale < 1.0d){
			at.scale(scale, scale);
		}
		Graphics2D g2d = imageOut.createGraphics();

		imageOut.flush();
		g2d.drawImage(image, at, null);
		g2d.dispose();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		ImageIO.write(imageOut, IMAGE_FORMAT, baos);

		return baos.toByteArray();
	}

}
