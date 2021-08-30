package com.example.eceshop;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;

public class CustomDialog extends Dialog {

    private String titleText;
    private String errorText;
    private TextView topText;
    private TextView centerText;
    private Button closeButton;
    private ImageView topImg;
    private Boolean isSuccess;
    private Context context;

    public CustomDialog(@NonNull Context context, String titleText, String errorText, Boolean isSuccess)
    {
        super(context);
        this.context = context;
        this.titleText = titleText;
        this.errorText = errorText;
        this.isSuccess = isSuccess;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);
        getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setCancelable(false);
        getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        closeButton = findViewById(R.id.closeButton);
        centerText = findViewById(R.id.centerDialogText);
        topText = findViewById(R.id.topDialogText);
        topImg = findViewById(R.id.topImageDialog);
        centerText.setText(errorText);
        topText.setText(titleText);
        if(isSuccess)
        {
            topImg.setImageResource(R.drawable.success);
            closeButton.setBackgroundColor(context.getResources().getColor(R.color.successColor));
        }
        else
        {
            topImg.setImageResource(R.drawable.error);
            closeButton.setBackgroundColor(context.getResources().getColor(R.color.accentColor));

        }
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
