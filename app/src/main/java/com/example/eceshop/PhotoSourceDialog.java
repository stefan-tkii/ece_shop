package com.example.eceshop;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

public class PhotoSourceDialog extends Dialog
{

    private String titleText;
    private String infoText;
    private TextView topText;
    private TextView centerText;
    private AppCompatButton cameraButton;
    private AppCompatButton galleryButton;
    private ImageView topImg;
    private Context context;
    private onCameraClicked cameraListener;
    private onGalleryClicked galleryListener;

    public PhotoSourceDialog(@NonNull Context context, String titleText, String infoText, onCameraClicked listener1, onGalleryClicked listener2)
    {
        super(context);
        this.context = context;
        this.titleText = titleText;
        this.infoText = infoText;
        cameraListener = listener1;
        galleryListener = listener2;
    }

    public interface onCameraClicked
    {
        void onCameraOptionClicked();
    }

    public interface onGalleryClicked
    {
        void onGalleryOptionClicked();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_source_dialog_layout);
        getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setCancelable(true);
        getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;

        topImg = findViewById(R.id.topImageSourceDialog);
        topText = findViewById(R.id.topSourceDialogText);
        centerText = findViewById(R.id.centerSourceDialogText);
        cameraButton = findViewById(R.id.cameraDialogButton);
        galleryButton = findViewById(R.id.galleryDialogButton);

        topText.setText(titleText);
        centerText.setText(infoText);
        topImg.setImageResource(R.drawable.upload_icon);

        cameraButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(cameraListener != null)
                {
                    cameraListener.onCameraOptionClicked();
                }
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(galleryListener != null)
                {
                    galleryListener.onGalleryOptionClicked();
                }
            }
        });

    }

}
