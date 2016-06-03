package com.weakentroll.extreme3dpong;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.text.DecimalFormat;

public class TextureHelper
{
	MyGLRenderer.ShapeTypes shapeTypes;
	
	public static int loadTexture(final Context context, final int resourceId, MyGLRenderer.ShapeTypes shape, float touchedX, float touchedY, float touchedZ, float boxMinX, float boxMaxX, float boxMinY, float boxMaxY, float boxMinZ,  float boxMaxZ)
	{
		final int[] textureHandle = new int[1];
		
		GLES20.glGenTextures(1, textureHandle, 0);
		
		//GLES20.glDeleteTextures(1, textureHandle, 0);
		
		if (textureHandle[0] != 0)
		{
			// Formatting the string representation value, not the actual numerical precsision
			DecimalFormat form = new DecimalFormat("0.00");
		    
			 BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;	// No pre-scaling

			// Read in the resource
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
			
			Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

			// get a canvas to paint over the bitmap
			Canvas canvas = new Canvas(mutableBitmap);
			mutableBitmap.eraseColor(0);

			// get a background image from resources
			// note the image format must match the bitmap format
			Drawable background;

            if (shape == shape.player) {
                background = context.getResources().getDrawable(R.drawable.text_bubble_bg);
                background.setBounds(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
                background.draw(canvas); // draw the background to our bitmap
            }
			else if (shape == shape.puck) {
				background = context.getResources().getDrawable(R.drawable.puck);
                background.setBounds(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
                background.draw(canvas); // draw the background to our bitmap
			}
			else if (shape == shape.opponent) {
                background = context.getResources().getDrawable(R.drawable.text_bubble_bg);
                background.setBounds(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
                background.draw(canvas); // draw the background to our bitmap
            }

            else if (shape == shape.crosshair) {
                background = context.getResources().getDrawable(R.drawable.green_bullseye);
                background.setBounds(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
                background.draw(canvas); // draw the background to our bitmap
            }

			if (shape != shape.puck) {
				// Draw the text
				/*Paint textPaint = new Paint();
				textPaint.setTextSize(48);
				textPaint.setAntiAlias(true);
				textPaint.setARGB(0xff, 0x00, 0x00, 0x00);
				// draw the text centered
				canvas.drawText("Touched Coords:", 20,45, textPaint);
				canvas.drawText(form.format(touchedX) + ", " + form.format(touchedY) + ", " + form.format(touchedZ), 20,85, textPaint);
				canvas.drawText("Box TL: " + form.format(boxMinX) + ", " + form.format(boxMaxY), 20,125, textPaint);
				canvas.drawText("Box BR: " + form.format(boxMaxX) + ", " + form.format(boxMinY), 20,165, textPaint);
				canvas.drawText("Puck -,+Y: " + form.format(boxMinZ) + ", " + form.format(boxMaxZ), 20,205, textPaint);*/
			}
			
			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			
			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			
			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mutableBitmap, 0);
			
			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();						
		}
		
		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}
		
		return textureHandle[0];
	}
}
