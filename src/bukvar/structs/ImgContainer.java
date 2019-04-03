/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bukvar.structs;

import java.io.Serializable;

/**
 *
 * @author jovan
 */
public class ImgContainer implements Serializable {
    
    public String letter;

    public ImgContainer(String letter) {
        this.letter = letter;
    }
    
}
