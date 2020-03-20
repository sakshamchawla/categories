package sakshamchawla.com.categories;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity {
    ListView listView;
    public final int resDelete = 2;
    ArrayList<Word> wordList;
    ArrayAdapter<Word> adapter;
    private long lastPressedTime;
    private static final int PERIOD = 2000;
    boolean sortPref = false;
    String importFilePath, exportPath;
    private final int ADDREQCODE = 546;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateHome();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sortPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_EXAMPLE_SWITCH, false);
    }

    void inflateHome() {
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.DLVWords);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DBCon con = new DBCon(MainActivity.this);
        wordList = con.getAllWords();
        adapter = new WordAdapter();
        listView.setAdapter(adapter);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddWord.class);
                MainActivity.this.startActivityForResult(intent, ADDREQCODE);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Word word = wordList.get(position);
                Intent intent = new Intent(MainActivity.this, ShowWord.class);
                intent.putExtra("word", word);
                MainActivity.this.startActivityForResult(intent, 0);
            }
        });
    }

    void refreshList() {
        DBCon con = new DBCon(MainActivity.this);
        wordList = con.getAllWords();
        adapter.clear();
        adapter = new WordAdapter();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == resDelete)
            refreshList();

        if (resultCode == 1 && requestCode == ADDREQCODE)
            refreshList();

        if (requestCode == 0 && resultCode == 1)
            refreshList();

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                Log.i("uri", uri.toString());

                importFilePath = uri != null ? uri.getPath() : null;
                checkAndWork(2);
            }
        }
    }

    void searchOut(String query) {
        System.out.println("Search Query: " + query);
        DBCon con = new DBCon(MainActivity.this);
        if (con.isChildWord(query)) {
            Intent intent = new Intent(MainActivity.this, ExpandWord.class);
            intent.putExtra("word", query);
            startActivity(intent);
        } else if (con.isRootWord(query)) {
            Intent intent = new Intent(MainActivity.this, ShowWord.class);
            intent.putExtra("word", con.getWord(query));
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, SearchNewWord.class);
            intent.putExtra("word", query);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView;

        ArrayList<String> sList = new ArrayList<>();
        for (Word tempWord : wordList) {
            sList.add(tempWord.getWord());
            ArrayList<String> SynList = sortString(tempWord.getSyn());
            sList.addAll(SynList);
            ArrayList<String> AntList = sortString(tempWord.getAnt());
            sList.addAll(AntList);
            ArrayList<String> RelList = sortString(tempWord.getRel());
            sList.addAll(RelList);
        }
        Set<String> hs = new HashSet<>(sList);
        sList.clear();
        sList.addAll(hs);
        MenuItem searchMenu = menu.findItem(R.id.action_search);

        // Get SearchView object.
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);

        // Get SearchView autocomplete object.
        final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        //searchAutoComplete.setBackgroundColor(Color.WHITE);
        //searchAutoComplete.setTextColor(Color.BLACK);
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.background_light);


        ArrayAdapter<String> newsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, sList);
        searchAutoComplete.setAdapter(newsAdapter);
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString = (String) adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText("" + queryString);
                searchOut(queryString);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // setContentView(R.layout.activity_result);
                //show results
                searchOut(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    ArrayList<String> sortString(String input) {
        ArrayList<String> temp = new ArrayList<>();
        StringTokenizer token = new StringTokenizer(input, ",");
        while (token.hasMoreTokens()) {
            temp.add(token.nextToken().trim());
        }
        SharedPreferences sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        boolean sortPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_EXAMPLE_SWITCH, false);
        if (sortPref)
            Collections.sort(temp, String.CASE_INSENSITIVE_ORDER);
        return temp;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            inflateHome();
            if (event.getDownTime() - lastPressedTime < PERIOD) {
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Press again to exit.", Toast.LENGTH_SHORT).show();
                lastPressedTime = event.getEventTime();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean isStoragePermissionGranted(int RequestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("perm", "Permission is granted bruh");
                performFileSearch(RequestCode);
                //checkAndWork(RequestCode);
                return true;
            }
            else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("perm", "Read Permission is granted bruh");
                performFileSearch(RequestCode);
                //checkAndWork(RequestCode);
                return true;
            }
            else {

                Log.v("perm", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, RequestCode);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("perm", "Permission is granted");
            performFileSearch(RequestCode);
            //checkAndWork(RequestCode);
            return true;
        }
    }

    private static final int READ_REQUEST_CODE = 42;


    public void performFileSearch(int RequestCode) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.addCategory(Intent.FILE);
        //intent.setType("*/*");
        if (RequestCode == 2)
            startActivityForResult(intent, READ_REQUEST_CODE);
        else
            checkAndWork(1);
    }

    void checkAndWork(int requestCode) {
        /*if (requestCode == 1) {
            try {
                final String inFileName = this.getDatabasePath("wordDB").getPath();
                System.out.println("Exporting");
                File dbFile = new File(inFileName);
                FileInputStream fis = new FileInputStream(dbFile);

                String outFileName = Environment.getExternalStorageDirectory() + "/database_copy.db";

                // Open the empty db as the output stream
                OutputStream output = new FileOutputStream(outFileName);

                // Transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                System.out.println("File Written to: " + Environment.getExternalStorageDirectory());
                Toast.makeText(this, "Exported to: " + outFileName, Toast.LENGTH_SHORT).show();
                // Close the streams
                output.flush();
                output.close();
                fis.close();
            } catch (IOException ex) {
                Toast.makeText(this, "Error in export", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        } else*/
        if (requestCode == 2) {
            Log.d("file", "Selected file: " + importFilePath);
          //  importFilePath = "/storage/emulated/0/Download/cat_db_backup_18-07-18_19-35.db";
            if (importFilePath != null) {
                try {
                    //       final String inFileName = Environment.getExternalStorageDirectory() + "/database_copy.db";
                    System.out.println("Importing");

                    File dbFile = new File(importFilePath);

                    FileInputStream fis = new FileInputStream(dbFile);
                    File data = Environment.getDataDirectory();
                    String currentDBPath = "//data/" + this.getPackageName() + "/databases/wordDB";
                    File currentDB = new File(data, currentDBPath);

                    OutputStream output = new FileOutputStream(currentDB);
                    FileChannel src = new FileInputStream(importFilePath).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    System.out.println("File Written to: " + this.getDatabasePath("wordDB").getPath());
                    Toast.makeText(this, "Imported Database", Toast.LENGTH_SHORT).show();
                    inflateHome();
                    // Close the streams
                    output.flush();
                    output.close();
                    fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else Toast.makeText(this, "Path Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Request Code: " + requestCode);
            Log.v("perm", "Permission: " + permissions[0] + "was " + grantResults[0]);
            performFileSearch(requestCode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.MNExport:
                //isStoragePermissionGranted(1);
                Intent ExportIntent = new Intent(MainActivity.this, ExportActivity.class);
                startActivity(ExportIntent);
                break;
            case R.id.MNImport:
                isStoragePermissionGranted(2);
                break;
            case R.id.MNSettings:
                Intent SettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(SettingsIntent);
                break;
            case R.id.MNShowMarked:
                Intent SMintent = new Intent(MainActivity.this, ShowMarked.class);
                startActivity(SMintent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    class WordAdapter extends ArrayAdapter<Word> {

        public WordAdapter() {
            super(MainActivity.this, R.layout.list_wordlist, wordList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Word word = getItem(position);
            LayoutInflater inflater = getLayoutInflater();
            View itemView = inflater.inflate(R.layout.list_wordlist, null);
            TextView TVWord = (TextView) itemView.findViewById(R.id.DTVWord);
            if (!word.getShortNote().isEmpty())
                TVWord.setText(String.format("%s (%s)", word.getWord(), word.getShortNote()));
            else
                TVWord.setText(word.getWord());
            return itemView;
        }
    }
}
