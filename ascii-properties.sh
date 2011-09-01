#!/bin/sh
# 
#
# Copyright (c) 2010-2011, Sonatype, Inc.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors: 
#
#    Sonatype, Inc.
#     
 

for f in `find . -name "*.properties" -type f`
do
  enc=`file -b --mime-encoding $f`
  if [ "$enc" != "us-ascii" ]
  then
    iconv -s -f $enc -t us-ascii --unicode-subst="\u%04X" $f > $f.tmp; mv $f.tmp $f
  fi
done
