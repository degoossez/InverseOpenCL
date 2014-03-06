package com.example.inversefilter;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

public class MainActivity extends Activity {

    static boolean sfoundLibrary = true;  
    static {
        try { 
        	System.load("/system/vendor/lib/libPVROCL.so");
        	 Log.i("Debug", "OpenCL lib Loaded");
        	 System.loadLibrary("InverseFilter"); 
        	 Log.i("Debug","My Lib Loaded!");
        }
        catch (UnsatisfiedLinkError e) {
          sfoundLibrary = false;
        }
      }
	/*
	 * loads the kernel into the app_execdir 
	 */
	private void copyFile(final String f) {
		InputStream in;
		try {
			in = getAssets().open(f);
			final File of = new File(getDir("execdir",MODE_PRIVATE), f);
			
			final OutputStream out = new FileOutputStream(of);

			final byte b[] = new byte[65535];
			int sz = 0;
			while ((sz = in.read(b)) > 0) {
				out.write(b, 0, sz);
			}
			in.close();
			out.close();
		} catch (IOException e) {       
			e.printStackTrace();
		}
	}
	
    
    private native void initOpenCL (String kernelName);
    private native void nativeInverseOpenCL (
            Bitmap inputBitmap,
            Bitmap outputBitmap
        );
    private native void shutdownOpenCL ();
    
    // Input bitmap object, serves as a single source for the image-processing kernel.
    Bitmap inputBitmap;
    // Output bitmap object, serves as a destination for the image-processing kernel.
    Bitmap outputBitmap;
    // Single ImageView that is used to output the resulting bitmap object.
    ImageView outputImageView;   
    //End Intel example OPENCL	
	
	final int info[] = new int[3]; // Width, Height, Execution time (ms)
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 //outputImageView = (ImageView)findViewById(R.id.imageView1);
		 outputImageView = (ImageView)findViewById(R.id.imageView2);
		 	
		 	inputBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.leopard);
		 	Log.i("Debug", String.valueOf(inputBitmap.getWidth()));
	        int imageWidth  = inputBitmap.getWidth();
	        int imageHeight = inputBitmap.getHeight();
	        // Two bitmap objects for the simple double-buffering scheme, where first bitmap object is rendered,
	        // while the second one is being updated, then vice versa.
	        outputBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
	        
     	Log.i("DEBUG","BEFORE runOpencl");
     	String kernelName = "inverse";
     	copyFile("inverse.cl");
     	initOpenCL(kernelName);
        nativeInverseOpenCL(
        		inputBitmap,
        		outputBitmap
            );
    	shutdownOpenCL();
    	Log.i("DEBUG","AFTER runOpencl");
	 	Log.i("Debug", String.valueOf(outputBitmap.getWidth()));

    	outputImageView.setImageBitmap(outputBitmap);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}

