This project is only used to publish motu-products-distrib.tar.gz on Nexus.
mvn deploy:deploy-file -Dfile="V:/cmems-cis/06-livraison/61-livraison-de-CLS/611-FilesUsedToBuild/motu-products-3.8.0.tar.gz" 
                       -DpomFile="pom.xml" 
                       -DrepositoryId="releases-repo" 
                       -Durl="http://mvnrepo.cls.fr:8081/nexus/content/repositories/releases" 
                       -Dpackaging=tar.gz