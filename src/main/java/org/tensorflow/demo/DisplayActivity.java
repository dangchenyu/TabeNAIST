package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayActivity extends Activity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        imageView = findViewById(R.id.imageView);


        Intent intent = getIntent();

        if (intent != null) {
            Bitmap bitmap = intent.getParcelableExtra("bitmap");
            imageView.setImageBitmap(bitmap);
        }
        assert intent != null;
        String message1 = intent.getStringExtra("Price");
        String message2 = intent.getStringExtra("Kcal");
        String message3 = intent.getStringExtra("Dish");
        String message4 = intent.getStringExtra("Recommend");

        TextView textView = findViewById(R.id.textView2);
        TextView textView1 = findViewById(R.id.textView3);
        TextView textView2 = findViewById(R.id.textView4);
        TextView textView3 = findViewById(R.id.textView5);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim);
        assert message4 != null;
        if (message4.equals("Watch out your \nWeight!")) {
            SpannableStringBuilder style = new SpannableStringBuilder(message4);
            style.setSpan(new ForegroundColorSpan(Color.argb(255, 151, 19, 19)), 0, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView3.setText(style);
            textView3.startAnimation(animation);
        } else if (message4.equals("Looks Good!")) {
            SpannableStringBuilder style = new SpannableStringBuilder(message4);
            style.setSpan(new ForegroundColorSpan(Color.argb(255, 30, 75, 42)), 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView3.setText(style);
            textView3.startAnimation(animation);
        } else {
            textView3.setText(message4);
            textView3.startAnimation(animation);
        }

        textView.setText("  What you eat:  " + message3);
        textView1.setText("  Kcal:  " + message2);
        textView2.setText("  Total Price:  " + message1);


    }
}
