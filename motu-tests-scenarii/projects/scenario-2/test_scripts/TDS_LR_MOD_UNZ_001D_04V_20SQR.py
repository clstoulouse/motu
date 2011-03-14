#!/usr/bin/env python
#
#  Copyright (c) 2010 Corey Goldberg (corey@goldb.org)
#  License: GNU LGPLv3
#  
#  This file is part of Multi-Mechanize


import urllib2
import time
import httplib
import os
import sys

import motu_api
import stop_watch
import tests_common

query_options = {            
                  # Proxy
                  'proxy_server': 'http://proxy-bureautique.cls.fr:8080',
                  # Authentication
                  'auth_mode': 'cas',
                  'user': 'opemis',
                  'pwd' :  'GUfoxipi',
                  # Motu server
                  'motu': 'http://misgw-qo-externe.cls.fr/misgw-qo-servlet/Motu',
                  # Product & service
                  'product_id': 'LR_MOD',
                  'service_id': 'http://purl.org/myocean/ontology/service/database#misgw-qo-tds',
                  # Geographic extraction
                  'latitude_max':  10.0,
                  'latitude_min': -10.0,
                  'longitude_max': -10,
                  'longitude_min': 170,
                  # Temporal extraction
                  'date_max': '2011-03-01',
                  'date_min': '2011-03-01',
                  #  Vertical extraction
                  # 'depth_min': 0,
                  # 'depth_max': 0,
                  # Variable extraction
                    'variable': ['sea_ice_thickness', 'surface_downward_heat_flux_in_sea_water', 'sea_water_salinity'],
                  # output file
                  'out_prefix_name': 'test_LR_MOD'
                }

class Transaction(object):
    def __init__(self):
        self.custom_timers = {}
        self.custom_metrics = {}
    
    def run(self):        
        stopWatch = stop_watch.localThreadStopWatch()
        stopWatch.clear()
        
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
   
