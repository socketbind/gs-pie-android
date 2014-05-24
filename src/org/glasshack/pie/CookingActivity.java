package org.glasshack.pie;

import android.app.Activity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabriel on 2014.05.24..
 */
public class CookingActivity extends Activity {

    public static final String TAG = "CookingActivity";
    private StepCardScrollAdapter adapter;

    private ScrollAware prevSelected;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        adapter = new StepCardScrollAdapter();

        createCards();

        CardScrollView cardScrollView = new CardScrollView(this);
        cardScrollView.setAdapter(adapter);
        cardScrollView.activate();
        setContentView(cardScrollView);

        cardScrollView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemSelected(position, view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (prevSelected != null) {
            prevSelected.deactivated();
        }
    }

    private void createCards() {
        Card ingredientCard = new Card(this);
        ingredientCard.setText("1/2 L or 2 cups of flour\n" +
                "1 teaspoon of salt\n" +
                "3/4 of a stick of tenderflake pastry lard\n" +
                "5 tablespoons of cold water\n" +
                "1 egg\n" +
                "Milk\n");
        ingredientCard.setFootnote("Ingredients");

        Card step1 = new Card(this);
        step1.setText("1. Pre-heat, whatever");
        step1.setFootnote("1/6");
        step1.setTimestamp("Let's cook");

        TimerStepView timerStep1 = new TimerStepView(this, "Lol", 60);

        adapter.add(ingredientCard.getView());
        adapter.add(step1.getView());
        adapter.add(timerStep1);
    }

    private void itemSelected(int position, View view) {
        Log.d(TAG, "Selected item: " + position);

        if (prevSelected != null) {
            prevSelected.deactivated();
        }

        prevSelected = null;

        if (view instanceof ScrollAware) {
            prevSelected = (ScrollAware) view;
            prevSelected.activated();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cooking_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class StepCardScrollAdapter extends CardScrollAdapter {

        private List<View> cards = new ArrayList<View>();

        public boolean add(View card) {
            return cards.add(card);
        }

        public boolean remove(View card) {
            return cards.remove(card);
        }

        @Override
        public int getPosition(Object item) {
            return cards.indexOf(item);
        }

        @Override
        public int getCount() {
            return cards.size();
        }

        @Override
        public Object getItem(int position) {
            return cards.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return cards.size();
        }

        @Override
        public int getItemViewType(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            return  cards.get(position);
        }
    }
}