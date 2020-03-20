package sakshamchawla.com.categories;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by SAKSHAM on 30-Mar-18.
 */

public class DBCon extends SQLiteOpenHelper {
    private static String NAME = "wordDB";
    private static int VERSION = 4;

    public DBCon(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table wordlist (word text, syn text, ant text, rel text, snote text, lnote text)");
        db.execSQL("create table meaninglist (word text, meaning text, ps text)");
        db.execSQL("create table favList (word text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1)
            db.execSQL("create table favList (word text)");
        if (oldVersion > 1)
            db.execSQL("delete from meaninglist");

    }

    Cursor cursor;
    SQLiteDatabase db;

    public ArrayList<Word> getAllWords() {
        ArrayList<Word> List = new ArrayList<>();
        try {
            db = getReadableDatabase();
            cursor = db.rawQuery("select * from wordlist order by word asc", null);
            while (cursor.moveToNext()) {
                String word = cursor.getString(0);
                String syn = cursor.getString(1);
                String ant = cursor.getString(2);
                String rel = cursor.getString(3);
                String shortNote = cursor.getString(4);
                String longNote = cursor.getString(5);
                Word temp = new Word(word, syn, ant, rel, shortNote, longNote);
                System.out.println("Fetched Word: " + word);
                List.add(temp);
            }
        } catch (SQLiteException ex) {
            System.out.println("Read Error!" + ex.getMessage());
        } finally {
            cursor.close();
            db.close();
        }
        return List;
    }

    void insWord(Word temp) {
        try {
            db = getWritableDatabase();
            String query = "insert into wordlist values('" + temp.getWord() + "','" + temp.getSyn() + "','" + temp.getAnt() + "','" + temp.getRel() + "','" + temp.getShortNote() + "','" + temp.getLongNote() + "')";
            db.execSQL(query);

        } catch (SQLiteException ex) {
            System.out.println("Write Error!" + ex.getMessage());
        } finally {
            db.close();
        }
    }

    boolean isRootWord(String word) {
        boolean res = false;
        try {
            db = getReadableDatabase();
            String query = "select * from wordlist where word = '" + word + "'";
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                res = true;
            }
        } catch (SQLException ex) {
            System.out.println("Read Error" + ex.getMessage());
        } finally {
            db.close();
        }
        return res;
    }

    boolean isChildWord(String word) {
        EWord eword = getEWord(word);
        return !eword.asAnt.isEmpty() || !eword.asSyn.isEmpty() || !eword.asRel.isEmpty();
    }

    void removeWord(Word word) {
        try {
            db = getWritableDatabase();
            String query = "delete from wordlist where word = '" + word.getWord() + "' and snote = '" + word.getShortNote() + "'";
            db.execSQL(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    Word getWord(String word) {
        Word wholeWord = null;
        try {
            db = getReadableDatabase();
            String query = "select * from wordlist where word = '" + word + "'";
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                String newword = cursor.getString(0);
                String syn = cursor.getString(1);
                String ant = cursor.getString(2);
                String rel = cursor.getString(3);
                String shortNote = cursor.getString(4);
                String longNote = cursor.getString(5);
                wholeWord = new Word(newword, syn, ant, rel, shortNote, longNote);
            }
        } catch (SQLException ex) {
            System.out.println("Read Error" + ex.getMessage());
        } finally {
            db.close();
        }
        return wholeWord;
    }

    public EWord getEWord(String word) {
        EWord tempEWord = null;
        ArrayList<String> asSyn, asAnt, asRel;
        asSyn = new ArrayList<>();
        asAnt = new ArrayList<>();
        asRel = new ArrayList<>();
        try {
            db = getWritableDatabase();
            String query = "select * from wordlist";
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                String newword = cursor.getString(0);
                String syn = cursor.getString(1);
                String ant = cursor.getString(2);
                String rel = cursor.getString(3);
                StringTokenizer synToken = new StringTokenizer(syn, ",");
                while (synToken.hasMoreTokens()) {
                    String temp = synToken.nextToken().trim();
                    if (temp.equalsIgnoreCase(word)) {
                        asSyn.add(newword);
                    }
                }
                StringTokenizer antToken = new StringTokenizer(ant, ",");
                while (antToken.hasMoreTokens()) {
                    String temp = antToken.nextToken().trim();
                    if (temp.equalsIgnoreCase(word)) {
                        asAnt.add(newword);
                    }
                }
                StringTokenizer relToken = new StringTokenizer(rel, ",");
                while (relToken.hasMoreTokens()) {
                    String temp = relToken.nextToken().trim();
                    if (temp.equalsIgnoreCase(word)) {
                        asRel.add(newword);
                    }
                }

            }

        } catch (SQLException ex) {

        }
        tempEWord = new EWord(asSyn, asAnt, asRel);
        return tempEWord;
    }

    void addMeaning(Meaning meaning) {
        try {

            db = getWritableDatabase();
            //meaning.meaning.replaceAll("'","");
            if (meaning.meaning.contains("'")) {
                System.out.println("' exists");
                meaning.meaning = meaning.meaning.replace("'", "''");

            }
            String query = "insert into meaninglist values('" + meaning.word + "','" + meaning.meaning + "','" + meaning.ps + "')";
            String sql = "CREATE TABLE table_name (column_1 INTEGER PRIMARY KEY, column_2 TEXT)";
            SQLiteStatement stmt = db.compileStatement(query);
            stmt.execute();

            System.out.println("Query: " + query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    Meaning getMeaning(String word) {
        Meaning temp = null;
        try {
            db = getReadableDatabase();
            String query = "select * from meaninglist where word = '" + word + "'";
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                String meaning = cursor.getString(1);
                String ps = cursor.getString(2);
                temp = new Meaning(word, meaning, ps);
            }
        } catch (SQLException ex) {

        }
        return temp;
    }

    boolean isFav(String word) {
        boolean res = false;
        try {
            db = getReadableDatabase();
            String query = "select * from favList where word = '" + word + "'";
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                res = true;
            }
        } catch (SQLException ex) {
            System.out.println("Read Error" + ex.getMessage());
        } finally {
            db.close();
        }
        return res;
    }

    void addFav(String word) {
        try {
            db = getWritableDatabase();
            String query = "insert into favList values ('" + word + "')";
            SQLiteStatement stmt = db.compileStatement(query);
            stmt.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    void removeFav(String word) {
        try {
            db = getWritableDatabase();
            String query = "delete from favList where word = '" + word + "'";
            SQLiteStatement stmt = db.compileStatement(query);
            stmt.execute();
            db.execSQL(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    ArrayList<String> getFavList() {
        ArrayList<String> temp = new ArrayList<>();
        try {
            db = getReadableDatabase();
            String query = "select * from favList;";
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                temp.add(cursor.getString(0));
                System.out.println(cursor.getString(0));
            }
        } catch (SQLException ex) {

        }
        return temp;
    }
}
