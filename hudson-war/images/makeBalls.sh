#!/bin/sh -ex
#******************************************************************************
#
# Copyright (c) 2004-2010 Oracle Corporation.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors: 
#
#    Kohsuke Kawaguchi
#
#*******************************************************************************


# build flashing balls

for sz in 16x16 24x24 32x32 48x48
do
  for color in grey blue yellow red green
  do
    cp $sz/$color.gif ../resources/images/$sz/$color.gif
    ./makeFlash.sh $sz/$color.gif ../resources/images/$sz/${color}_anime.gif
  done
done