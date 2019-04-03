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

    public Lession(String name, int timeToPickImgsSecs, int imgsToPresent, int minMatchingImgs) {
        this.name = name;
        this.timeToPickImgsIndef = timeToPickImgsSecs == 0;
        this.timeToPickImgsSecs = timeToPickImgsSecs;
        this.imgsToPresent = imgsToPresent;
        this.minMatchingImgs = minMatchingImgs;
        this.images = new ArrayList<>();
    }
    
}
