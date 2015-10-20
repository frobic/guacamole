#!/bin/bash

adb backup -noapk $1
dd if=backup.ab bs=24 skip=1|openssl zlib -d > backup.tar
tar -xopf backup.tar