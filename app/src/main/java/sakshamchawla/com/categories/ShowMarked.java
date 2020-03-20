package sakshamchawla.com.categories;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ShowMarked extends AppCompatActivity {
    ListView listView;

    ArrayAdapter<String> adapter;
    ArrayList<String> wordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.DLVWords);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Marked Words");
        }
        final DBCon con = new DBCon(ShowMarked.this);
        wordList = con.getFavList();
        adapter = new WordAdapter();
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String word = wordList.get(position);
                Intent intent = new Intent(ShowMarked.this, ShowWord.class);
                intent.putExtra("word", con.getWord(word));
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class WordAdapter extends ArrayAdapter<String> {

        public WordAdapter() {
            super(ShowMarked.this, R.layout.list_wordlist, wordList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            String word = getItem(position);
            LayoutInflater inflater = getLayoutInflater();
            View itemView = inflater.inflate(R.layout.list_wordlist, null);
            TextView TVWord = (TextView) itemView.findViewById(R.id.DTVWord);
            TVWord.setText(word);
            return itemView;
        }
    }
}
