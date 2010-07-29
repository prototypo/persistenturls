#!/bin/sh
# Create an installer JAR for a PURL server.

# Where IzPack is installed.
IZPACK=/Applications/IzPack

# PURL server version number.
VERSION=1.6.3

# Location of UNZIP utility
UNZIP=/usr/bin/unzip

# Unzip the NetKernel directory, if needed.
if [ -d 'NetKernel-Base' ]; then
  # do nothing
  echo "No need to unzip NetKernel-Base... skipping.\n";
else
  # Unzip the directory
  $UNZIP NetKernel-Base.zip
fi

$IZPACK/bin/compile IzPackInstall.xml -b . -o PURLZ-Server-$VERSION.jar -k standard

echo 'REMINDER: Did you run "ant all" and "ant deploy" in the ../src directory before this script?'

