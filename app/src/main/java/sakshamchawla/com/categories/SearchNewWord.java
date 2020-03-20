package sakshamchawla.com.categories;

import android.content.Context;
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
import android.widget.LinearLayout;
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

public class SearchNewWord extends AppCompatActivity {
    LinearLayout LLMeaning;
    TextView TVWord, TVMeaning, TVPS;
    TextToSpeech tts;
    String Word;

    ProgressBar PBMeaningLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_new_word);
        Word = getIntent().getSerializableExtra("word").toString();
        Word = Word.replace(" ", "-");
        DBCon con = new DBCon(SearchNewWord.this);
        TVWord = findViewById(R.id.DSNTVWord);
        LLMeaning = findViewById(R.id.DSNLLMeaning);
        LLMeaning.setVisibility(View.GONE);
        TVMeaning = findViewById(R.id.DSNTVMeanings);
        PBMeaningLoad = findViewById(R.id.DPBSNW);
        PBMeaningLoad.setVisibility(View.VISIBLE);
        TVMeaning.setVisibility(View.GONE);

        TVMeaning.setMovementMethod(new ScrollingMovementMethod());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        TVPS = findViewById(R.id.DSNTVPS);
        TVPS.setVisibility(View.GONE);
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
                tts = new TextToSpeech(SearchNewWord.this, new TextToSpeech.OnInitListener() {
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
            word = word.trim().toLowerCase();
            if (word.matches("[0-9A-Za-z-]+")) {
                System.out.println("Word Matches");
                String result = "";
                ArrayList<Meaning> meaningsList = new ArrayList<>();
            /*try {

                URL url = new URL("https://www.dictionaryapi.com/api/v1/references/collegiate/xml/" + word + "?key=7c33277d-3064-4bef-848e-95742b5cbebc");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    int charCode = 0;
                    while (charCode != 1) {
                        charCode = inputStream.read();
                        if (charCode == -1) {
                            break;
                        } else
                            result += (char) charCode;
                    }
                    // System.out.println(result);
                    httpURLConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/
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
            return null;

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
}
