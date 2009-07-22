#!/usr/bin/python

import sys
import textwrap

from xml.dom import pulldom

BOOLEAN = "boolean"
STRING = "String"

PARSER = """\
/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.parsers;

import com.joelapenna.foursquared.foursquare.Foursquare;
import com.joelapenna.foursquared.foursquare.error.FoursquareError;
import com.joelapenna.foursquared.foursquare.types.%(class_name)s;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class %(class_name)sParser extends AbstractParser<%(class_name)s> {
    private static final String TAG = "%(class_name)sParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public %(class_name)s parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError {
        %(class_name)s %(top_node_name)s = new %(class_name)s();
        int eventType = parser.nextToken();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));
                    String name = parser.getName();

                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("%(top_node_name)s".equals(name)) {
                        parse%(class_name)sTag(parser, %(top_node_name)s);
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return %(top_node_name)s;
    }

    public void parse%(class_name)sTag(XmlPullParser parser, %(class_name)s %(top_node_name)s) throws XmlPullParserException,
            IOException {
        assert parser.getName() == "%(top_node_name)s";
        if (DEBUG) Log.d(TAG, "parsing %(top_node_name)s stanza");
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            String name = parser.getName();
%(stanzas)s            }
        }
    }
}
"""
STANZA = """\
            } else if ("%(name)s".equals(name)) {
                %(top_node_name)s.set%(camel_name)s(parser.nextText());
"""


def main():
  doc = pulldom.parse(sys.argv[1])

  class_name = None
  attributes = {}

  for event, node in doc:
    if event == pulldom.START_ELEMENT:
      if class_name is None:
        top_node_name = node.localName
        class_name = node.localName.capitalize()
        continue
      elif node.nodeValue is not None and node.nodeValue in ["0", "1"]:
        typ = BOOLEAN
      elif node.hasChildNodes():
        typ = node.nodeValue.capitalize()
      else:
        typ = STRING
      attributes.setdefault(node.localName, typ)

  GenerateClass(top_node_name, class_name, attributes)


def GenerateClass(top_node_name, class_name, attributes):
  stanzas = []
  for name in sorted(attributes):
    typ = attributes[name]
    replacements = Replacements(top_node_name, name, typ)
    stanzas.append(STANZA % replacements)
  if stanzas:
    stanzas[0] = stanzas[0].replace('} else ', '', 1)

  replacements = Replacements(top_node_name, name, typ)
  replacements['stanzas'] = '\n'.join(stanzas)
  print PARSER % replacements


def Replacements(top_node_name, name, typ):
  # CameCaseClassName
  class_name = ''.join([word.capitalize() for word in top_node_name.split('_')])
  # CamelCaseClassName
  camel_name = ''.join([word.capitalize() for word in name.split('_')])
  # camelCaseLocalName
  attribute_name = camel_name[0].lower() + camel_name[1:]
  # mFieldName
  field_name = 'm' + camel_name

  return {
      'class_name': class_name,
      'name': name,
      'top_node_name': top_node_name,
      'camel_name': camel_name,
      'attribute_name': attribute_name,
      'field_name': field_name,
      'typ': typ
  }


if __name__ == '__main__':
  main()
