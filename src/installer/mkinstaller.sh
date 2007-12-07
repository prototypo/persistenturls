#!/bin/sh
# Create an installer JAR for a PURL server.

# Where IzPack is installed.
IZPACK=/Applications/IzPack

# PURL server version number.
VERSION=1.0b2

echo 'REMINDER: Run ant all and ant deploy before this script!'

$IZPACK/bin/compile IzPackInstall.xml -b . -o PURLZ-Server-$VERSION.jar -k standard
