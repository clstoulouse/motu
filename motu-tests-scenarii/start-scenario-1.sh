PYTHONPATH=.:../motu-client-python/lib:projects/common
echo $PYTHONPATH
# Scenario 1 accessing data through TDS with scan mode, uncompressed data
# ================================================================
#../multi-mechanize/multi-mechanize.py scenario-1 TDS.LR_OBS.UNZ.001D.01V.FULL
#../multi-mechanize/multi-mechanize.py scenario-1 TDS.HR_OBS.UNZ.001D.01V.FULL

# Scenario 1 accessing data through TDS without scan mode, uncompressed data
# ===================================================================
../multi-mechanize/multi-mechanize.py scenario-1 THR.LR_OBS.UNZ.001D.01V.FULL
../multi-mechanize/multi-mechanize.py scenario-1 THR.HR_OBS.UNZ.001D.01V.FULL

# Scenario 1 accessing data through file system (FTP), uncompressed data
# =============================================================
../multi-mechanize/multi-mechanize.py scenario-1 FILE.LR_OBS.UNZ.001D.01V.FULL
../multi-mechanize/multi-mechanize.py scenario-1 FILE.HR_OBS.UNZ.001D.01V.FULL

# Scenario 1 accessing data through TDS without scan mode, compressed data
# =================================================================
../multi-mechanize/multi-mechanize.py scenario-1 THR.LR_OBS.ZIP.001D.01V.FULL
../multi-mechanize/multi-mechanize.py scenario-1 THR.HR_OBS.ZIP.001D.01V.FULL

# Scenario 1 accessing data through file system (FTP), compressed data
# ===========================================================
../multi-mechanize/multi-mechanize.py scenario-1 FILE.LR_OBS.ZIP.001D.01V.FULL
../multi-mechanize/multi-mechanize.py scenario-1 FILE.HR_OBS.ZIP.001D.01V.FULL
