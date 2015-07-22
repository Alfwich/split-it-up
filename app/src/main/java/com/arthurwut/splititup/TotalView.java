package com.arthurwut.splititup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class TotalView extends LinearLayout {

    private ArrayList<TotalSummer> sums;
    private ArrayList<Button> buttons;
    private int currentPosition;
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
                    currentPosition = 0;
                    addValueToSum(1);
                    break;

                case R.id.totalViewGreen:
                    currentPosition = 1;
                    addValueToSum(1);
                    break;

                case R.id.totalViewBlue:
                    currentPosition = 2;
                    addValueToSum(1);
                    break;

            }
        }
    }

    public void init( LayoutInflater inflater ) {

        inflater.inflate(R.layout.total_view, this, true);

        sums = new ArrayList<>();
        sums.add(new TotalSummer());
        sums.add(new TotalSummer());
        sums.add(new TotalSummer());
        currentPosition = 0;
        currentValue = 0.0f;

        buttons = new ArrayList<>();
        Button redButton   = (Button) getRootView().findViewById( R.id.totalViewRed);
        redButton.setOnClickListener( new TotalViewClickHandler() );
        buttons.add( redButton );

        Button greenButton = (Button) getRootView().findViewById( R.id.totalViewGreen);
        greenButton.setOnClickListener( new TotalViewClickHandler() );
        buttons.add( greenButton );

        Button blueButton  = (Button) getRootView().findViewById( R.id.totalViewBlue);
        blueButton.setOnClickListener( new TotalViewClickHandler() );
        buttons.add( blueButton );
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

    public void addValueToSum( int magnitude ) {
        TotalSummer s = sums.get( currentPosition );
        Button b = buttons.get( currentPosition );
        if( s != null && b != null ) {
            s.addValue( currentValue * magnitude );
            b.setText( Float.toString( s.getValue() ) );
            b.requestLayout();
        }
    }
}
