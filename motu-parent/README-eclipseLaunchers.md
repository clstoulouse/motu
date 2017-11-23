# Motu localhost Tomcat v7.0 Server - No CAS SSO.launch

Starts the Motu server on tomcat 7.0 without using any CAS server to authenticate users




# Motu localhost Tomcat v7.0 Server - CAS SSO QT.launch

Starts the Motu server on tomcat 7.0 and uses the CAS server installed on the QT machine. 
The CAS server has an auto-signed certificate.   
So on the Motu server machine, you have to set the CA certificate in order to Java be able to validate the CAS server certificate.  

First let's have a look at the CAS SERVER and how to set up the SSL certificates of the CAS server and of the CA authority.
Then in order to runs well, your Motu server needs to add the CAS server CA certificate into its cacerts.  

### [CAS SERVER MACHINE]    
- CAS QT server: atoll@atoll-qt2.vlandata.cls.fr  
```
                 cd /opt/atoll/tomcat-cas-server/  
                 # LDAP configuration file  
                 vi /opt/atoll/tomcat-cas-server/webapps/cas/WEB-INF/deployerConfigContext.xml  
                 # QT CAS server uses its own SSL certificates defined in:  
                 vi /opt/atoll/tomcat-cas-server/conf/server.xml  
	 			 @see <Connector port="8443"   
				                keystoreFile="/opt/atoll/ssl/motuTrustStoreFromP12.keystore"  
				                truststoreFile="/opt/atoll/ssl/caTrustStore.jks"  
```

How to generate CA and CAS server certificates?  
cd /opt/atoll/ssl  
- First generate the certificate for the CA  
openssl genrsa 2048 > ca.key  
* Country Name=FR ; State or Province=Unknown, Locality Name=Toulouse, Organization Name=CLS,   
* Organizational Unit Name=DOS, Common Name=ca_cert_qt, Email Address=Unknown  
openssl req -new -x509 -days 9999 -key ca.key > ca.crt  

* Create CA trust store, -ext SAN=ip is the IP of the Motu server, Need to use Java 1.7  
* password=changeit, Trust this certificate=yes  
~/jdk1.7.0_79/bin/keytool -import -file ca.crt -alias root -keystore caTrustStore.jks  -ext SAN=ip:192.168.8.217  

ls  
-rw-rw-r-- 1 atoll atoll 1570 Jun 21 12:24 ca.crt  
-rw-rw-r-- 1 atoll atoll 1675 Jun 21 12:23 ca.key  
-rw-rw-r-- 1 atoll atoll 1179 Jun 21 12:25 caTrustStore.jks  

- Then generate the certificate for the CAS server and make it signed by the CA  
* Generate CAS server certificate and make it signed by CA  
openssl genrsa 2048 > motu.key  
  
* Generate the certificate request  
* Country Name=FR ; State or Province=Unknown, Locality Name=Toulouse, Organization Name=CLS,   
* Organizational Unit Name=DOS, Common Name=atoll-qt2.vlandata.cls.fr, Email Address=Unknown  
* A challenge password=changeit, An optional company name=CLS  
openssl req -new -key motu.key -out motu.csr  

* Generate auto signed certificate  
openssl x509 -req -days 9999 -in motu.csr -signkey motu.key -out motu.crt  

* Generate auto signed certificate in p12 format, Enter password=changeit  
openssl pkcs12 -export -name tomcat -in motu.crt -inkey motu.key -out motu-key.p12  

* Make the CAS server certificate signed by the CA   
openssl x509 -req -in motu.csr -out motuSignedByCA.crt -CA ca.crt -CAkey ca.key -CAcreateserial -CAserial ca.srl  


* Export the signed CAS certificate into a p12 format, password=changeit  
openssl pkcs12 -export -in motuSignedByCA.crt -inkey motu.key -out motuSignedByCA.p12 -name tomcat -CAfile ca.crt -caname root -chain  
* Import the CAS certificate into a Java keystore, password=changeit, trust tis certificate=yes  
~/jdk1.7.0_79/bin/keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore   motuTrustStoreFromP12.keystore -srckeystore motuSignedByCA.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias tomcat -ext SAN=ip:192.168.8.217  
  
  
Now restart CAS tomcat.  
cd /opt/atoll  
./stop-cas  
./start-cas  
tail -f tomcat-cas-server/logs/catalina.out  







### [MOTU SERVER MACHINE (your host)]
- Download the file  ca.crt from the CAS server machine (/opt/atoll/ssl/ca.crt) into ${MOTU_HOME}/config/security/ and rename it "cas-qt-ca.crt"
- Copy the default Java cacerts "C:\dvlt\java\jdk\jdk1.7.0_80\jre\lib\security\cacerts" file into ${MOTU_HOME}/config/security/
  and rename this file to "cacerts-with-cas-qt-ca.jks"
- Then import "cas-qt-ca.crt" inside "cacerts-with-cas-qt-ca.jks", Password=changeit, Trust the certificate=yes (oui)
  C:\dvlt\java\jdk\jdk1.7.0_80\bin\keytool -import -v -trustcacerts -alias atoll-qt2.vlandata.cls.fr -file cas-qt-ca.crt -keystore cacerts-with-cas-qt-ca.jks -keypass changeit
- In the launcher, this file is referenced -Djavax.net.ssl.trustStore=${MOTU_HOME}/config/security/cacerts-with-cas-qt-ca.jks


### [WEB BROWSER]  
* First logout from CAS server  
https://atoll-qt2.vlandata.cls.fr:8443/cas/logout  

*Try to access to   
http://%COMPUTERNAME%.pc.cls.fr:8080/motu-web/Motu  
=> As the certificate is autosigned, and the browser does not trust the CA certificate, it warns: "Your connection is not   private", click "advanced", then "Proceed to atoll-qt2.vlandata.cls.fr (unsafe)"  
Your are redirected to the login page of the CAS server: https://atoll-qt2.vlandata.cls.fr:8443/cas/login  
Enter login and password  
=> If your are authenticated successfully, you have now access to the Motu web page  









# Motu localhost Tomcat v7.0 Server - CAS SSO QO.launch

Same as the QT but here the CAS SSO server points to https://corecas.cls.fr/cas  
On the QO machine the SSL certificates are signed by a CA known by Java, so there is nothing to set on that side.  
