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
public class Lession implements Serializable {
    
    public String name;
    public boolean timeToPickImgsIndef;
    public int timeToPickImgsSecs;
    public int imgsToPresent;
    public int minMatchingImgs;
    public ArrayList<ImgContainer> images;

    public Lession(String name) {
        this.name = name;
        this.timeToPickImgsIndef = true;
        this.timeToPickImgsSecs = 0;
        this.imgsToPresent = 4;
        this.minMatchingImgs = 1;
        this.images = new ArrayList<>();
    }
    
}
