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

# use motu libraries
sys.path.append(os.path.join(os.path.dirname(__file__), '../../motu-client-python/lib'))

import motu_api
import stop_watch

class Struct:
    def __init__(self, **entries): 
        self.__dict__.update(entries)                
    
    def __getattr__(self, key):
        if key in self.__dict__:
            return self.__dict__[key]        
        return None
                

def set_output_directory(query_options, output_dir, thread_num, process_num, transaction):
    # execute the request
    query_options['out_name'] = query_options['out_prefix_name'] + '-' + str(thread_num) + '-' + str(process_num) + '-' + str(transaction) + '.nc'
    query_options['out_dir'] = os.sep.join([output_dir,'data'])
    
    
def compute_custom_timers(stopWatch):
    # 0 is start 
    # 1 is  end_authentication
    # 2 is end_processing
    # 3 is end_downloading
    # 4 is end
    times = stopWatch.getTimes()
    custom_timers = {}
    for key, value in times.iteritems():
       custom_timers[key] = value
    
    return custom_timers
    
def compute_custom_metrics(query_options):
    # compute downloaded size
    custom_metrics = {}
    file = os.sep.join([query_options['out_dir'], query_options['out_name']])
    custom_metrics['ofs'] = os.stat(file).st_size
    try:
       os.path(file)
    except (RuntimeError, TypeError, NameError):
       pass
    return custom_metrics
   
