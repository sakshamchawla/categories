package sakshamchawla.com.categories;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ExpandWord extends AppCompatActivity {
    LinearLayout LLMeaning;
    ListView LVSyn, LVAnt, LVRel;
    TextView TVWord, TVMeaning, TVPS;
    ArrayAdapter<String> SynAdapter, AntAdapter, RelAdapter;
    String Word;
    TextToSpeech tts;
    ProgressBar PBMeaningLoad;
    String callFrom = "", callHead = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand_word);
        Word = getIntent().getSerializableExtra("word").toString();
        try {
            callFrom = getIntent().getSerializableExtra("callFrom").toString();
            callHead = getIntent().getSerializableExtra("callHead").toString();
        } catch (Exception ex) {

        }
        DBCon con = new DBCon(ExpandWord.this);
        final EWord eWord = con.getEWord(Word);
        LVSyn = findViewById(R.id.DEWLVShowSyns);
        LVAnt = findViewById(R.id.DEWLVShowAnts);
        LVRel = findViewById(R.id.DEWLVShowRels);
        TVWord = findViewById(R.id.DEWTVWord);
        LLMeaning = findViewById(R.id.DEWLLMeaning);
        TVPS = findViewById(R.id.DEWTVPS);
        LLMeaning.setVisibility(View.GONE);
        TVMeaning = findViewById(R.id.DEWTVMeanings);
        PBMeaningLoad = findViewById(R.id.DPBEW);
        PBMeaningLoad.setVisibility(View.VISIBLE);
        TVMeaning.setVisibility(View.GONE);
        TVPS.setVisibility(View.GONE);
        TVMeaning.setMovementMethod(new ScrollingMovementMethod());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(Word);
        }
        Meaning Rmeaning = con.getMeaning(Word);
        if (Rmeaning == null) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                LLMeaning.setVisibility(View.VISIBLE);
                WordMeaning wordMeaning = new WordMeaning();
                wordMeaning.execute(Word);

            } else {
                Toast.makeText(this, "Can't fetch meaning. Check your internet connection.", Toast.LENGTH_LONG).show();
            }
        } else {
            inflateMeaning(Rmeaning);
        }
        TVWord.setText(Word);
        SynAdapter = new ArrayAdapter<String>(ExpandWord.this, R.layout.layout_wordcat, eWord.getAsSyn());
        LVSyn.setAdapter(SynAdapter);
        LVSyn.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Listening");
                if (!eWord.asSyn.get(position).equals(callHead))
                    startSwordAct(eWord.asSyn.get(position));
                else finish();
            }
        });
        AntAdapter = new ArrayAdapter<String>(ExpandWord.this, R.layout.layout_wordcat, eWord.getAsAnt());
        LVAnt.setAdapter(AntAdapter);
        LVAnt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!eWord.asAnt.get(position).equals(callHead))
                    startSwordAct(eWord.asAnt.get(position));
                else finish();
            }
        });
        RelAdapter = new ArrayAdapter<String>(ExpandWord.this, R.layout.layout_wordcat, eWord.getAsRel());
        LVRel.setAdapter(RelAdapter);
        LVRel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!eWord.asRel.get(position).equals(callHead))
                    startSwordAct(eWord.asRel.get(position));
                else finish();
            }
        });
    }

    void startSwordAct(String word) {
        DBCon con = new DBCon(ExpandWord.this);
        Intent intent = new Intent(ExpandWord.this, ShowWord.class);
        intent.putExtra("word", con.getWord(word));
        startActivity(intent);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.MNEWTTSWord:
                tts = new TextToSpeech(ExpandWord.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {

                            int result = tts.setLanguage(Locale.US);

                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "This Language is not supported");
                            } else {

                                speakOut();
                            }

                        } else {
                            Log.e("TTS", "Initilization Failed!");
                        }
                    }

                });

                break;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    void speakOut() {
        tts.setLanguage(Locale.US);
        tts.speak(Word, TextToSpeech.QUEUE_ADD, null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tts, menu);
        return super.onCreateOptionsMenu(menu);
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
        }

        void cacheMeaning(Meaning meaning) {
            DBCon con = new DBCon(getBaseContext());
            con.addMeaning(meaning);
        }
    }
}
