/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared.util;

import java.util.Iterator;

@SuppressWarnings("unchecked")
public class InfiniteIterator<T> implements Iterator {

    private T[] mArray;
    private int mLength;
    private int mCount;

    public InfiniteIterator(T[] array) {
        mArray = array;
        mLength = array.length;
        mCount = 0;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public T next() {
        T toReturn = mArray[mCount];
        mCount++;
        if (mCount == mLength) {
            mCount = 0;
        }
        return toReturn;
    }

    @Override
    public void remove() {
    }

    public void reset() {
        mCount = 0;
    }
}