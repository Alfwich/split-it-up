package com.arthurwut.splititup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Stack;

public class TotalView extends LinearLayout {

    private ArrayList<TotalSummer> sums;
    private float currentValue;

    public TotalView(Context context) {
        super(context);
        init( LayoutInflater.from(context) );
    }

    public TotalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init( LayoutInflater.from(context) );
    }

    public TotalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(LayoutInflater.from(context));
    }

    private class TotalViewClickHandler implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch( v.getId() ){
                case R.id.totalViewRed:
                    changeValue(0, currentValue);
                    break;

                case R.id.totalViewGreen:
                    changeValue(1, currentValue);
                    break;

                case R.id.totalViewBlue:
                    changeValue(2, currentValue);
                    break;

                case R.id.totalViewRedUndo:
                    undoValue(0);
                    break;

                case R.id.totalViewGreenUndo:
                    undoValue(1);
                    break;

                case R.id.totalViewBlueUndo:
                    undoValue(2);
                    break;
            }
        }
    }

    private class TotalClickAction {
        public int sumId;
        public float delta;

        TotalClickAction( int sumId, float delta ) {
            this.sumId = sumId;
            this.delta = delta;
        }
    }

    public void init( LayoutInflater inflater ) {

        inflater.inflate(R.layout.total_view, this, true);

        sums = new ArrayList<>();

        Button redButton     = (Button) getRootView().findViewById( R.id.totalViewRed);
        Button redButtonUndo = (Button) getRootView().findViewById( R.id.totalViewRedUndo);
        redButton.setOnClickListener(new TotalViewClickHandler());
        redButtonUndo.setOnClickListener(new TotalViewClickHandler());
        sums.add(new TotalSummer(redButton));

        Button greenButton     = (Button) getRootView().findViewById( R.id.totalViewGreen);
        Button greenButtonUndo = (Button) getRootView().findViewById( R.id.totalViewGreenUndo);
        greenButton.setOnClickListener(new TotalViewClickHandler());
        greenButtonUndo.setOnClickListener(new TotalViewClickHandler());
        sums.add(new TotalSummer(greenButton));

        Button blueButton     = (Button) getRootView().findViewById( R.id.totalViewBlue);
        Button blueButtonUndo = (Button) getRootView().findViewById( R.id.totalViewBlueUndo);
        blueButton.setOnClickListener(new TotalViewClickHandler());
        blueButtonUndo.setOnClickListener(new TotalViewClickHandler());
        sums.add(new TotalSummer( blueButton ));

        currentValue = 0.0f;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for(int i = 0 ; i < getChildCount() ; i++){
            getChildAt(i).layout(l, t, r, b);
        }
    }

    public void setValue( float value ) {
        this.currentValue = value;
    }

    public void changeValue(int position, float delta ) {
        TotalSummer s = sums.get( position );
        if( s != null ) {
            s.addValue(delta);
        }
    }

    public void undoValue( int position ) {
        TotalSummer s = sums.get( position );
        if( s != null ) {
            s.undoLast();
        }
    }
}
