package sakshamchawla.com.categories;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class AddWord extends AppCompatActivity {
    TextInputEditText ETWord, ETSyn, ETAnt, ETRel, ETShortNote, ETLongNote;
    Button BTSave;
    ArrayList<String> SynList, AntList, RelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);
        ETWord = findViewById(R.id.DETWord);
        ETSyn = findViewById(R.id.DETSyn);
        ETAnt = findViewById(R.id.DETAnt);
        ETRel = findViewById(R.id.DETRel);
        ETShortNote = findViewById(R.id.DETShortNote);
        ETLongNote = findViewById(R.id.DETLongNote);
        BTSave = findViewById(R.id.DBTNewSaveWord);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        BTSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Word newWord;
                String word = ETWord.getText().toString().trim().toLowerCase().replace("'", "''");
                if (!word.isEmpty()) {
                    String SynListStr = ETSyn.getText().toString().trim().toLowerCase().replace("'", "''");
                    String AntListStr = ETAnt.getText().toString().trim().toLowerCase().replace("'", "''");
                    String RelListStr = ETRel.getText().toString().trim().toLowerCase().replace("'", "''");
                    String ShortNote = ETShortNote.getText().toString().trim().toLowerCase().replace("'", "''");
                    String LongNote = ETLongNote.getText().toString().trim().toLowerCase().replace("'", "''");


                    newWord = new Word(word, SynListStr, AntListStr, RelListStr, ShortNote, LongNote);
                    DBCon con = new DBCon(AddWord.this);

                    con.insWord(newWord);
                    System.out.println("Insert successful");
                    setResult(1);
                    finish();


                } else {
                    ETWord.setError("Cannot be empty");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
