#!/bin/bash

# Make HTML from XML and place into $OUTDIR.

OUTDIR=../site/project/purl/documentation/requirements

for i in *.xml; do
  STEM=`echo $i | sed -e 's/.xml//g'`;
  xsltproc -o $OUTDIR/$STEM.html Reqs2HTML.xsl $i
done

mkdir -p $OUTDIR/images
cp images/*.jpg images/*.png $OUTDIR/images

cp index.html URLs.html $OUTDIR

