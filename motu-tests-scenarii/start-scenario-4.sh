PYTHONPATH=.:../motu-client-python/lib:projects/common
echo $PYTHONPATH
# Scenario 4 accessing data through TDS with scan mode, uncompressed data
# ================================================================

# Scenario 4 accessing data through TDS without scan mode, uncompressed data
# ===================================================================
../multi-mechanize/multi-mechanize.py scenario-4 THR.LR_OBS.UNZ.365D.01V.40SQR
../multi-mechanize/multi-mechanize.py scenario-4 THR.HR_OBS.UNZ.365D.01V.40SQR
../multi-mechanize/multi-mechanize.py scenario-4 THR.LR_MOD.UNZ.365D.22V.40SQR

# Scenario 4 accessing data through file system (FTP), uncompressed data
# ==============================================================
#../multi-mechanize/multi-mechanize.py scenario-4 FILE.LR_OBS.UNZ.365D.01V.40SQR
#../multi-mechanize/multi-mechanize.py scenario-4 FILE.HR_OBS.UNZ.365D.01V.40SQR
#../multi-mechanize/multi-mechanize.py scenario-4 FILE.LR_MOD.UNZ.365D.22V.40SQR

# Scenario 4 accessing data through TDS without scan mode, compressed data
# =================================================================
../multi-mechanize/multi-mechanize.py scenario-4 THR.LR_OBS.ZIP.365D.01V.40SQR
../multi-mechanize/multi-mechanize.py scenario-4 THR.HR_OBS.ZIP.365D.01V.40SQR
../multi-mechanize/multi-mechanize.py scenario-4 THR.LR_MOD.ZIP.365D.22V.40SQR

# Scenario 4 accessing data through file system (FTP), compressed data
# ===========================================================
../multi-mechanize/multi-mechanize.py scenario-4 FILE.LR_OBS.ZIP.365D.01V.40SQR
../multi-mechanize/multi-mechanize.py scenario-4 FILE.HR_OBS.ZIP.365D.01V.40SQR
../multi-mechanize/multi-mechanize.py scenario-4 FILE.LR_MOD.ZIP.365D.22V.40SQR
