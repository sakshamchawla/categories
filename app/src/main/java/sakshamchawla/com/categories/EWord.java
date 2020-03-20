package sakshamchawla.com.categories;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by SAKSHAM on 05-Apr-18.
 */

public class EWord implements Serializable {
    ArrayList<String> asSyn, asAnt, asRel;

    EWord(ArrayList<String> asSyn, ArrayList<String> asAnt, ArrayList<String> asRel) {
        this.asSyn = asSyn;
        this.asAnt = asAnt;
        this.asRel = asRel;
    }

    public ArrayList<String> getAsAnt() {
        return asAnt;
    }

    public ArrayList<String> getAsRel() {
        return asRel;
    }

    public ArrayList<String> getAsSyn() {
        return asSyn;
    }

    public void setAsAnt(ArrayList<String> asAnt) {
        this.asAnt = asAnt;
    }

    public void setAsRel(ArrayList<String> asRel) {
        this.asRel = asRel;
    }

    public void setAsSyn(ArrayList<String> asSyn) {
        this.asSyn = asSyn;
    }
}
