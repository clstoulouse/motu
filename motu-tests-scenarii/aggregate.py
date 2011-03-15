# expected directory tree
# results  / scenario / test name_date / result.csv

import os
import glob
import string
import re
from xml.dom.minidom import Document


testNamePattern = r"(.+)_(\d\d\d\d.\d\d\.\d\d_\d\d\.\d\d\.\d\d)"
outputXmlFile = "results.xml"

# creating main result document
result = Document()

root = result.createElement('results')
result.appendChild(root)

basePath = 'results'

try:
    scenarios = os.listdir( basePath )    
    for scenario in scenarios:      
      if not os.path.isdir(os.path.join(basePath,scenario)):
         continue
      tests = os.listdir(os.path.join(basePath,scenario))
      for test in tests:
         testPath = os.path.join(basePath,scenario,test)
         resultFile = os.path.join(testPath,'results.csv')
         if not os.path.isfile( resultFile ):
           print "A result file must be present in the test directory ", testPath     
         else:       
           m = re.match( testNamePattern, test)
           if m:         
            testName = m.group(1)
            testDate = m.group(2)
            print "Processing test ", testName
            resultFileHandler = open(resultFile,"r")
            for line in resultFileHandler:     
               line = line.strip()
               # create transaction with context informations
               transaction = result.createElement('transaction')
               transaction.setAttribute('scenario', scenario)
               transaction.setAttribute('test', testName)
               transaction.setAttribute('date', testDate)
               # put result information as read in the file
               
               data = line.split(';')
               if len(data) != 8:
                    print "Invalid transaction result (this line will be skiped) :\n", line
                    continue
               transaction.setAttribute('count', data[0] )
               transaction.setAttribute('elapsed-time', data[1])
               transaction.setAttribute('epoch-time', data[2])
               transaction.setAttribute('user-group-name', data[3])
               transaction.setAttribute('script-run-time', data[4])
               transaction.setAttribute('error', data[5])
               
               # dealing with custom values               
               custom_counters = data[6].lstrip('{').rstrip('}').strip()               
               custom_metrics  = data[7].lstrip('{').rstrip('}').strip()               
               custom_values = custom_counters
               if len(custom_metrics)>0:
                  if len(custom_counters)>0:
                     custom_values = custom_values + ','
                  custom_value = custom_values +custom_metrics               
                  
               for couple in custom_values.split(','):
                  if ':' in couple:
                      key, sep, value = couple.partition(':')
                      
                      transaction.setAttribute(key.strip().strip("'"), value.strip().strip("'"))
               
               root.appendChild(transaction)
           else:
            print "Test name '"+test+"' doesn't follow the pattern "+testNamePattern           
finally:    
    #writing a well formed document even if an exception has occured
    outHandler = open(os.path.join(basePath,outputXmlFile),'w')
    result.writexml(outHandler,"\n","   ")
    