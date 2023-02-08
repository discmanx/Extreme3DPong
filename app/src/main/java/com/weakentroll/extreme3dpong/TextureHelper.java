package com.weakentroll.extreme3dpong;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.text.DecimalFormat;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;

import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

public class TextureHelper
{
	MyGLRenderer.ShapeTypes shapeTypes;
	
	public static int loadTexture(final Context context, final int resourceId, MyGLRenderer.ShapeTypes shape, float touchedX, float touchedY, float touchedZ, float boxMinX, float boxMaxX, float boxMinY, float boxMaxY, float boxMinZ,  float boxMaxZ)
	{
		final int[] textureHandle = new int[1];
		
		glGenTextures(1, textureHandle, 0);
		
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
				background = context.getResources().getDrawable(R.drawable.mars);
                background.setBounds(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
                background.draw(canvas); // draw the background to our bitmap
			}
			else if (shape == shape.opponent) {
                background = context.getResources().getDrawable(R.drawable.text_bubble_bg);
                background.setBounds(0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
                background.draw(canvas); // draw the background to our bitmap
            }

            else if (shape == shape.crosshair) {
                background = context.getResources().getDrawable(R.drawable.square_crosshair);
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
			texImage2D(GLES20.GL_TEXTURE_2D, 0, mutableBitmap, 0);
			
			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();						
		}
		
		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}
		
		return textureHandle[0];
	}

	public static int loadCubeMap(Context context, int[] cubeResources) {

		final int[] textureObjectIds = new int[1];
		glGenTextures(1, textureObjectIds, 0);

		if (textureObjectIds[0] == 0) {
			Log.w("TextureHelper.java", "Could not generate a new texture opengl object.");
			return 0;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		final Bitmap[] cubeBitmaps = new Bitmap[6];

		for (int i = 0; i < 6; i++) {
			cubeBitmaps[i] = BitmapFactory.decodeResource(context.getResources(), cubeResources[i], options);

			if (cubeBitmaps[i] == null) {
				Log.w("TextureHelper.java", "CubeResources[" + cubeResources[i] + "] could not be loaded");
				glDeleteTextures(1, textureObjectIds, 0);
				return 0;
			}

		}
		glBindTexture(GL_TEXTURE_CUBE_MAP, textureObjectIds[0]);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0);

		glBindTexture(GL_TEXTURE_2D, 0);
		for (Bitmap bitmap : cubeBitmaps) {
			bitmap.recycle();
		}
		return textureObjectIds[0];
	}
}
