/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bukvar.structs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import javafx.scene.image.Image;

/**
 *
 * @author jovan
 */
public class Bukvar implements Serializable {
    
    private static final String dataFileName = "bukvar.bin";
    private static final String firstLessionName = "Прва лекција";
    
    public HashMap<String, Lession> lessions;
    public String defaultLessionName;
    
    public static Bukvar getBukvar(Image table) {
        ObjectInputStream ois = null;
        Bukvar bukvar;
        try {
            FileInputStream fis = new FileInputStream(dataFileName);
            ois = new ObjectInputStream(fis);
            bukvar = (Bukvar)ois.readObject();
        } catch (Exception e) {
            System.out.println("Data file not found.");
            bukvar = new Bukvar(table);
        } finally {
            try { ois.close(); } catch (Exception e) {} 
        }
        return bukvar;
    }

    private Bukvar(Image table) {
        this.lessions = new HashMap<>();
        this.defaultLessionName = firstLessionName;
        this.lessions.put(firstLessionName, new Lession(firstLessionName, table));
    }
    
    public void save() {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(dataFileName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
        } catch (Exception e) {
            System.out.println("Error dumping data file.");
            System.out.println(e);
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
