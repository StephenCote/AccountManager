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


public class GraphicsUtil {
	
	/// Adapted from earlier 2004 lib I created,
	/// And updated based on http://stackoverflow.com/questions/1069095/how-do-you-create-a-thumbnail-image-out-of-a-jpeg-in-java
	
	
	public static byte[] createThumbnail(byte[] source_bytes, int maximum_width, int maximum_height) throws IOException {
		
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
