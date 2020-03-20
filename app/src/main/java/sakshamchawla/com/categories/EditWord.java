package sakshamchawla.com.categories;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Created by SAKSHAM on 04-Apr-18.
 */

public class EditWord extends AppCompatActivity {
    TextInputEditText ETWord, ETSyn, ETRel, ETAnt, ETShortNote, ETLongNote;
    Button BTSave;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Edit Word");
        }
        final Word word = (Word) getIntent().getSerializableExtra("word");
        ETWord = findViewById(R.id.DETWord);
        ETSyn = findViewById(R.id.DETSyn);
        ETAnt = findViewById(R.id.DETAnt);
        ETRel = findViewById(R.id.DETRel);
        ETShortNote = findViewById(R.id.DETShortNote);
        ETLongNote = findViewById(R.id.DETLongNote);
        BTSave = findViewById(R.id.DBTNewSaveWord);
        ETWord.setText(word.getWord());
        ETSyn.setText(word.getSyn());
        ETAnt.setText(word.getAnt());
        ETRel.setText(word.getRel());
        ETShortNote.setText(word.getShortNote());
        ETLongNote.setText(word.getLongNote());
        BTSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBCon con = new DBCon(EditWord.this);
                con.removeWord(word);
                con.insWord(new Word(ETWord.getText().toString().trim().replace("'", "''"), ETSyn.getText().toString().trim().replace("'", "''"), ETAnt.getText().toString().trim().replace("'", "''"), ETRel.getText().toString().trim().replace("'", "''"), ETShortNote.getText().toString().trim().replace("'", "''"), ETLongNote.getText().toString().trim().replace("'", "''")));
                Intent intent = new Intent();
                intent.putExtra("newword", ETWord.getText().toString().trim());
                setResult(1, intent);
                finish();
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
