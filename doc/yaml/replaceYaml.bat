echo #######################
echo ##### begin copy ######
echo #######################

echo ###### copy boom_admin ########
copy .\conf\prod\boom_admin\application.yml ..\..\boom_admin\src\main\resources\
copy .\conf\prod\boom_admin\bootstrap.yml ..\..\boom_admin\src\main\resources\


echo ###### copy boom_gateway ########
copy .\conf\prod\boom_gateway\application.yml ..\..\boom_gateway\src\main\resources\
copy .\conf\prod\boom_gateway\bootstrap.yml ..\..\boom_gateway\src\main\resources\


echo ###### copy boom_monitor ########
copy .\conf\prod\boom_monitor\application.yml ..\..\boom_monitor\src\main\resources\
copy .\conf\prod\boom_monitor\bootstrap.yml ..\..\boom_monitor\src\main\resources\


echo ###### copy boom_picture ########
copy .\conf\prod\boom_picture\application.yml ..\..\boom_picture\src\main\resources\
copy .\conf\prod\boom_picture\bootstrap.yml ..\..\boom_picture\src\main\resources\


echo ###### copy boom_search ########
copy .\conf\prod\boom_search\application.yml ..\..\boom_search\src\main\resources\
copy .\conf\prod\boom_search\bootstrap.yml ..\..\boom_search\src\main\resources\


echo ###### copy boom_sms ########
copy .\conf\prod\boom_sms\application.yml ..\..\boom_sms\src\main\resources\
copy .\conf\prod\boom_sms\bootstrap.yml ..\..\boom_sms\src\main\resources\


echo ###### copy boom_spider ########
copy .\conf\prod\boom_spider\application.yml ..\..\boom_spider\src\main\resources\
copy .\conf\prod\boom_spider\bootstrap.yml ..\..\boom_spider\src\main\resources\


echo ###### copy boom_web ########
copy .\conf\prod\boom_web\application.yml ..\..\boom_web\src\main\resources\
copy .\conf\prod\boom_web\bootstrap.yml ..\..\boom_web\src\main\resources\
