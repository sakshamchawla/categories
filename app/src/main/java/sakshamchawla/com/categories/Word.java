package sakshamchawla.com.categories;

import java.io.Serializable;

/**
 * Created by SAKSHAM on 30-Mar-18.
 */

public class Word implements Serializable {
    private String word, meaning, syn, ant, rel;
    private String shortNote, longNote;

    Word(String word, String syn, String ant, String rel, String shortNote, String longNote) {

        this.word = word;
        this.syn = syn;
        this.ant = ant;
        this.rel = rel;
        this.shortNote = shortNote;
        this.longNote = longNote;
    }

    public void setLongNote(String longNote) {
        this.longNote = longNote;
    }

    public void setShortNote(String shortNote) {
        this.shortNote = shortNote;
    }


    public void setAnt(String ant) {
        this.ant = ant;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public void setSyn(String syn) {
        this.syn = syn;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getAnt() {
        return ant;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getRel() {
        return rel;
    }

    public String getSyn() {
        return syn;
    }

    public String getWord() {
        return word;
    }

    public String getLongNote() {
        return longNote;
    }

    public String getShortNote() {
        return shortNote;
    }
}
