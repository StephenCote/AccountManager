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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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
	
	public static byte[] createThumbnail(byte[] source_bytes, int maximum_width, int maximum_height) throws IOException {
		
		//long startTime = System.currentTimeMillis();
		
		byte[] out_bytes = new byte[0];
		
		Image image = ImageIO.read(new ByteArrayInputStream(source_bytes));
		if(image == null) return out_bytes;
		
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		
		/* nothing to do, the image is smaller */
		if(width < maximum_width && height < maximum_height){
			System.out.println("Invalid dimensions");
			return out_bytes;
		}
		
		double scale = (double)maximum_height/(double)height;


		if (width > height){
			scale = (double)maximum_width/(double)width;
		}

		int scale_width = (int)(scale * width);
		int scale_height = (int)(scale * height);

		BufferedImage image_out = new BufferedImage(scale_width, scale_height,BufferedImage.TYPE_INT_RGB);

		AffineTransform at = new AffineTransform();

		if (scale < 1.0d){
			at.scale(scale, scale);
		}
		Graphics2D g2d = image_out.createGraphics();

		image_out.flush();
		g2d.drawImage(image, at, null);
		g2d.dispose();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		ImageIO.write(image_out, "jpg", baos);

		//long stopTime = System.currentTimeMillis();
		//logger.debug("Created thumbnail in " + (stopTime - startTime) + "ms");
		return baos.toByteArray();
	}
	
	private BufferedImage getCompatibleImage(int w, int h) {
		  GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		  GraphicsDevice gd = ge.getDefaultScreenDevice();
		  GraphicsConfiguration gc = gd.getDefaultConfiguration();
		  BufferedImage image = gc.createCompatibleImage(w, h);
		  return image;
		}

}
