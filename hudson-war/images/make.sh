#!/bin/sh -e
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


for src in *.svg
do
  echo processing $src
  e=$(echo $src | sed -e s/.svg/.png/ )
  for sz in 16 24 32 48
  do
    dst=${sz}x${sz}/$e
    if [ ! -e $dst -o $src -nt $dst ];
    then
      mkdir ${sz}x${sz} > /dev/null 2>&1 || true
      svg2png -w $sz -h $sz < $src > $dst
      #convert t.png \( +clone -fill white -draw 'color 0,0 reset' \) \
      #   -compose Dst_Over $dst
      # composite -compose Dst_Over -tile xc:white t.png $dst
      # rm t.png
    fi
  done
done
