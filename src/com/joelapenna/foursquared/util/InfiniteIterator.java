/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared.util;

import java.util.Iterator;

public class InfiniteIterator implements Iterator {

    private int[] mArray;
    private int mLength;
    private int mCount;

    public InfiniteIterator(int[] array) {
        mArray = array;
        mLength = array.length;
        mCount = 0;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Object next() {
        int toReturn = mArray[mCount];
        mCount++;
        if (mCount == mLength) {
            mCount = 0;
        }
        return toReturn;
    }

    @Override
    public void remove() {
    }
}