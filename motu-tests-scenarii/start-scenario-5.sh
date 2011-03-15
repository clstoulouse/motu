PYTHONPATH=.:../motu-client-python/lib:projects/common
echo $PYTHONPATH
# Scenario 5 accessing data through TDS with scan mode, uncompressed data
# ================================================================

# Scenario 5 accessing data through TDS without scan mode, uncompressed data
# ===================================================================
../multi-mechanize/multi-mechanize.py scenario-5 THR.HR_MOD.UNZ.001D.04V.40SQR

# Scenario 5 accessing data through file system (FTP), uncompressed data
# ==============================================================
../multi-mechanize/multi-mechanize.py scenario-5 FILE.HR_MOD.UNZ.001D.04V.40SQR

# Scenario 5 accessing data through TDS without scan mode, compressed data
# =================================================================
../multi-mechanize/multi-mechanize.py scenario-5 THR.HR_MOD.ZIP.001D.04V.40SQR

# Scenario 5 accessing data through file system (FTP), compressed data
# ===========================================================
../multi-mechanize/multi-mechanize.py scenario-5 FILE.HR_MOD.ZIP.001D.04V.40SQR
