# motu-docker

## Description
This project is the NGinx  server that will be the entry point for queries to MOTU services, it is made to redirect download queries to prepared files,
and the functionnal queries are propagated to the configured MOTU.  
  
Note that MOTU server and the NGinx server can work on 2 different ports, so that functional queries could be directly sent to the MOTU server (or loadbalancer) without going through this NGinx.  
In that case, the download URL property (downloadHttpUrl) of the motuConfiguration.xml file shall link to the URL (host, port and path) configured for this NGinx server. 
 

## Build Component and Docker image

These commands build a Jar containing a Manifest with the main Java class, then build the Docker image and push it.  
```  
export MAVEN_CLI_OPTS=-Dci.docker.registry=registry-ext.cls.fr:443 -Dci.docker.prefix=registry-ext.cls.fr:443/motu/motu/motu-nginx -Dci.docker.username=gitlab-ci-token -Dci.docker.password=xxxxxxxxxxxxxxxxxxxx -Dci.docker.auth.string= -Ddocker.http.proxy= -Ddocker.https.proxy= -Dcls.docker.registry=registry-ext.cls.fr:443 -Ddocker.verbose --batch-mode --errors --fail-at-end --show-version --settings /motu/motu/ci/settings.xml
mvn $MAVEN_CLI_OPTS clean package -DskipTests docker:build  
mvn $MAVEN_CLI_OPTS docker:push
``` 

## Configuration

Var env name | Default Value| Type | Description 
---|:---:|---|---
**NGINX_PORT**|8070| int | The public port to access the NGINX Server
**MOTU_URL**| http://\<current hostname\>:8080| String | The public URL to access the MOTU Server
**MOTU_PATH**|/motu-web| String | The public path to access the MOTU Server services. 
**MOTU_DOWNLOAD_PATH**|/mis-gateway/deliveries| String | The path of the URL for downloading result files.

Note that if **MOTU_PATH** is set to "/motu-web/Motu", all static data won't be loaded on the displayed page (icon and javascript). That is why the default value is set at /motu-web.

## Volumes

The NGinx server need to access the volume were the results file are stored. This volume shall be mounted under **/var/www**.

## Run NGinx Docker
### Get the image
```console  
motu@motu:~$ docker login registry-ext.cls.fr:443  
motu@motu:~$ docker pull registry-ext.cls.fr:443/motu/motu/motu-nginx
 [...]
 Status: Downloaded newer image for registry-ext.cls.fr:443/motu/motu/motu-nginx:latest
 
motu@motu:~$ docker images
REPOSITORY                                     TAG      IMAGE ID       CREATED          SIZE
registry-ext.cls.fr:443/motu/motu/motu-nginx   latest   1ec24e087d96   15 minutes ago   127MB
```
### Launch the image
```console
docker run --name motu-nginx -v /opt/motu/data/public/download:/var/www -p 18070:8070 -e NGINX_PORT=18070 -e MOTU_URL=http://$(hostname):18080 -d 1ec24e087d96  
 [...]
motu@motu:~$ docker ps
CONTAINER ID   IMAGE         COMMAND                 CREATED        STATUS        PORTS                            NAMES  
e0b1ddf1ccfb   1ec24e087d96  "/opt/motu-nginx/b..."  9 seconds ago  Up 8 seconds  80/tcp, 0.0.0.0:18070->8070/tcp  motu-nginx  
```

## Log of the NGinx Docker

From the host were the Docker image was launched:
```
tail -f /var/log/syslog
 or
docker logs e0b1ddf1ccfb  
```

## Stop the NGinx Docker

```
docker stop e0b1ddf1ccfb
docker rm motu-nginx
```  