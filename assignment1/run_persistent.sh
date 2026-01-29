#!/bin/sh
java -cp classes -Xmx1g ir.Engine -d ../davisWiki -l dd2477.png -p patterns.txt  -links ../rank-disk/linksDavis.txt -titles ../rank-disk/davisTitles.txt -ni
