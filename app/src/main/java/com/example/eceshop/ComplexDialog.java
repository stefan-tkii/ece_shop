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

public class ComplexDialog extends Dialog
{

    private String titleText;
    private String infoText;
    private TextView topText;
    private TextView centerText;
    private AppCompatButton cancelButton;
    private AppCompatButton proceedButton;
    private ImageView topImg;
    private Context context;
    private onProceedClicked mListener;

    public ComplexDialog(@NonNull Context context, String titleText, String infoText, onProceedClicked listener)
    {
        super(context);
        this.context = context;
        this.titleText = titleText;
        this.infoText = infoText;
        mListener = listener;
    }

    public interface onProceedClicked
    {
        void onProceedButtonClicked();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.complex_dialog_layout);
        getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setCancelable(false);
        getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;

        topImg = findViewById(R.id.topImageComplexDialog);
        topText = findViewById(R.id.topComplexDialogText);
        centerText = findViewById(R.id.centerComplexDialogText);
        cancelButton = findViewById(R.id.cancelDialogButton);
        proceedButton = findViewById(R.id.proceedDialogButton);

        topText.setText(titleText);
        centerText.setText(infoText);
        topImg.setImageResource(R.drawable.info_icon);

        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        proceedButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener != null)
                {
                    mListener.onProceedButtonClicked();
                }
            }
        });
    }

}
