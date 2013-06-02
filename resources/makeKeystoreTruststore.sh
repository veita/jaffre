#!/bin/sh

keytool -genkeypair -alias example01 -keystore keystore -storetype JCEKS -dname "CN=host.example.org, OU=dev, O=Example Org., L=Somewhere, ST=Some State, C=Some Country" -keyalg RSA -keysize 1024 -validity 1825 -storepass secret -keypass secret

keytool -list -keystore keystore -storetype JCEKS -rfc -storepass secret

keytool -export -alias example01 -keystore keystore -storetype JCEKS -rfc -file example01.cer -storepass secret

keytool -import -alias example01 -file example01.cer -keystore truststore -storetype JCEKS -storepass secret

