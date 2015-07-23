package com.arthurwut.splititup;

import android.util.Log;
import android.widget.TextView;
import java.util.Stack;

public class TotalSummer {
    private float total;
    private TextView outputView;
    private Stack<Float> undos;

    TotalSummer() {
        init();
        outputView = null;
    }

    TotalSummer( TextView v ) {
        init();
        outputView = v;
    }

    private void init() {
        total = 0.0f;
        outputView = null;
        undos = new Stack<>();
    }

    private void updateTextView() {
        if( outputView != null ) {
            outputView.setText( String.format( "%.2f", total) );
            outputView.requestLayout();
        }
    }

    public void addValue( float v ) {
        total += v;
        undos.push( v );
        updateTextView();
    }

    public void undoLast() {
        if( undos.size() > 0 ) {
            total -= undos.pop();
            updateTextView();
        }
    }

    public float getValue() {
        return total;
    }

}
