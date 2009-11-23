#!/usr/bin/python

import os
import subprocess
import sys

BASEDIR = '../main/src/com/joelapenna/foursquare'
TYPESDIR = '../captures/types/v1'

captures = sys.argv[1:]
if not captures:
  captures = os.listdir(TYPESDIR)

for f in captures:
  basename = f.split('.')[0]
  javaname = ''.join([c.capitalize() for c in basename.split('_')])
  fullpath = os.path.join(TYPESDIR, f)
  typepath = os.path.join(BASEDIR, 'types', javaname + '.java')
  parserpath = os.path.join(BASEDIR, 'parsers', javaname + 'Parser.java')

  cmd = 'python gen_class.py %s > %s' % (fullpath, typepath)
  print cmd
  subprocess.call(cmd, stdout=sys.stdout, shell=True)

  cmd = 'python gen_parser.py %s > %s' % (fullpath, parserpath)
  print cmd
  subprocess.call(cmd, stdout=sys.stdout, shell=True)
