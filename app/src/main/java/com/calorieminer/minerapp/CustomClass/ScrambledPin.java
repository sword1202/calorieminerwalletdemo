package com.calorieminer.minerapp.CustomClass;

import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class ScrambledPin {
    private List<AbstractMap.SimpleImmutableEntry<Integer,Integer>> matrix = null;
    private List<Integer> positions = null;

    public ScrambledPin() {

        positions = new ArrayList<Integer>();
        positions.add(0);
        positions.add(1);
        positions.add(2);
        positions.add(3);
        positions.add(4);
        positions.add(5);
        positions.add(6);
        positions.add(7);
        positions.add(8);
        positions.add(9);

        matrix = new ArrayList<AbstractMap.SimpleImmutableEntry<Integer,Integer>>();

        init();

    }

    private void init()  {

        SecureRandom random = new SecureRandom();

        for(int i = 0; i < 10; i++)  {

            int ran = random.nextInt(positions.size());
            AbstractMap.SimpleImmutableEntry<Integer,Integer> pair = new AbstractMap.SimpleImmutableEntry<Integer,Integer>(i, positions.get(ran));
            positions.remove(ran);
            matrix.add(pair);

        }

    }

    public List<AbstractMap.SimpleImmutableEntry<Integer,Integer>> getMatrix()  {
        return matrix;
    }
}
