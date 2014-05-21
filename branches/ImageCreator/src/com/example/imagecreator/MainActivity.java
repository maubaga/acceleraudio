package com.example.imagecreator;

import java.io.FileOutputStream;
import java.util.Random;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			ImageView imageView = (ImageView) rootView.findViewById(R.id.pic);
			imageView.setImageURI(Uri.parse("/data/data/com.example.imagecreator/files/" + "immagine.png"));;
			return rootView;
		}
	}

	public void create(View view) {
		Random rand = new Random();
		//creo l'immagine
		Bitmap bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap

		//le do i valori
		for(int i = 0; i < bmp.getWidth(); i++)
			for(int j = 0; j < bmp.getHeight(); j++)
				bmp.setPixel(i, j, rand.nextInt());

		//la salvo
		FileOutputStream out;
		try {
			out = openFileOutput("immagine.png",MODE_PRIVATE);
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			Toast toast=Toast.makeText(this,"Immagine creta",Toast.LENGTH_LONG);
			toast.show();
			out.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		//la visualizzo
		ImageView imageView = (ImageView) findViewById(R.id.pic);
		imageView.setImageBitmap(bmp);

	}
}
