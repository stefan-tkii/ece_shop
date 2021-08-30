package com.example.eceshop;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

public class SuggestionSimpleCursorAdapter extends SimpleCursorAdapter
{

    private final LayoutInflater inflater;
    private int layout;
    private SuggestionDatabase db;

    public SuggestionSimpleCursorAdapter(Context context, int layout, Cursor c,
                                         String[] from, int[] to)
    {
        super(context, layout, c, from, to);
        this.inflater = LayoutInflater.from(context);
        this.layout = layout;
        this.db = new SuggestionDatabase(context);
    }

    public SuggestionSimpleCursorAdapter(Context context, int layout, Cursor c,
                                         String[] from, int[] to, int flags)
    {
        super(context, layout, c, from, to, flags);
        this.layout = layout;
        this.inflater = LayoutInflater.from(context);
        this.db = new SuggestionDatabase(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        super.bindView(view, context, cursor);
        CharSequence suggestion = convertToString(cursor);
        TextView content = (TextView) view.findViewById(R.id.suggestion_text);
        content.setText(suggestion);
        ImageView img = (ImageView) view.findViewById(R.id.suggestion_delete_img);
        img.setImageResource(R.drawable.ic_baseline_delete);
        img.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e("OnClickedImg", "The thrash image has been clicked.");
                boolean val = db.deleteSuggestion(suggestion.toString());
                if(val)
                {
                    Log.e("OnClickedImg", "Suggestion is deleted.");
                    cursor.requery();
                    notifyDataSetChanged();
                }
                else
                {
                    Log.e("OnClickedImg", "Suggestion is not found.");
                }
            }
        });
    }

    @Override
    public CharSequence convertToString(Cursor cursor)
    {
        int indexColumnSuggestion = cursor.getColumnIndex(SuggestionDatabase.FIELD_SUGGESTION);
        return cursor.getString(indexColumnSuggestion);
    }

}
