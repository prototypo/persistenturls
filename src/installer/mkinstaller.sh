#!/bin/sh
# Create an installer JAR for a PURL server.

# Where IzPack is installed.
IZPACK=/Applications/IzPack

# PURL server version number.
VERSION=1.3

$IZPACK/bin/compile IzPackInstall.xml -b . -o PURLZ-Server-$VERSION.jar -k standard

echo 'REMINDER: Did you run "ant all" and "ant deploy" in the ../src directory before this script?'

