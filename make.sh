read -p "Enter GPG keyname: " gpgkeynm
read -p "Enter key alias to sign JAR/APK: " keyalias
read -p "Enter encrypted pass-phrase to sign JAR: " passw
read -s -p "Enter pass-phrase to sign APK: " passwapk
mvn clean install -Prelease -Dandroid.release=true -Dsignpassapk=$passwapk -Dsignpass=$passw -Dsignalias=$keyalias -Dgpgkeyname=$gpgkeynm

