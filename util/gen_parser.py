#!/usr/bin/python

import datetime
import sys
import textwrap

import common

from xml.dom import pulldom

PARSER = """\
/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.%(type_name)s;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: %(timestamp)s
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class %(type_name)sParser extends AbstractParser<%(type_name)s> {
    private static final String TAG = "%(type_name)sParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public %(type_name)s parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        // Disabled, because some parsers need to parse elements with different
        // names.
        // parser.require(XmlPullParser.START_TAG, null, "%(top_node_name)s");
        parser.require(XmlPullParser.START_TAG, null, null);

        %(type_name)s %(top_node_name)s = new %(type_name)s();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            %(stanzas)s

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return %(top_node_name)s;
    }
}"""

BOOLEAN_STANZA = """\
            } else if ("%(name)s".equals(name)) {
                %(top_node_name)s.set%(camel_name)s(Boolean.valueOf(parser.nextText()));
"""

GROUP_STANZA = """\
            } else if ("%(name)s".equals(name)) {
                %(top_node_name)s.set%(camel_name)s(new GroupParser(new %(camel_name_singular)sParser()).parse(parser));
"""

COMPLEX_STANZA = """\
            } else if ("%(name)s".equals(name)) {
                %(top_node_name)s.set%(camel_name)s(new %(camel_name)sParser().parse(parser));
"""

STANZA = """\
            } else if ("%(name)s".equals(name)) {
                %(top_node_name)s.set%(camel_name)s(parser.nextText());
"""


def main():
  type_name, top_node_name, attributes = common.WalkNodesForAttributes(
      sys.argv[1])
  GenerateClass(type_name, top_node_name, attributes)


def GenerateClass(type_name, top_node_name, attributes):
  stanzas = []
  for name in sorted(attributes):
    typ = attributes[name]
    replacements = Replacements(top_node_name, name, typ)
    if typ == common.BOOLEAN:
      stanzas.append(BOOLEAN_STANZA % replacements)

    elif typ == common.GROUP:
      replacements['camel_name_singular'] = replacements['camel_name'][:-1]
      stanzas.append(GROUP_STANZA % replacements)

    elif typ in common.COMPLEX:
      replacements['camel_name_singular'] = replacements['camel_name'][:-1]
      stanzas.append(COMPLEX_STANZA % replacements)

    else:
      stanzas.append(STANZA % replacements)
  if stanzas:
    # pop off the extranious } else for the first conditional stanza.
    stanzas[0] = stanzas[0].replace('} else ', '', 1)

  replacements = Replacements(top_node_name, name, typ)
  replacements['stanzas'] = '\n'.join(stanzas).strip()
  print PARSER % replacements


def Replacements(top_node_name, name, typ):
  # CameCaseClassName
  type_name = ''.join([word.capitalize() for word in top_node_name.split('_')])
  # CamelCaseClassName
  camel_name = ''.join([word.capitalize() for word in name.split('_')])
  # camelCaseLocalName
  attribute_name = camel_name[0].lower() + camel_name[1:]
  # mFieldName
  field_name = 'm' + camel_name

  return {
      'type_name': type_name,
      'name': name,
      'top_node_name': top_node_name,
      'camel_name': camel_name,
      'attribute_name': attribute_name,
      'field_name': field_name,
      'typ': typ,
      'timestamp': datetime.datetime.now()
  }


if __name__ == '__main__':
  main()
