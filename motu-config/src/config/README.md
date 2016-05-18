sudo apt-get install nfs-common

sudo mkdir -p /data/atoll
sudo mkdir -p /data/atoll02

#Manual installation
sudo mount netapp4-nfs.cls.fr:/vol/vol_ATOLL01/qtree_ATOLL01/DATA /data/atoll
sudo mount netapp3-nfs.cls.fr:/vol/vol_ATOLL02/qtree_ATOLL02/DATA /data/atoll02

#vi /etc/fstab
netapp4-nfs.cls.fr:/vol/vol_ATOLL01/qtree_ATOLL01/DATA          /data/atoll     nfs     defaults        0 0
netapp3-nfs.cls.fr:/vol/vol_ATOLL02/qtree_ATOLL02/DATA          /data/atoll02   nfs     defaults        0 0
