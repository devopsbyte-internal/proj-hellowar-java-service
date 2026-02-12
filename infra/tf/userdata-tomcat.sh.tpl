#!/usr/bin/env bash
# Find first bash in path and run it (all linux distros, unless PATH badly manipulated)


set -euo pipefail

echo "[user_data] Starting backend bootstrap..." 

# Adjust this URL to your real GitHub raw URL
BOOTSTRAP_URL="${bootstrap_url}"

curl -fSL "$BOOTSTRAP_URL" -o /root/bootstrap-tomcat.sh
chmod +x /root/bootstrap-tomcat.sh

echo "[user_data] Running bootstrap script..."
/root/bootstrap-tomcat.sh

sleep 1
echo "[user_data] Backend bootstrap completed."
