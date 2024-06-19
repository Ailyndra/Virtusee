package com.virtusee.core;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.FileHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;

import androidx.appcompat.app.AppCompatActivity;

import static androidx.core.content.FileProvider.getUriForFile;

@EActivity
public class TTD extends AppCompatActivity {
    @ViewById
    LinearLayout ttdLayout;

    private String idUsr;

    @Extra
	String imgPath;

    View view;
    signature mSignature;
    Bitmap bitmap;

    @Bean
    AuthHelper authHelper;

    private Uri fileUri;
    private String mCurrentPhotoPath;

    private MenuItem absDelete;
    private MenuItem absSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ttd);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @AfterViews
    protected void init() {
        idUsr = authHelper.getUserid();

        if(!imgPath.equals("")){
            File mediaFile = new File(imgPath);
            mCurrentPhotoPath = imgPath;
            fileUri = getUriForFile(this, "com.virtusee.core.provider.fileprovider", mediaFile);
        } else {
            File mediaFile = FileHelper.getPrivateTtdFile(this,idUsr);
            if(mediaFile!=null) {
                fileUri = getUriForFile(this, "com.virtusee.core.provider.fileprovider", mediaFile);
                mCurrentPhotoPath = mediaFile.getAbsolutePath();
            }
        }


        mSignature = new signature(this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        ttdLayout.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view = ttdLayout;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.ttd, menu);
	    return true;
	}

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        absDelete = menu.findItem(R.id.abs_ttd_delete);
        absSave = menu.findItem(R.id.abs_ttd_save);

        absDelete.setEnabled(false);
        absSave.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        try {
            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;
                default:
                    return super.onOptionsItemSelected(menuItem);
            }
        } catch(RuntimeException e){
            return super.onOptionsItemSelected(menuItem);
        }
    }



    @OptionsItem(R.id.abs_ttd_delete)
	void clearTTD() {
        mSignature.clear();
        absSave.setEnabled(false);
    }


    @OptionsItem(R.id.abs_ttd_save)
    void saveTTD() {
        view.setDrawingCacheEnabled(true);
        mSignature.save(view, mCurrentPhotoPath);
    }


    public class signature extends View {

        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v, String StoredPath) {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(ttdLayout.getWidth(), ttdLayout.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                // Output the file
                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
                v.draw(canvas);

                // Convert the output file to Image such as .png
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFileOutStream);

                Bundle b = new Bundle();
                Intent intent = new Intent();
                intent.putExtra("ttdPath", StoredPath);
                setResult(RESULT_OK,intent);

                mFileOutStream.flush();
                mFileOutStream.close();

                finish();


            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }

        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            absSave.setEnabled(true);
            absDelete.setEnabled(true);

            return true;
        }

        private void debug(String string) {

            Log.v("log_tag", string);

        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

}
