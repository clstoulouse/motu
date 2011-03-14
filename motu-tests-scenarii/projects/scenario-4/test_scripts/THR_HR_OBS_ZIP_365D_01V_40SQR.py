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
                  'product_id': 'HR_OBS',
                  'service_id': 'http://purl.org/myocean/ontology/service/database#misgw-qo-zip',                  
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
                  'variable' : ['sea_surface_temperature'],
                  # output file
                  'out_prefix_name': 'test_HR_OBS'
                }

class Transaction(object):
    def __init__(self):
        self.custom_timers = {}
        self.custom_metrics = {}
    
    def run(self):        
        stopWatch = stop_watch.localThreadStopWatch()
        stopWatch.clear()

        initDate = datetime.date(2010, 2, 28)
        dayStep = 2
        for i in range (0, 365, dayStep):
          startDate = initDate + datetime.timedelta(days=i+1)
          if ((i + dayStep) < 365):
            endDate = startDate + datetime.timedelta(days=dayStep - 1)
          else:
            endDate = initDate + datetime.timedelta(days=365)
          query_options['date_min'] = startDate.strftime("%Y-%m-%d")
          query_options['date_max'] = endDate.strftime("%Y-%m-%d")          

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
   