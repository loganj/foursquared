#!/usr/bin/python

from xml.dom import pulldom

BOOLEAN = "boolean"
STRING = "String"


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
      has_text_child = (node.hasChildNodes()
                        and node.firstChild.nodeType == node.TEXT_NODE)
      if has_text_child:
        value = node.firstChild.data
      else:
        value = None
      if value in ["0", "1"]:
        typ = BOOLEAN
      elif node.hasChildNodes() and not has_text_child:
        typ = ''.join([word.capitalize() for word in node.tagName.split('_')])
      else:
        typ = STRING
      attributes.setdefault(node.tagName, typ)
  return type_name, top_node_name, attributes
