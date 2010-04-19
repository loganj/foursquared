/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.widget;


/**
 * Interface that our adapters can implement to release any observers they
 * may have registered with remote resources manager. Most of the adapters
 * register an observer in their constructor, but there is was no appropriate
 * place to release them. Parent activities can call this method in their
 * onPause(isFinishing()) block to properly release the observers.
 * 
 * If the observers are not released, it will cause a memory leak.
 * 
 * @date March 8, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public interface ObservableAdapter {
    public void removeObserver();
}
