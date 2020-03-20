package sakshamchawla.com.categories;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ShowWord extends AppCompatActivity {
    Word word;
    TextView TVWord, TVPS, TVMeaning, TVMeaningHead, TVShortNote, TVLongNote;
    ArrayList<String> SynList, AntList, RelList;
    LinearLayout LLMeaning;
    RecyclerView RVSyn, RVAnt, RVRel;
    LinearLayoutManager SynLLManager, RelLLManager, AntLLManager;
    RLAdapter synAdapter, antAdapter, relAdapter;
    int height;
    Meaning Rmeaning;
    TextToSpeech tts;
    ProgressBar PBMeaningLoad;
    private int EDITED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_word);

        Intent intent = getIntent();
        word = (Word) intent.getSerializableExtra("word");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(word.getWord());
        }
        PBMeaningLoad = findViewById(R.id.DPBShowWord);
        PBMeaningLoad.setVisibility(View.VISIBLE);
        TVPS = findViewById(R.id.DTVPS);
        TVMeaning = findViewById(R.id.DTVMeanings);
        TVMeaning.setVisibility(View.GONE);
        TVPS.setVisibility(View.GONE);
        TVMeaningHead = findViewById(R.id.DTVMeaningHead);
        TVShortNote = findViewById(R.id.DSWTVShortNote);
        TVLongNote = findViewById(R.id.DSWTVLongNote);
        RVSyn = findViewById(R.id.DRVShowSyns);
        RVAnt = findViewById(R.id.DRVShowAnts);
        RVRel = findViewById(R.id.DRVShowRels);
        RVSyn.setHasFixedSize(true);
        RVAnt.setHasFixedSize(true);
        RVRel.setHasFixedSize(true);
        TVMeaning.setMovementMethod(ScrollingMovementMethod.getInstance());
        SynLLManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        AntLLManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        RelLLManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        RVSyn.setLayoutManager(SynLLManager);
        RVAnt.setLayoutManager(AntLLManager);
        RVRel.setLayoutManager(RelLLManager);
        TVMeaning.setMovementMethod(new ScrollingMovementMethod());
        LLMeaning = findViewById(R.id.DLLMeaning);
        LLMeaning.setVisibility(View.GONE);
        DBCon con = new DBCon(this);
        Rmeaning = con.getMeaning(word.getWord());
        if (Rmeaning == null) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                LLMeaning.setVisibility(View.VISIBLE);
                WordMeaning wordMeaning = new WordMeaning();
                wordMeaning.execute(word.getWord());

            } else {
                Toast.makeText(this, "Can't fetch meaning. Check your internet connection.", Toast.LENGTH_LONG).show();
            }
        } else {
            inflateMeaning(Rmeaning);
        }
        inflateViews(word);

    }

    void inflateMeaning(Meaning iMeaning) {
        LLMeaning.setVisibility(View.VISIBLE);
        PBMeaningLoad.setVisibility(View.GONE);
        TVMeaning.setVisibility(View.VISIBLE);
        TVPS.setVisibility(View.VISIBLE);
        if (iMeaning != null) {
            if (!iMeaning.ps.isEmpty())
                TVPS.setText(iMeaning.ps.substring(0, iMeaning.ps.length() - 2));
            StringTokenizer meaningTokenizer = new StringTokenizer(iMeaning.meaning, "?");
            System.out.println("Root Meaning: " + iMeaning.meaning);
            String longmeaning = "";
            int i = 1;
            while (meaningTokenizer.hasMoreTokens()) {
                longmeaning += i + ". " + meaningTokenizer.nextToken().trim() + "\n";
                i++;
            }
            System.out.println("Long Meaning: " + longmeaning);
            longmeaning = longmeaning.trim();
            TVMeaning.setText(longmeaning);
        } else {

            TVMeaning.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            TVMeaning.setText("No Meaning found.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_showword, menu);
        MenuItem item = menu.findItem(R.id.MNStar);
        DBCon con = new DBCon(ShowWord.this);
        if (con.isFav(word.getWord()))
            item.setIcon(android.R.drawable.btn_star_big_on);
        else
            item.setIcon(android.R.drawable.btn_star_big_off);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.MNEditWord:
                Intent intent = new Intent(ShowWord.this, EditWord.class);
                intent.putExtra("word", word);
                startActivityForResult(intent, 1);
                break;
            case R.id.MNDeleteWord:
                new AlertDialog.Builder(this)
                        .setTitle("Delete")
                        .setMessage("Delete \"" + word.getWord() + "\"?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DBCon con = new DBCon(ShowWord.this);
                                con.removeWord(word);
                                EDITED = 1;
                                setResult(EDITED);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            case R.id.MNTTSWord:
                setTTS(0);
                break;
            case R.id.MNPlay:
                setTTS(1);
                break;
            case android.R.id.home:
                finish();
            case R.id.MNStar:
                DBCon con = new DBCon(ShowWord.this);
                if (con.isFav(word.getWord())) {
                    System.out.println("Word unmarked");
                    item.setIcon(android.R.drawable.btn_star_big_off);
                    con.removeFav(word.getWord());
                } else {
                    System.out.println("Word marked");
                    item.setIcon(android.R.drawable.btn_star_big_on);
                    con.addFav(word.getWord());
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    void setTTS(final int i) {
        tts = new TextToSpeech(ShowWord.this, new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        if (i == 0)
                            speakWord();
                        else
                            speakEverything();
                    }
                } else

                {
                    Log.e("TTS", "Initilization Failed!");
                }

            }
        });
    }


    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        setResult(EDITED);
        super.onDestroy();
    }

    void speakWord() {
        tts.setLanguage(Locale.US);
        tts.speak(word.getWord(), TextToSpeech.QUEUE_ADD, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void speakEverything() {
        tts.setLanguage(Locale.US);
        String toSpeak = word.getWord() + ".";
        tts.speak(toSpeak, TextToSpeech.QUEUE_ADD, null);

        if (!word.getSyn().isEmpty()) {
            String toSpeakSyn = String.join(",", SynList);
            toSpeak = "Synonyms. " + toSpeakSyn;
            speakDetails(toSpeak);
        }
        if (!word.getAnt().isEmpty()) {
            String toSpeakAnt = String.join(",", AntList);
            toSpeak = "Antonyms. " + toSpeakAnt;
            speakDetails(toSpeak);
        }
        if (!word.getRel().isEmpty()) {
            String toSpeakRel = String.join(",", RelList);
            toSpeak = "Related. " + toSpeakRel;
            speakDetails(toSpeak);
        }
    }

    void speakDetails(String toSpeak) {

       /* try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        tts.setSpeechRate(0.75f);
        tts.speak(toSpeak, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DBCon con = new DBCon(ShowWord.this);
        if (requestCode == 1 && resultCode == 1) {
            Word newword = con.getWord(data.getSerializableExtra("newword").toString());
            EDITED = 1;
            setResult(EDITED);
            word = newword;
            inflateViews(newword);
        }
    }

    ArrayList<String> sortString(String input) {
        ArrayList<String> temp = new ArrayList<>();
        StringTokenizer token = new StringTokenizer(input, ",");
        while (token.hasMoreTokens()) {
            temp.add(token.nextToken().trim());
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean sortPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_EXAMPLE_SWITCH, false);
        if (sortPref)
            Collections.sort(temp, String.CASE_INSENSITIVE_ORDER);
        return temp;
    }

    void inflateViews(Word word) {
        TVWord = findViewById(R.id.DTVWord);
        TextView TVSNHead = findViewById(R.id.DTVSNHead);
        TextView TVLNHead = findViewById(R.id.DTVLNHead);
        TVWord.setText(word.getWord());
        if (word.getShortNote().isEmpty()) {
            TVSNHead.setVisibility(View.GONE);
            TVShortNote.setVisibility(View.GONE);
        } else
            TVShortNote.setText(word.getShortNote());
        if (word.getLongNote().isEmpty()) {
            TVLNHead.setVisibility(View.GONE);
            TVLongNote.setVisibility(View.GONE);
        } else {
            TVLongNote.setText(word.getLongNote());
            TVLongNote.setMovementMethod(new ScrollingMovementMethod());
        }

        SynList = sortString(word.getSyn());
        AntList = sortString(word.getAnt());
        RelList = sortString(word.getRel());
        synAdapter = new RLAdapter(SynList);
        antAdapter = new RLAdapter(AntList);
        relAdapter = new RLAdapter(RelList);
        RVSyn.setAdapter(synAdapter);
        RVSyn.addOnItemTouchListener(
                new RecyclerItemClickListener(this, RVSyn, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        startEwordAct(SynList.get(position));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                })
        );
        RVAnt.setAdapter(antAdapter);
        RVAnt.addOnItemTouchListener(
                new RecyclerItemClickListener(this, RVSyn, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        startEwordAct(AntList.get(position));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                })
        );
        RVRel.setAdapter(relAdapter);
        RVRel.addOnItemTouchListener(
                new RecyclerItemClickListener(this, RVSyn, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        startEwordAct(RelList.get(position));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                })
        );

    }


    void startEwordAct(String word) {
        Intent intent = new Intent(ShowWord.this, ExpandWord.class);
        intent.putExtra("word", word);
        intent.putExtra("callFrom", "ShowWord");
        intent.putExtra("callHead", Objects.requireNonNull(getSupportActionBar()).getTitle());
        startActivity(intent);
    }

    class WordMeaning extends AsyncTask<String, Void, ArrayList<Meaning>> {

        String word = null;

        @Override
        protected ArrayList<Meaning> doInBackground(String... strings) {
            word = strings[0];
            String result = "";
            ArrayList<Meaning> meaningsList = new ArrayList<>();

            try {
                DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
                DocumentBuilder b = f.newDocumentBuilder();
                Document doc = b.parse("https://www.dictionaryapi.com/api/v1/references/collegiate/xml/" + word + "?key=7c33277d-3064-4bef-848e-95742b5cbebc");

                doc.getDocumentElement().normalize();

                NodeList items = doc.getElementsByTagName("entry");
                for (int i = 0; i < items.getLength(); i++) {
                    Node n = items.item(i);

                    if (n.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    Element e = (Element) n;
                    Meaning meaning;
                    String tempPS = "", tempMeaning = "";
                    NodeList psList = e.getElementsByTagName("fl");
                    for (int j = 0; j < psList.getLength(); j++) {
                        Node dt = psList.item(j);
                        if (dt.getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element psElem = (Element) psList.item(j);
                        Node psNode = psElem.getChildNodes().item(0);
                        //System.out.println(psNode.getNodeValue());
                        tempPS += psNode.getNodeValue() + ", ";
                    }
                    NodeList titleList = e.getElementsByTagName("dt");
                    for (int j = 0; j < titleList.getLength(); j++) {
                        Node dt = titleList.item(j);
                        if (dt.getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element titleElem = (Element) titleList.item(j);
                        Node titleNode = titleElem.getChildNodes().item(0);
                        //System.out.println(titleNode.getNodeValue().replace(":", ""));
                        tempMeaning += titleNode.getNodeValue().replace(":", "") + "?";
                    }
                    NodeList titleList1 = e.getElementsByTagName("sx");
                    for (int j = 0; j < titleList1.getLength(); j++) {
                        Node dt = titleList1.item(j);
                        if (dt.getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element titleElem = (Element) titleList1.item(j);
                        Node titleNode = titleElem.getChildNodes().item(0);
                        //System.out.println(titleNode.getNodeValue().replace(":", ""));
                        tempMeaning += titleNode.getNodeValue().replace(":", "") + "?";
                    }
                    NodeList titleList2 = e.getElementsByTagName("d_link");
                    for (int j = 0; j < titleList2.getLength(); j++) {
                        Node dt = titleList2.item(j);
                        if (dt.getNodeType() != Node.ELEMENT_NODE)
                            continue;
                        Element titleElem = (Element) titleList2.item(j);
                        Node titleNode = titleElem.getChildNodes().item(0);
                        //System.out.println(titleNode.getNodeValue().replace(":", ""));
                        tempMeaning += titleNode.getNodeValue().replace(":", "") + "?";
                    }
                    meaning = new Meaning(word, tempPS, tempMeaning);
                    meaningsList.add(meaning);
                    System.out.println("PS: " + tempPS + "\nMeaning: " + tempMeaning);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return meaningsList;
        }

        @Override
        protected void onPostExecute(ArrayList<Meaning> meaningsList) {
            super.onPostExecute(meaningsList);
            if (!meaningsList.isEmpty()) {
                String PS = "", Meaning = "";
                for (Meaning temp : meaningsList) {
                    PS += temp.ps;
                    Meaning += temp.meaning;
                }

                Meaning tempMeaning = new Meaning(meaningsList.get(0).word, PS, Meaning);
                inflateMeaning(tempMeaning);
                cacheMeaning(tempMeaning);
            } else inflateMeaning(null);
       /*     XmlPullParserFactory xmlFactoryObject = null;
            Meaning meaning = null;
            try {
                xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser parser = xmlFactoryObject.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(result));
                int event = parser.getEventType();
                meaning = new Meaning();
                meaning.meaning = "";
                meaning.ps = "";
                meaning.word = word;
                while (event != XmlPullParser.END_DOCUMENT) {
                    String name;
                    switch (event) {
                        case XmlPullParser.START_TAG:
                            name = parser.getName();
                            if ("entry".equals(name)) {

                            } else if (meaning != null) {
                                if ("fl".equals(name)) {
                                    meaning.ps += parser.nextText() + ", ";
                                    System.out.println("PS: " + meaning.ps);
                                } else if ("dt".equals(name)) {
                                    try {
                                        meaning.meaning += parser.nextText().replace(":", "") + "?";
                                        System.out.println("Meaning: " + meaning.meaning);
                                    } catch (Exception e) {
                                        continue;
                                    }
                                } else if ("sx".equals(name)) {
                                    try {
                                        meaning.meaning += parser.nextText().replace(":", "") + "?";
                                        System.out.println("Meaning: " + meaning.meaning);
                                    } catch (Exception e) {
                                        continue;
                                    }
                                }
                            }
                            break;
                    }
                    event = parser.next();
                }

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            if (meaning != null) {
                cacheMeaning(meaning);
                inflateMeaning(meaning);
            }*/


        }

        void cacheMeaning(Meaning meaning) {
            DBCon con = new DBCon(getBaseContext());
            con.addMeaning(meaning);
        }
    }

    class RLAdapter extends RecyclerView.Adapter<RLAdapter.ViewHolder> {
        private ArrayList<String> wordlist;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView TVWord;

            public ViewHolder(TextView v) {
                super(v);
                TVWord = v;
            }
        }

        public RLAdapter(ArrayList<String> wordlist) {
            this.wordlist = wordlist;
        }

        @Override
        public RLAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wordcat, parent, false);

            return new ViewHolder(v);

        }

        @Override
        public void onBindViewHolder(RLAdapter.ViewHolder holder, int position) {
            holder.TVWord.setText(new StringBuilder().append(position + 1).append(".  ").append(wordlist.get(position)).toString());

        }

        @Override
        public int getItemCount() {
            return wordlist.size();
        }
    }
}

class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);

        public void onLongItemClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
