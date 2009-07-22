#!/usr/bin/python

import logging

from xml.dom import pulldom

BOOLEAN = "boolean"
STRING = "String"
GROUP = "Group"


DEFAULT_INTERFACES = ['Parcelable', 'FoursquareType']

INTERFACES = {
    'Checkin': DEFAULT_INTERFACES + ['VenueFilterable'],
    'Tip': DEFAULT_INTERFACES + ['VenueFilterable'],
    'Venue': DEFAULT_INTERFACES + ['VenueFilterable'],
}

DEFAULT_CLASS_IMPORTS = [
    'import android.os.Parcel',
    'import android.os.Parcelable',
]

CLASS_IMPORTS = {
    'Checkin': DEFAULT_CLASS_IMPORTS + [
        'import com.joelapenna.foursquare.filters.VenueFilterable'
    ],
    'User': DEFAULT_CLASS_IMPORTS + [
        'import com.joelapenna.foursquare.types.Group'
    ],
    'Tip': DEFAULT_CLASS_IMPORTS + [
        'import com.joelapenna.foursquare.types.Group'
    ],
    'Venue': DEFAULT_CLASS_IMPORTS + [
        'import com.joelapenna.foursquare.filters.VenueFilterable'
    ],
    'Tip': DEFAULT_CLASS_IMPORTS + [
        'import com.joelapenna.foursquare.filters.VenueFilterable'
    ],
}


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

  for event, node in doc:
    if event == pulldom.START_ELEMENT:
      # Get the type name to use.
      if type_name is None:
        type_name = ''.join([word.capitalize()
                             for word in node.tagName.split('_')])
        top_node_name = node.tagName
        continue

      doc.expandNode(node)
      has_text_child = False
      has_complex_child = False
      child_node_tagnames = set()
      for child in node.childNodes:
        if child.nodeType == node.ELEMENT_NODE:
          child_node_tagnames.add(child.tagName)
          has_complex_child = True
        if child.nodeType == node.TEXT_NODE and not child.data.isspace():
          has_text_child = True
      assert len(child_node_tagnames) in [0, 1]

      if has_text_child:
        logging.warn(node.tagName + ' has a text child')
        value = node.firstChild.data
        if (node.tagName == 'badges'):
            import pdb; pdb.set_trace()
      else:
        value = None

      if node.tagName in TYPES:
        typ = TYPES[node.tagName]
      if value in ["0", "1", "true", "false"]:
        typ = BOOLEAN
      elif node.hasChildNodes() and has_complex_child:
        typ = GROUP
      elif node.hasChildNodes() and not has_text_child:
        typ = ''.join([word.capitalize() for word in node.tagName.split('_')])
      else:
        typ = STRING
      attributes.setdefault(node.tagName, typ)
  return type_name, top_node_name, attributes
