/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Response;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * @date April 28, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class ResponseParser extends AbstractParser<Response> {

    @Override
    public Response parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareParseException, FoursquareError {

        // A response has no children.
        Response response = new Response();
        response.setValue(parser.nextText());
        return response;
    }
}
