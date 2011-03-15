#!/usr/bin/env python
#
#  Copyright (c) 2010 Corey Goldberg (corey@goldb.org)
#  License: GNU LGPLv3
#  
#  This file is part of Multi-Mechanize


import urllib2
import time
import datetime
import httplib
import os
import sys
import calendar

import motu_api
import stop_watch
import tests_common

query_options = {            
                  # Proxy
                  #'proxy_server': 'http://proxy-bureautique.cls.fr:8080',
                  # Authentication
                  'auth_mode': 'cas',
                  'user': 'opemis',
                  'pwd' :  'GUfoxipi',
                  # Motu server
                  'motu': 'http://misgw-qo-externe.cls.fr/misgw-qo-servlet/Motu',
                  # Product & service
                  'product_id': 'http://purl.org/myocean/ontology/product/database#HR_OBS_ZIP',
                  'service_id': 'http://purl.org/myocean/ontology/service/database#misgw-qo-file',                  
                  # Geographic extraction
                  'latitude_max':  20.0,
                  'latitude_min': -20.0,
                  'longitude_max': -170,
                  'longitude_min': 170,
                  # Temporal extraction
                  #'date_max': '2011-03-01',
                  #'date_min': '2011-03-01',
                  #  Vertical extraction
                  # 'depth_min'; 0,
                  # 'depth_max': 0,
                  #'variable' : ['sea_surface_temperature'],
                  # output file
                  'out_prefix_name': 'test_HR_OBS_ZIP'
                }

class Transaction(object):
    def __init__(self):
        self.custom_timers = {}
        self.custom_metrics = {}
    
    def run(self):        
        stopWatch = stop_watch.localThreadStopWatch()
        stopWatch.clear()

        dateList = ['2010-03-03','2010-03-05','2010-03-07','2010-03-10','2010-03-12','2010-03-14','2010-03-17','2010-03-19'
                   ,'2010-03-21','2010-03-24','2010-03-26','2010-03-28','2010-03-31','2010-04-02','2010-04-04','2010-04-07'
                   ,'2010-04-09','2010-04-11','2010-04-14','2010-04-16','2010-04-18','2010-04-21','2010-04-23','2010-04-25'
                   ,'2010-04-28','2010-04-30','2010-05-02','2010-05-05','2010-05-07','2010-05-09','2010-05-12','2010-05-14'
                   ,'2010-05-16','2010-05-19','2010-05-21','2010-05-23','2010-05-26','2010-05-28','2010-05-30','2010-06-02'
                   ,'2010-06-04','2010-06-06','2010-06-09','2010-06-11','2010-06-13','2010-06-16','2010-06-18','2010-06-20'
                   ,'2010-06-23','2010-06-25','2010-06-27','2010-06-30','2010-07-02','2010-07-04','2010-07-07','2010-07-09'
                   ,'2010-07-11','2010-07-14','2010-07-16','2010-07-18','2010-07-21','2010-07-23','2010-07-25','2010-07-28'
                   ,'2010-07-30','2010-08-01','2010-08-04','2010-08-06','2010-08-08','2010-08-11','2010-08-13','2010-08-15'
                   ,'2010-08-18','2010-08-20','2010-08-22','2010-08-25','2010-08-27','2010-08-29','2010-09-01','2010-09-03'
                   ,'2010-09-05','2010-09-08','2010-09-10','2010-09-12','2010-09-15','2010-09-17','2010-09-19','2010-09-22'
                   ,'2010-09-24','2010-09-26','2010-09-29','2010-10-01','2010-10-03','2010-10-06','2010-10-08','2010-10-10'
                   ,'2010-10-13','2010-10-15','2010-10-17','2010-10-20','2010-10-22','2010-10-24','2010-10-27','2010-10-29'
                   ,'2010-10-31','2010-11-03','2010-11-05','2010-11-07','2010-11-10','2010-11-12','2010-11-14','2010-11-17'
                   ,'2010-11-19','2010-11-21','2010-11-24','2010-11-26','2010-11-28','2010-12-01','2010-12-03','2010-12-05'
                   ,'2010-12-08','2010-12-10','2010-12-12','2010-12-15','2010-12-17','2010-12-19','2010-12-22','2010-12-24'
                   ,'2010-12-26','2010-12-29','2010-12-31','2011-01-02','2011-01-05','2011-01-07','2011-01-09','2011-01-12'
                   ,'2011-01-14','2011-01-16','2011-01-19','2011-01-21','2011-01-23','2011-01-26','2011-01-28','2011-01-30'
                   ,'2011-02-02','2011-02-04','2011-02-06','2011-02-09','2011-02-11','2011-02-13','2011-02-16','2011-02-18'
                   ,'2011-02-20','2011-02-23','2011-02-25','2011-02-27']
        for i in range (0, len (dateList)):
          query_options['date_min'] = dateList[i]
          query_options['date_max'] = dateList[i]
          
          tests_common.set_output_directory(query_options, self.output_dir, self.thread_num, self.process_num, self.transaction)
       
          motu_api.execute_request(tests_common.Struct(**query_options))
        
        self.custom_timers =  tests_common.compute_custom_timers(stopWatch)
        self.custom_metrics = tests_common.compute_custom_metrics(query_options)   

if __name__ == '__main__':
    trans = Transaction()
    trans.thread_num = 0
    trans.process_num = 0 
    trans.transaction = 0
    trans.run()
   