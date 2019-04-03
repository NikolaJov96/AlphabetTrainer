/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bukvar.structs;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author jovan
 */
public class Bukvar implements Serializable {
    
    public ArrayList<Lession> lessions;
    public int defaultLessionId;

    public Bukvar() {
        this.lessions = new ArrayList<>();
        this.defaultLessionId = 0;
        
        this.lessions.add(new Lession("Прва лекција", 0, 4, 1));
    }
    
}
