package com.wolf.jumpnoroot.util;

import java.util.Random;

/**
 * Created by Roye on 2018/1/9.
 */

public class Utils {


    public static int[] getRandomCoordinate(int[] from, int[] to){
        return new int[]{from[0]+new Random().nextInt(to[0]-from[0]),from[1]+new Random().nextInt(to[1]-from[1])};
    }


}
