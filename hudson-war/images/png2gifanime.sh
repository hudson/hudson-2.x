#!/bin/bash -e
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


# take multiple PNG files in the command line, and convert them to animation gif in the white background
# then send it to stdout
i=0
tmpbase=/tmp/png2gifanime$$

for f in "$@"
do
  convert $f \( +clone -fill white -draw 'color 0,0 reset' \) \
         -compose Dst_Over $tmpbase$i.gif
  fileList[$i]=$tmpbase$i.gif
  i=$((i+1))
done

convert -delay 10 ${fileList[@]} -loop 0 "${tmpbase}final.gif"
cat ${tmpbase}final.gif
rm ${fileList[@]} ${tmpbase}final.gif