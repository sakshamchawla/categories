package sakshamchawla.com.categories;

import java.io.Serializable;

public class Meaning implements Serializable {
    String word, meaning, ps;

    Meaning() {

    }

    Meaning(String word, String meaning, String ps) {
        this.word = word;
        this.meaning = meaning;
        this.ps = ps;
    }
}
