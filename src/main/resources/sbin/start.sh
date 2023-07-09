#!/bin/bash

# Copyright 2023 com.ovo.consuming_resources
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#source /etc/profile
#设置变量
JAVA_HOME=/*/*/openjdk11linux64
SERVICE_HOME=/root/ovo/consuming_resources
SERVICE_NAME=consuming_resources-1.0-SNAPSHOT.jar

# 执行命令并获取结果
pid=$($JAVA_HOME/bin/jps -ml | grep $SERVICE_NAME | awk '{print $1}')

# 判断结果是否为空
if [ -z "$pid" ]; then
  echo "进程不存在"
else
  echo ">>> kill -9 $pid"
  kill -9 $pid
fi

nohup $JAVA_HOME/bin/java -jar $SERVICE_HOME/$SERVICE_NAME -c:35 -m:50 > $SERVICE_HOME/ovo_consuming_resources.log 2>&1 &

#30分钟一次执行脚本
#root权限修改 vi /etc/crontab 配置文件并加入任务计划
#*/30 * * * * root sh /root/ovo/consuming_resources/start.sh &>/dev/null 2>&1