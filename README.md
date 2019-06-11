# parallel-lab5-docker
Paralell course work containing docker image with lab work 5. Docker image has hadoop, yarn and preinstalled spark. Also contains java .jar with NASA logs.

# Instructions
2 variants to get images:
 - 1 variant:
    1. download https://drive.google.com/open?id=1Cdj0WNS1jR2YHnH3TPO36E_quFMPTceb
    2. unzip .7z file to get .tar file with images
    3. `docker load -i <path to image tar file>`
 - 2 variant:
    1. download to-build-images.7z
    2. unzip .7z
    3. put logs to `img_files\data\input\<here>`
    4. run script `build.sh`

after you get images run them with:
 1. run ```docker-compose up```
 2. connect to master with  ```docker exec -it lab5_master_1 /bin/bash```
 3. when connected run script ```parallel-init.sh``` to run hdfs
 4. run ```./bin/spark-submit --master $MASTER /tmp/lab5.jar``` if you want to run java program in spark standalone mode
 5. run ```./bin/spark-submit --master yarn --deploy-mode cluster /tmp/lab5.jar``` to run java program in over yarn mode

results are presented here in result directory, however you can find results in ```hdfs dfs -ls / ```