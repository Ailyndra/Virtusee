package com.virtusee.core;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.virtusee.helper.ImageHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import androidx.appcompat.app.AppCompatActivity;

@EActivity
public class Photo extends AppCompatActivity {
    @ViewById
    ImageView photoView;

    @Extra
	String imgPath;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.photo);

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.photo, menu);
	    return true;
	}


	@OptionsItem(R.id.abs_photo_cancel)
	void cancelForm() {
		Intent returnIntent = new Intent();
		setResult(RESULT_OK,returnIntent); 
		finish();
	}

	@OptionsItem(R.id.abs_photo_delete)
	void deletePhoto() {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("result","delete");
		setResult(RESULT_OK,returnIntent); 
		finish();
	}
	
	@AfterViews
    protected void init() {
		Log.e("photo",imgPath);
		setSupportProgressBarIndeterminateVisibility(true);
		parseContent(imgPath);
    }

	@Background
	void parseContent(String path) {
		Bitmap sample = BitmapFactory.decodeFile(path);
		if(sample!=null){
			int thumbWidth,thumbHeight;
			if(sample.getWidth() > sample.getHeight()){
				thumbWidth = 800;
				thumbHeight = 600;
			} else {
				thumbWidth = 600;
				thumbHeight = 800;
			}
			
			
			Bitmap thumbBitmap = ImageHelper.createThumb(path, thumbWidth, thumbHeight);
			setPhoto(thumbBitmap);
		}
	}	


	@UiThread
	void setPhoto(Bitmap image) {
		photoView.setImageBitmap(image);
		setSupportProgressBarIndeterminateVisibility(false);
	}


	@Override
	public void onDestroy() {
	    super.onDestroy();
        Log.e("checkdestroy","Destroy");
        try {
            Bitmap dd = ((BitmapDrawable) photoView.getDrawable()).getBitmap();
            if (dd != null) dd.recycle();
            photoView.setImageDrawable(null);
        } catch (NullPointerException e){
        } catch (Exception e){
        }
	}


	@UiThread
	public void showMessage(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
}
