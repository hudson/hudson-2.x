#!/bin/sh

for image in `ls *.png`; do
    method=`echo "$image" | sed 's/.png//'`
    echo "    ImageResource ${method}();"
done