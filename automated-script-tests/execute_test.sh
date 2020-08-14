#!/bin/bash

device=$1
pattern=${2:-*}

outputs=outputs.txt

function runTest() {
  path=$1
  echo "running" $path

  curl -X POST \
    https://shzkh-api.esper.cloud/api/v0/enterprise/c1a3ad31-187c-43cc-83be-0fff034248ad/command/ \
    -H 'Authorization: Bearer wvwAS6UlvVvFmxHdTSFKJJgtAAmSZx' \
    -H 'Content-Type: application/json' \
    -d @$path -o /dev/null >/dev/null
}

adb -s $device logcat -c
adb -s $device logcat -s "ScriptProcessor" "ImplicitActivity" "ExplicitActivity" "MainActivity" "TestBackgroundService" "TestBroadcastReceiver" "TestForegroundService" -v tag >$outputs &
pid1=$!

for entry in json/$pattern; do
  runTest $entry
  sleep 3
done
sleep 5

kill $pid1
