/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bukvar.structs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jovan
 */
public class Bukvar implements Serializable {
    
    private static final String dataFileName = "bukvar.bin";
    private static final String frstLessionName = "Прва лекција";
    
    public HashMap<String, Lession> lessions;
    public String defaultLessionName;
    
    public static Bukvar getBukvar() {
        ObjectInputStream ois = null;
        Bukvar bukvar;
        try {
            FileInputStream fis = new FileInputStream(dataFileName);
            ois = new ObjectInputStream(fis);
            bukvar = (Bukvar)ois.readObject();
        } catch (Exception e) {
            System.out.println("Data file not found.");
            bukvar = new Bukvar();
        } finally {
            try { ois.close(); } catch (Exception e) {} 
        }
        return bukvar;
    }

    private Bukvar() {this.lessions = new HashMap<>();
        this.defaultLessionName = frstLessionName;
        this.lessions.put(frstLessionName, new Lession(frstLessionName));
    }
    
    public void save() {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(dataFileName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
        } catch (Exception e) {
            System.out.println("Error dumping data file.");
        } finally {
            try { fout.close(); } catch (IOException ex) {}
        }
    }

    public boolean deleteLession(String name) {
        if (!name.equals(defaultLessionName)) {
            lessions.remove(name);
            return true;
        }
        return false;
    }
    
}
