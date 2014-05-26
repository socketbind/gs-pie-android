package org.glasshack.pie;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.glass.input.VoiceInputHelper;
import com.google.glass.input.VoiceListener;
import com.google.glass.logging.FormattingLogger;
import com.google.glass.logging.FormattingLoggers;
import com.google.glass.voice.VoiceCommand;
import com.google.glass.voice.VoiceConfig;
import com.google.glass.widget.SliderView;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;
import org.glasshack.pie.model.Recipe;
import org.glasshack.pie.model.Step;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

import java.io.IOException;
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
    public static final String CARD_POSITION = "card_position";
    public static final String RECIPE_ENDPOINT = "http://takkerapp.com:3000";
    public static final String DEFAULT_RECIPE_ID = "5380bfeff067fc8d3b6ca130";
    public static final int CACHE_SIZE = 10485760;

    private StepCardScrollAdapter adapter;

    private ScrollAware currentScrollAware;

    private TextToSpeech tts;

    private TextView loading;
    private SliderView loadingIndeterminate;

    private VoiceInputHelper voiceInputHelper;

    private final VoiceConfig VOICE_CONFIG =
            new VoiceConfig("myhotwords",
                    new String[]{PREVIOUS, NEXT});
    private CardScrollView cardScrollView;

    private RecipeService recipeService;

    private String recipeId = DEFAULT_RECIPE_ID;

    private Recipe currentRecipe = null;

    private InitializationSequence initSequence;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.loading);
        loading = (TextView) findViewById(R.id.loading);
        loadingIndeterminate = (SliderView) findViewById(R.id.indeterminate_loading);

        voiceInputHelper = new VoiceInputHelper(this, new PieVoiceListener(),
                VoiceInputHelper.newUserActivityObserver(this));

        OkHttpClient okHttpClient;

        try {
            okHttpClient = new OkHttpClient();
            okHttpClient.setCache(new Cache(getCacheDir(), CACHE_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
            showError("HTTP client error.");
            return;
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(RECIPE_ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .build();

        recipeId = DEFAULT_RECIPE_ID;

        Uri data = getIntent().getData();
        if (data != null) {
            List<String> pathSegments = data.getPathSegments();
            if (pathSegments != null && pathSegments.size() > 0) {
                recipeId = pathSegments.get(0);
            }
        }

        recipeService = restAdapter.create(RecipeService.class);
    }

    private void recipeDownloaded(Recipe recipe) {
        currentRecipe = recipe;
        initSequence.recipeDownloaded();
    }

    private void recipeFailedToDownload() {
        showError("Download failure.");
    }

    private void showError(String text) {
        loading.setText(text);
        loadingIndeterminate.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initSequence = new InitializationSequence();

        voiceInputHelper.addVoiceServiceListener();

        if (currentScrollAware != null) {
            currentScrollAware.activated();
        }

        setContentView(R.layout.loading);
        loadingIndeterminate.startIndeterminate();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    initSequence.ttsInitialized();
                } else {
                    tts.stop();
                    tts.shutdown();
                    tts = null;
                }
            }
        });

        if (currentRecipe == null) {
            recipeService.getRecipeById(recipeId, new Callback<Recipe>() {
                @Override
                public void success(Recipe recipe, Response response) {
                    recipeDownloaded(recipe);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    retrofitError.printStackTrace();
                    recipeFailedToDownload();
                }
            });
        } else {
            initSequence.recipeDownloaded();
        }
    }

    // tts and recipe ready
    private void initializationDone() {
        tts.setLanguage(Locale.US);

        tts.setSpeechRate(0.7f);

        createCardsFromRecipe(currentRecipe);

        setupCards();
    }

    @Override
    protected void onPause() {
        super.onPause();

        voiceInputHelper.removeVoiceServiceListener();

        if (currentScrollAware != null) {
            currentScrollAware.deactivated();
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

        if (cardScrollView != null) {
            outState.putInt(CARD_POSITION, cardScrollView.getSelectedItemPosition());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cardScrollView.setSelection(savedInstanceState.getInt(CARD_POSITION, 0));
    }

    private void setupCards() {
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

        if (adapter.getCount() > 0) {
            View firstCard = adapter.get(0);
            itemSelected(firstCard, adapter.getPosition(firstCard));
        }
    }

    private void createCardsFromRecipe(Recipe currentRecipe) {
        adapter = new StepCardScrollAdapter();

        SimpleStepView ingredients = new SimpleStepView(this, currentRecipe.getIndigrients());
        ingredients.setFooter("Ingredients");
        adapter.add(ingredients);

        /*VideoStepView videoStepView = new VideoStepView(this, "http://www.leanbackplayer.com/videos/360p/elephants_dream_640x360_2.30.mp4");
        adapter.add(videoStepView);*/

        int stepIndex = 0;

        List<Step> steps = currentRecipe.getSteps();
        for (Step step : steps) {
            stepIndex++;

            // nem túl szép :)
            if (step.getType().equals("video")) {
                VideoStepView videoStepView = new VideoStepView(this, "file:///" + step.getBody());
                adapter.add(videoStepView);
            } else {
                SimpleStepView stepView;

                if (step.getType().equals("textTimer")) {
                    stepView = new TimerStepView(this, step.getBody(), step.getTimer());
                } else {
                    stepView = new SimpleStepView(this, step.getBody());
                }

                if (step.getImage() != null) {
                    Picasso.with(CookingActivity.this).load(step.getImage().getGlassUrl()).into(stepView.getStepImageView());
                }

                stepView.setTimestamp(stepIndex + "/" + steps.size());

                adapter.add(stepView);
            }
        }
    }

    private void itemSelected(View view, int position) {
        if (currentScrollAware != null) {
            currentScrollAware.deactivated();
        }

        currentScrollAware = null;

        if (view instanceof ScrollAware) {
            currentScrollAware = (ScrollAware) view;
            currentScrollAware.activated();
        }

        if (tts != null) {
            tts.stop();
        }

        if (view instanceof SimpleStepView) {
            SimpleStepView simpleStepView = (SimpleStepView) view;
            if (tts != null) {
                tts.speak(simpleStepView.getStepText(), TextToSpeech.QUEUE_FLUSH, null);
            }
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

        public int size() {
            return cards.size();
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

    private class InitializationSequence {
        private boolean recipeDownloaded = false, ttsInitialized = false;

        public InitializationSequence() {
        }

        public void recipeDownloaded() {
            recipeDownloaded = true;
            checkConditions();
        }

        public void ttsInitialized() {
            ttsInitialized = true;
            checkConditions();
        }

        private synchronized void checkConditions() {
            if (recipeDownloaded && ttsInitialized) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initializationDone();
                    }
                });
            }
        }
    }
}