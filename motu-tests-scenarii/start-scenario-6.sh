PYTHONPATH=.:../motu-client-python/lib:projects/common
echo $PYTHONPATH
# Scenario 6 accessing data through TDS with scan mode, uncompressed data
# ================================================================

# Scenario 6 accessing data through TDS without scan mode, uncompressed data
# ===================================================================
../multi-mechanize/multi-mechanize.py scenario-6 THR.HR_MOD.UNZ.365D.22V.FULL

# Scenario 6 accessing data through file system (FTP), uncompressed data
# ==============================================================
../multi-mechanize/multi-mechanize.py scenario-6 FILE.HR_MOD.UNZ.365D.22V.FULL

# Scenario 6 accessing data through TDS without scan mode, compressed data
# =================================================================
../multi-mechanize/multi-mechanize.py scenario-6 THR.HR_MOD.ZIP.365D.22V.FULL

# Scenario 6 accessing data through file system (FTP), compressed data
# ===========================================================
../multi-mechanize/multi-mechanize.py scenario-6 FILE.HR_MOD.ZIP.365D.22V.FULL
