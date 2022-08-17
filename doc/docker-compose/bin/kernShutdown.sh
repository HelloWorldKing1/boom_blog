#!/usr/bin/env bash

echo '=====开始结束运行蘑菇博客服务====='

echo '=====结束运行portainer可视化工具====='
docker-compose -f ../yaml/portainer.yml down

echo '=====结束运行mysql====='
docker-compose -f ../yaml/mysql.yml down

echo '=====结束运行nacos====='
docker-compose -f ../yaml/nacos.yml down

echo '=====结束运行rabbitmq====='
docker-compose -f ../yaml/rabbitmq.yml down

echo '=====结束运行redis====='
docker-compose -f ../yaml/redis.yml down

echo '=====结束运行boom_data====='
docker-compose -f ../yaml/boom_data.yml down

echo '=====结束运行minio====='
docker-compose -f ../yaml/minio.yml down


echo '=========================='
echo '=====结束后台服务运行====='
echo '=========================='

echo '=====结束运行boom_gateway====='
docker-compose -f ../yaml/boom_gateway.yml down

echo '=====结束运行boom_admin====='
docker-compose -f ../yaml/boom_admin.yml down

echo '=====结束运行boom_picture====='
docker-compose -f ../yaml/boom_picture.yml down

echo '=====结束运行boom_sms====='
docker-compose -f ../yaml/boom_sms.yml down

echo '=====结束运行boom_web====='
docker-compose -f ../yaml/boom_web.yml down


echo '=========================='
echo '=====结束前台服务运行====='
echo '=========================='

echo '=====结束运行vue_boom_admin====='
docker-compose -f ../yaml/vue_boom_admin.yml down


echo '=====结束运行vue_boom_web====='
docker-compose -f ../yaml/vue_boom_web.yml down

echo '=============================='
echo '=====所有服务已经结束运行====='
echo '=============================='