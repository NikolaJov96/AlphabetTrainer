/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bukvar.structs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

/**
 *
 * @author jovan
 */
public class Lession implements Serializable {
    
    public String name;
    public boolean timeToPickImgsIndef;
    public int timeToPickImgsSecs;
    public int imgsToPresent;
    public int minMatchingImgs;
    public ArrayList<ImgContainer> images;
    public transient Image table;

    public Lession(String name, Image table) {
        this.name = name;
        this.timeToPickImgsIndef = true;
        this.timeToPickImgsSecs = 0;
        this.imgsToPresent = 4;
        this.minMatchingImgs = 1;
        this.images = new ArrayList<>();
        this.table = table;
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        table = SwingFXUtils.toFXImage(ImageIO.read(s), null);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        if (table != null) {
            s.defaultWriteObject();
            ImageIO.write(SwingFXUtils.fromFXImage(table, null), "png", s);
        }
    }
    
}
