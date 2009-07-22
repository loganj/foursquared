#!/usr/bin/python

import sys
import textwrap

import common

from xml.dom import pulldom

HEADER = """\
/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class %s implements FoursquareType {
"""


GETTER = """\
public %(typ)s get%(camel_name)s() {
    return %(field_name)s;
}
"""

SETTER = """\
public void set%(camel_name)s(%(typ)s %(attribute_name)s) {
    %(field_name)s = %(attribute_name)s;
}
"""

BOOLEAN_GETTER = """\
public %(typ)s %(attribute_name)s() {
    return %(field_name)s;
}
"""

def main():
  type_name, top_node_name, attributes = common.WalkNodesForAttributes(
      sys.argv[1])
  GenerateClass(type_name, attributes)


def GenerateClass(type_name, attributes):
  lines = []
  for name in sorted(attributes):
    typ = attributes[name]
    lines.extend(Fields(name, typ).split('\n'))

  lines.append('')
  lines.extend(Constructor(type_name).split('\n'))
  lines.append('')

  for name in sorted(attributes):
    typ = attributes[name]
    lines.extend(Accessors(name, typ).split('\n'))

  print Header(type_name)
  print '    ' + '\n    '.join(lines)
  print Footer()


def AccessorReplacements(name, typ):
  # CamelCaseClassName
  camel_name = ''.join([word.capitalize() for word in name.split('_')])
  # camelCaseLocalName
  attribute_name = camel_name[0].lower() + camel_name[1:]
  # mFieldName
  field_name = 'm' + camel_name

  return {
      'camel_name': camel_name,
      'attribute_name': attribute_name,
      'field_name': field_name,
      'typ': typ
  }


def Header(type_name):
  return HEADER % type_name


def Fields(name, typ):
  """Print the field declarations."""
  replacements = AccessorReplacements(name, typ)
  return 'private %(typ)s %(field_name)s;' % replacements


def Constructor(type_name):
  return 'public %s() {\n}' % type_name


def Accessors(name, typ):
  """Print the getter and setter definitions."""
  replacements = AccessorReplacements(name, typ)
  if typ == common.BOOLEAN:
    return '%s\n%s' % (BOOLEAN_GETTER % replacements, SETTER % replacements)
  else:
    return '%s\n%s' % (GETTER % replacements, SETTER % replacements)


def Footer():
  return '}'


if __name__ == '__main__':
  main()
