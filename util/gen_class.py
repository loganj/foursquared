#!/usr/bin/python

import datetime
import sys
import textwrap

import common

from xml.dom import pulldom

HEADER = """\
/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;
%(imports)s
/**
 * Auto-generated: %(timestamp)s
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class %(type_name)s implements %(interfaces)s {
"""


GETTER = """\
public %(attribute_type)s get%(camel_name)s() {
    return %(field_name)s;
}
"""

SETTER = """\
public void set%(camel_name)s(%(attribute_type)s %(attribute_name)s) {
    %(field_name)s = %(attribute_name)s;
}
"""

BOOLEAN_GETTER = """\
public %(attribute_type)s %(attribute_name)s() {
    return %(field_name)s;
}
"""


def main():
  type_name, top_node_name, attributes = common.WalkNodesForAttributes(
      sys.argv[1])
  GenerateClass(type_name, attributes)


def GenerateClass(type_name, attributes):
  lines = []
  for attribute_name in sorted(attributes):
    typ, children = attributes[attribute_name]
    lines.extend(Field(attribute_name, typ).split('\n'))

  lines.append('')
  lines.extend(Constructor(type_name).split('\n'))
  lines.append('')

  # getters and setters
  for attribute_name in sorted(attributes):
    attribute_type, children = attributes[attribute_name]
    lines.extend(Accessors(attribute_name, attribute_type).split('\n'))

  print Header(type_name)
  #print '    ' + '\n    '.join(lines)
  for line in lines:
    if not line:
      print line
    else:
      print '    ' + line
  print Footer()


def AccessorReplacements(attribute_name, attribute_type):
  # CamelCaseClassName
  camel_name = ''.join([word.capitalize()
                        for word in attribute_name.split('_')])
  # camelCaseLocalName
  attribute_name = (camel_name[0].lower() + camel_name[1:])
  # mFieldName
  field_attribute_name = 'm' + camel_name

  return {
      'attribute_name': attribute_name,
      'camel_name': camel_name,
      'field_name': field_attribute_name,
      'attribute_type': attribute_type
  }


def Header(type_name):
  interfaces = common.INTERFACES.get(type_name, common.DEFAULT_INTERFACES)
  import_names = common.CLASS_IMPORTS.get(type_name,
      common.DEFAULT_CLASS_IMPORTS)
  if import_names:
    imports = ';\n'.join(imports) + ';'
  else:
    imports = ''
  return HEADER % {'type_name': type_name,
                   'interfaces': ', '.join(interfaces),
                   'imports': imports,
                   'timestamp': datetime.datetime.now()}


def Field(attribute_name, attribute_type):
  """Print the field declarations."""
  replacements = AccessorReplacements(attribute_name, attribute_type)
  return 'private %(attribute_type)s %(field_name)s;' % replacements


def Constructor(type_name):
  return 'public %s() {\n}' % type_name


def Accessors(name, attribute_type):
  """Print the getter and setter definitions."""
  replacements = AccessorReplacements(name, attribute_type)
  if attribute_type == common.BOOLEAN:
    return '%s\n%s' % (BOOLEAN_GETTER % replacements, SETTER % replacements)
  else:
    return '%s\n%s' % (GETTER % replacements, SETTER % replacements)


def Footer():
  return '}'


if __name__ == '__main__':
  main()
