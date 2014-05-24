package org.glasshack.pie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.TextView;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.glass.input.VoiceInputHelper;
import com.google.glass.input.VoiceListener;
import com.google.glass.logging.FormattingLogger;
import com.google.glass.logging.FormattingLoggers;
import com.google.glass.voice.VoiceCommand;
import com.google.glass.voice.VoiceConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by gabriel on 2014.05.24..
 */
public class CookingActivity extends Activity {

    public static final String TAG = "CookingActivity";

    public static final String PREVIOUS = "previous";
    public static final String NEXT = "next";
    public static final String SHOT = "shot";
    public static final String CARD_POSITION = "card_position";

    private StepCardScrollAdapter adapter;

    private ScrollAware prevSelected;

    private TextToSpeech tts;
    private TextView loading;

    private VoiceInputHelper voiceInputHelper;

    private final VoiceConfig VOICE_CONFIG =
            new VoiceConfig("myhotwords",
                    new String[]{PREVIOUS, NEXT, SHOT});
    private CardScrollView cardScrollView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        loading = new TextView(this);
        loading.setText("Loading...");
        setContentView(loading);

        voiceInputHelper = new VoiceInputHelper(this, new PieVoiceListener(),
                VoiceInputHelper.newUserActivityObserver(this));
    }

    private void setupCards() {
        createCards();

        cardScrollView = new CardScrollView(this);
        cardScrollView.setAdapter(adapter);
        cardScrollView.activate();
        setContentView(cardScrollView);

        cardScrollView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemSelected(view, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        cardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof ScrollAware) {
                    ScrollAware scrollAware = (ScrollAware) view;
                    scrollAware.onTapped();
                }
            }
        });

        View firstCard = adapter.get(0);
        itemSelected(firstCard, adapter.getPosition(firstCard));
    }

    private void ttsInitialized(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);

            tts.setSpeechRate(0.7f);

            setupCards();
        } else {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        voiceInputHelper.addVoiceServiceListener();

        if (prevSelected != null) {
            prevSelected.activated();
        }

        setContentView(loading);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                ttsInitialized(status);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        voiceInputHelper.removeVoiceServiceListener();

        if (prevSelected != null) {
            prevSelected.deactivated();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(CARD_POSITION, cardScrollView.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cardScrollView.setSelection(savedInstanceState.getInt(CARD_POSITION, 0));
    }

    private void createCards() {
        adapter = new StepCardScrollAdapter();

        SimpleStepView ingredient1 = new SimpleStepView(this,
                "- 1/2 L or 2 cups of flour\n" +
                        "- 1 teaspoon of salt\n" +
                        "- 3/4 of a stick (90 g) of tenderflake pastry lard\n" +
                        "- 5 tablespoons of cold water\n" +
                        "- 1 egg (for brushing)\n" +
                        "- Milk (for brushing)"
        );

        ingredient1.setFooter("Crust");
        ingredient1.setImageResource(R.drawable.intro);

        /*Card ingredientCard1 = new Card(this);
        ingredientCard1.setText("- 1/2 L or 2 cups of flour\n" +
                "- 1 teaspoon of salt\n" +
                "- 3/4 of a stick (90 g) of tenderflake pastry lard\n" +
                "- 5 tablespoons of cold water\n" +
                "- 1 egg (for brushing)\n" +
                "- Milk (for brushing)");
        ingredientCard1.setFootnote("Crust");
        ingredientCard1.addImage(R.drawable.intro);
        ingredientCard1.setImageLayout(Card.ImageLayout.FULL);*/

        Card ingredientCard2 = new Card(this);
        ingredientCard2.setText(
                "- 80 mL or 1/3 cup of white sugar\n" +
                        "- 80 mL or 1/3 cup of brown sugar\n" +
                        "- 1/4 teaspoon of salt\n" +
                        "- 1 tsp cinnamon\n" +
                        "- 1/2 tsp nutmeg\n" +
                        "- 3 tablespoons of flour\n" +
                        "- 6-8 medium-sized apples"
        );
        ingredientCard2.setFootnote("Filling");
        ingredientCard2.addImage(R.drawable.intro);
        ingredientCard2.setImageLayout(Card.ImageLayout.FULL);

        Card step1 = new Card(this);
        step1.setText("Preheat the oven 200°C");
        step1.setTimestamp("1/25");

        Card step2 = new Card(this);
        step2.setText("Place the flour, salt, and butter in a large bowl.");
        step2.setTimestamp("2/25");

        Card step3 = new Card(this);
        step3.setText("With a pastry blender or fork, crush the butter until it forms tiny balls with the flour. Then slowly add the water.");
        step3.setTimestamp("3/25");
        step3.addImage(R.drawable.step3);
        step3.setImageLayout(Card.ImageLayout.FULL);

        Card step4 = new Card(this);
        step4.setText("Wrap both of the dough balls and refrigerate them for 30 minutes before proceeding to the next step.");
        step4.setTimestamp("4/25");

        Card step5 = new Card(this);
        step5.setText("On a floured counter-top, begin to roll the dough out into a circle shape about 2 inches larger in diameter than the pie pan.");
        step5.setTimestamp("5/25");
        step5.addImage(R.drawable.step5);
        step5.setImageLayout(Card.ImageLayout.FULL);

        Card step6 = new Card(this);
        step6.setText("Slowly lift the flattened dough off the counter-top by wrapping it completely around the rolling pin.");
        step6.setTimestamp("6/25");
        step6.addImage(R.drawable.step6);
        step6.setImageLayout(Card.ImageLayout.FULL);

        Card step7 = new Card(this);
        step7.setText("Unroll the dough over the pan, being careful not to let it tear. Fit it into the pan, pressing it against all the sides.");
        step7.setTimestamp("7/25");
        step7.addImage(R.drawable.step7);
        step7.setImageLayout(Card.ImageLayout.FULL);

        Card step8 = new Card(this);
        step8.setText("Cut off the overhanging edges. Leave about 0.5 cm of extra dough over the pie pan.");
        step8.setTimestamp("8/25");

        TimerStepView timerStep1 = new TimerStepView(this, "Place the pie shell in the refrigerator.", 15);
        timerStep1.setTimestamp("9/25");

        Card step10 = new Card(this);
        step10.setText("Peel and slice the apples into pieces about 1 cm cubes.");
        step10.setTimestamp("10/25");
        step10.addImage(R.drawable.step10);
        step10.setImageLayout(Card.ImageLayout.FULL);

        Card step11 = new Card(this);
        step11.setText("Put them into a large bowl and mix with sugar, salt, lemon juice, flour,nutmeg and cinnamon.");
        step11.setTimestamp("11/25");

        Card step12 = new Card(this);
        step12.setText("Shake over to cover the top of mixture. Place in refrigerator.");
        step12.setTimestamp("12/25");

        Card step13 = new Card(this);
        step13.setText("Roll out the remaining ball of dough on a floured surface, just like you did before.");
        step13.setTimestamp("13/25");
        step13.addImage(R.drawable.step13);
        step13.setImageLayout(Card.ImageLayout.FULL);

        Card step14 = new Card(this);
        step14.setText("Gently fold it in half and make 4 to 5 half inch long slices along the fold and 4 slices in the center of the folded piece. These will allow the filling to breath and not break through the sides. Unfold the top crust set it aside.");
        step14.setTimestamp("14/25");

        Card step15 = new Card(this);
        step15.setText("Remove the pie shell and filling from the refrigerator.");
        step15.setTimestamp("15/25");

        Card step16 = new Card(this);
        step16.setText("Pour the filling into the pie shell, spreading it out with the back of a spoon.");
        step16.setTimestamp("16/25");
        step16.addImage(R.drawable.step16);
        step16.setImageLayout(Card.ImageLayout.FULL);

        Card step17 = new Card(this);
        step17.setText("Brush the edges of the pie shell with a beaten egg.");
        step17.setTimestamp("17/25");
        step17.addImage(R.drawable.step17);
        step17.setImageLayout(Card.ImageLayout.FULL);

        Card step18 = new Card(this);
        step18.setText("Lay the sliced top crust over filling. Cut off the excess edges.");
        step18.setTimestamp("18/25");
        step18.addImage(R.drawable.step18);
        step18.setImageLayout(Card.ImageLayout.FULL);

        Card step19 = new Card(this);
        step19.setText("Take both thumbs facing each other and place them over the edge.");
        step19.setTimestamp("19/25");
        step19.addImage(R.drawable.step19);
        step19.setImageLayout(Card.ImageLayout.FULL);

        Card step20 = new Card(this);
        step20.setText("Push thumbs down and towards each other. Do this around the entire pie to seal it.");
        step20.setTimestamp("20/25");
        step20.addImage(R.drawable.step20);
        step20.setImageLayout(Card.ImageLayout.FULL);

        Card step21 = new Card(this);
        step21.setText("Brush the lattice with the egg wash.");
        step21.setTimestamp("21/25");
        step21.addImage(R.drawable.step21);
        step21.setImageLayout(Card.ImageLayout.FULL);

        Card step22 = new Card(this);
        step22.setText("Dust cinnamon and sugar over the top crust for an extra touch.");
        step22.setTimestamp("22/25");

        TimerStepView timerStep2 = new TimerStepView(this, "Bake at 200°C for 15 minutes.", 15);
        timerStep2.setTimestamp("23/25");

        Card step24 = new Card(this);
        step24.setText("Remove when the top crust is golden.");
        step24.setTimestamp("24/25");
        step24.addImage(R.drawable.step24);
        step24.setImageLayout(Card.ImageLayout.FULL);

        TimerStepView timerStep3 = new TimerStepView(this, "Allow the pie to cool 45 minutes to 1 hour at room temperature before serving.", 45);
        timerStep3.setTimestamp("25/25");
        timerStep3.setImageResource(R.drawable.step25);

        adapter.add(ingredient1);
        adapter.add(ingredientCard2.getView());
        adapter.add(step1.getView());
        adapter.add(step2.getView());
        adapter.add(step3.getView());
        adapter.add(step4.getView());
        adapter.add(step5.getView());
        adapter.add(step6.getView());
        adapter.add(step7.getView());
        adapter.add(step8.getView());
        adapter.add(timerStep1);
        adapter.add(step10.getView());
        adapter.add(step11.getView());
        adapter.add(step12.getView());
        adapter.add(step13.getView());
        adapter.add(step14.getView());
        adapter.add(step15.getView());
        adapter.add(step16.getView());
        adapter.add(step17.getView());
        adapter.add(step18.getView());
        adapter.add(step19.getView());
        adapter.add(step20.getView());
        adapter.add(step21.getView());
        adapter.add(step22.getView());
        adapter.add(timerStep2);
        adapter.add(step24.getView());
        adapter.add(timerStep3);
    }

    private void itemSelected(View view, int position) {
        if (prevSelected != null) {
            prevSelected.deactivated();
        }

        prevSelected = null;

        if (view instanceof ScrollAware) {
            prevSelected = (ScrollAware) view;
            prevSelected.activated();
        }

        // erre nem vagyok büszke
        TextView tvStepText = (TextView) view.findViewById(R.id.step_text);

        if (tvStepText == null) {
            tvStepText = findFirstTextView(view);
        }

        if (tvStepText != null) {
            tts.speak(tvStepText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public TextView findFirstTextView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                TextView firstTextView = findFirstTextView(viewGroup.getChildAt(i));

                if (firstTextView != null) {
                    return firstTextView;
                }
            }

            return null;
        } else if (view instanceof TextView) {
            return (TextView) view;
        } else {
            return null;
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

        public View get(int location) {
            return cards.get(location);
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

    private class PieVoiceListener implements VoiceListener {

        @Override
        public FormattingLogger getLogger() {
            return FormattingLoggers.getContextLogger();
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public boolean onResampledAudioData(byte[] bytes, int i, int i2) {
            return false;
        }

        @Override
        public boolean onVoiceAmplitudeChanged(double v) {
            return false;
        }

        @Override
        public VoiceConfig onVoiceCommand(VoiceCommand vc) {
            if (PREVIOUS.equals(vc.getLiteral())) {
                Log.d(TAG, "Prev!");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int pos = cardScrollView.getSelectedItemPosition();
                        Log.d(TAG, "UI Thread prev: " + pos);
                        if (pos > 0) {
                            cardScrollView.setSelection(--pos);
                        }
                    }
                });
            } else if (NEXT.equals(vc.getLiteral())) {
                Log.d(TAG, "Next!");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int pos = cardScrollView.getSelectedItemPosition();
                        Log.d(TAG, "UI Thread next: " + pos);
                        if (pos < cardScrollView.getAdapter().getCount()) {
                            cardScrollView.setSelection(++pos);
                        }
                    }
                });
            } else if (SHOT.equals(vc.getLiteral())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent("com.google.glass.action.TAKE_PICTURE");
                        startActivity(i);
                    }
                });
            }

            return null;
        }

        @Override
        public void onVoiceConfigChanged(VoiceConfig voiceConfig, boolean b) {

        }

        @Override
        public void onVoiceServiceConnected() {
            voiceInputHelper.setVoiceConfig(VOICE_CONFIG);
        }

        @Override
        public void onVoiceServiceDisconnected() {

        }
    }
}