#!/usr/bin/python

import logging

from xml.dom import minidom
from xml.dom import pulldom

BOOLEAN = "boolean"
STRING = "String"
GROUP = "Group"


# Interfaces that all FoursquareTypes implement.
DEFAULT_INTERFACES = ['FoursquareType']

# Interfaces that specific FoursqureTypes implement.
INTERFACES = {
}

DEFAULT_CLASS_IMPORTS = [
]

CLASS_IMPORTS = {
#    'Checkin': DEFAULT_CLASS_IMPORTS + [
#        'import com.joelapenna.foursquare.filters.VenueFilterable'
#    ],
#    'Venue': DEFAULT_CLASS_IMPORTS + [
#        'import com.joelapenna.foursquare.filters.VenueFilterable'
#    ],
#    'Tip': DEFAULT_CLASS_IMPORTS + [
#        'import com.joelapenna.foursquare.filters.VenueFilterable'
#    ],
}


COMPLEX = [
    'Group',
    'Badge',
    'Beenhere',
    'Checkin',
    'CheckinResponse',
    'City',
    'Credentials',
    'Data',
    'Mayor',
    'Rank',
    'Score',
    'Scoring',
    'Settings',
    'Stats',
    'Tags',
    'Tip',
    'User',
    'Venue',
]
TYPES = COMPLEX + ['boolean']


def WalkNodesForAttributes(path):
  """Parse the xml file getting all attributes.
  <venue>
    <attribute>value</attribute>
  </venue>

  Returns:
    type_name - The java-style name the top node will have. "Venue"
    top_node_name - unadultured name of the xml stanza, probably the type of
    java class we're creating. "venue"
    attributes - {'attribute': 'value'}
  """
  doc = pulldom.parse(path)

  type_name = None
  top_node_name = None
  attributes = {}

  level = 0
  for event, node in doc:
    # For skipping parts of a tree.
    if level > 0:
      if event == pulldom.END_ELEMENT:
        level-=1
        logging.warn('(%s) Skip end: %s' % (str(level), node))
        continue
      elif event == pulldom.START_ELEMENT:
        logging.warn('(%s) Skipping: %s' % (str(level), node))
        level+=1
        continue

    if event == pulldom.START_ELEMENT:
      logging.warn('Parsing: ' + node.tagName)
      # Get the type name to use.
      if type_name is None:
        type_name = ''.join([word.capitalize()
                             for word in node.tagName.split('_')])
        top_node_name = node.tagName
        logging.warn('Found Top Node Name: ' + top_node_name)
        continue

      typ = node.getAttribute('type')
      child = node.getAttribute('child')
      # We don't want to walk complex types.
      if typ in COMPLEX:
        logging.warn('Found Complex: ' + node.tagName)
        level = 1
      elif typ not in TYPES:
        logging.warn('Found String: ' + typ)
        typ = STRING
      else:
        logging.warn('Found Type: ' + typ)
      logging.warn('Adding: ' + str((node, typ)))
      attributes.setdefault(node.tagName, (typ, [child]))
  logging.warn('Attr: ' + str((type_name, top_node_name, attributes)))
  return type_name, top_node_name, attributes
