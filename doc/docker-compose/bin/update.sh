#!/usr/bin/env bash

echo '=====开始更新蘑菇博客镜像====='

echo '=====开始关闭运行的容器====='
sh kernShutdown.sh

echo '=====开始更新boom-gateway====='
docker pull registry.cn-shenzhen.aliyuncs.com/boomblog/boom_gateway

echo '=====开始更新boom-admin====='
docker pull registry.cn-shenzhen.aliyuncs.com/boomblog/boom_admin

echo '=====开始更新boom-web====='
docker pull registry.cn-shenzhen.aliyuncs.com/boomblog/boom_web

echo '=====开始更新boom-sms====='
docker pull registry.cn-shenzhen.aliyuncs.com/boomblog/boom_sms

echo '=====开始更新boom-picture====='
docker pull registry.cn-shenzhen.aliyuncs.com/boomblog/boom_picture

echo '=====开始更新vue_boom_admin====='
docker pull registry.cn-shenzhen.aliyuncs.com/boomblog/vue_boom_admin

echo '=====开始更新vue_boom_web====='
docker pull registry.cn-shenzhen.aliyuncs.com/boomblog/vue_boom_web

echo '=====删除docker标签为none的镜像====='
docker images | grep none | awk '{print $3}' | xargs docker rmi

echo '=====开始运行的一键部署脚本====='
sh kernStartup.sh
