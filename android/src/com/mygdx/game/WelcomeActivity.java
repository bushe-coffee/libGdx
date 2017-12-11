package com.mygdx.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.dressplus.dnn.ObjHunter;
import com.dressplus.dnn.utils.FileUtil;
import com.dressplus.dnn.utils.MySystemParams;


public class WelcomeActivity extends Activity {

    private static final byte MODEL_INIT = 1;
    private int prepare = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = new Button(this);
        button.setText("START");
        button.setTextColor(Color.BLUE);
        button.setTextSize(48f);

        setContentView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prepare > 0) {
                    startActivity(new Intent(WelcomeActivity.this, AndroidLauncher2.class));
                    WelcomeActivity.this.finish();
                }
            }
        });

        FileUtil.CopyFilesFromAssets(getApplicationContext(), "model", "DPObjTracker");
        String modelPath = FileUtil.getPath("DPObjTracker");
        Log.e("maxiaoran", modelPath);
        int flag = ObjHunter.getInstance().initDetector(modelPath, 1);
        if (flag == MODEL_INIT) {
            prepare += 1;
        }


    }
}
