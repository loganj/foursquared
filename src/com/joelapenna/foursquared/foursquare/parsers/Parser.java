/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.parsers;

import com.joelapenna.foursquared.foursquare.Foursquare;
import com.joelapenna.foursquared.foursquare.error.FoursquareException;
import com.joelapenna.foursquared.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquared.foursquare.types.FoursquareType;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public abstract class Parser<T extends FoursquareType> {
    private static final String TAG = "Parser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private static XmlPullParserFactory mFactory;

    abstract protected T parseInner(final XmlPullParser parser) throws FoursquareException,
            FoursquareParseException, XmlPullParserException, IOException;

    final public T parse(InputStream is) throws FoursquareException, FoursquareParseException {
        if (DEBUG) Log.d(TAG, "parse()ing");
        setFactory();
        XmlPullParser parser = createParser(is);
        try {
            return parseInner(parser);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "IOException", e);
            throw new FoursquareParseException("Recieved IOException while processing");
        } catch (XmlPullParserException e) {
            throw new FoursquareParseException("Recieved XmlPullParserException while processing");
        }
    }

    protected static XmlPullParser createParser(InputStream is) {
        XmlPullParser parser;
        try {
            parser = mFactory.newPullParser();
            parser.setInput(is, null);
        } catch (XmlPullParserException e) {
            throw new IllegalArgumentException();
        }
        return parser;
    }

    protected static void setFactory() {
        if (mFactory == null) {
            try {
                mFactory = XmlPullParserFactory.newInstance();
            } catch (XmlPullParserException e) {
                throw new IllegalStateException("Could not create a factory");
            }
        }
    }
}
