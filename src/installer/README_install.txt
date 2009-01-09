The version number needs to be updated in the following files:

mkinstaller.sh
IzPackInstall.xml
README.html

If any of the files under the options/ directory are changed in the main distribution directories, they must be updated under options manually.

Both of these problems are hackish and should be automated.


To make an installer, you will need to have Ant and IzPack installed.  Next run these commands (in order):

cd <checkout_dir>/src
ant clean
ant all
ant deploy
cd <checkout_dir>/installer
./mkinstaller.sh
