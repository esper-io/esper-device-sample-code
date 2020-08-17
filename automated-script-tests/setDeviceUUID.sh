#!/bin/bash

tmp=$(mktemp)

for entry in json/*; do
  jq --arg uuid "$1" '.devices = [$uuid]' $entry >"$tmp" && mv "$tmp" $entry
done
