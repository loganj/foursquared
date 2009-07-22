#!/usr/bin/python

from xml.dom import pulldom

BOOLEAN = "boolean"
STRING = "String"


def WalkNodesForAttributes(path):
  doc = pulldom.parse(path)

  type_name = None
  top_node_name = None
  attributes = {}

  for event, node in doc:
    if event == pulldom.START_ELEMENT:
      # Get the type name to use.
      if type_name is None:
        type_name = node.tagName.capitalize()
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
