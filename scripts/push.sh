#!/bin/sh
while true
do
  gpio wfi 4 falling
  echo skip
  sleep 1
done
