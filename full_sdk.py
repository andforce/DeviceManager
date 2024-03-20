#!/usr/bin/python3
# -*- coding: utf-8 -*-

import os
import time


# 打印日志
def log(msg):
    print(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time())), msg)

log('start')

# /home/dy/data/aosp/out/target/common/obj/JAVA_LIBRARIES

AOSP_PATH = '/home/dy/data/aosp'
PRODUCT_NAME = 'generic_arm64'


# 拷贝文件到指定目录
def copy_file(src, dst):
    if os.path.exists(src):
        os.system('cp -f ' + src + ' ' + dst)
        log('copy ' + src + ' to ' + dst)
    else:
        log(src + ' not found')

# 拷贝framework-res.apk到指定目录
copy_file(AOSP_PATH + '/out/target/product/' + PRODUCT_NAME + '/system/framework/framework-res.apk', AOSP_PATH + '/sdk/full_sdk/framework-res.apk')
# unzip framework-res.apk
os.system('cd ' + AOSP_PATH + '/sdk/full_sdk/ && unzip -o framework-res.apk')
# 删除framework-res.apk
os.system('rm -f ' + AOSP_PATH + '/sdk/full_sdk/framework-res.apk')


# 拷贝framework.jar到指定目录
copy_file(AOSP_PATH + '/out/target/product/' + PRODUCT_NAME + '/system/framework/framework.jar', AOSP_PATH + '/sdk/full_sdk/framework.jar')
# jadx --no-res framework.jar
os.system('cd ' + AOSP_PATH + '/sdk/full_sdk/ && ' + 'jadx --no-res ' + AOSP_PATH + '/sdk/full_sdk/framework.jar')
# 拷贝反编译后的产物
os.system('cp -rf ' + AOSP_PATH + '/sdk/full_sdk/framework/sources/* ' + AOSP_PATH + '/sdk/full_sdk/')
# 删除中间目录
os.system('rm -rf ' + AOSP_PATH + '/sdk/full_sdk/framework/')
# 删除framework.jar
os.system('rm -f ' + AOSP_PATH + '/sdk/full_sdk/framework.jar')

# 拷贝services.jar到指定目录
copy_file(AOSP_PATH + '/out/target/product/' + PRODUCT_NAME + '/system/framework/services.jar', AOSP_PATH + '/sdk/full_sdk/services.jar')
# jadx --no-res services.jar
os.system('cd ' + AOSP_PATH + '/sdk/full_sdk/ && ' + 'jadx --no-res ' + AOSP_PATH + '/sdk/full_sdk/services.jar')
# 拷贝反编译后的产物
os.system('cp -rf ' + AOSP_PATH + '/sdk/full_sdk/services/sources/* ' + AOSP_PATH + '/sdk/full_sdk/')
# 删除中间目录
os.system('rm -rf ' + AOSP_PATH + '/sdk/full_sdk/services/')
# 删除services.jar
os.system('rm -f ' + AOSP_PATH + '/sdk/full_sdk/services.jar')



# 拷贝classes.jar到指定目录
copy_file(AOSP_PATH + '/out/target/common/obj/JAVA_LIBRARIES/ext_intermediates/classes.jar', AOSP_PATH + '/sdk/full_sdk/ext_intermediates_classes.jar')
copy_file(AOSP_PATH + '/out/target/common/obj/JAVA_LIBRARIES/framework-graphics_intermediates/classes.jar', AOSP_PATH + '/sdk/full_sdk/framework-graphics_intermediates_classes.jar')
copy_file(AOSP_PATH + '/out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/classes.jar', AOSP_PATH + '/sdk/full_sdk/framework_intermediates_classes.jar')
copy_file(AOSP_PATH + '/out/target/common/obj/JAVA_LIBRARIES/framework-minus-apex_intermediates/classes.jar', AOSP_PATH + '/sdk/full_sdk/framework-minus-apex_intermediates_classes.jar')
copy_file(AOSP_PATH + '/out/target/common/obj/JAVA_LIBRARIES/ims-common_intermediates/classes.jar', AOSP_PATH + '/sdk/full_sdk/ims-common_intermediates_classes.jar')
copy_file(AOSP_PATH + '/out/target/common/obj/JAVA_LIBRARIES/telephony-common_intermediates/classes.jar', AOSP_PATH + '/sdk/full_sdk/telephony-common_intermediates_classes.jar')
copy_file(AOSP_PATH + '/out/target/common/obj/JAVA_LIBRARIES/voip-common_intermediates/classes.jar', AOSP_PATH + '/sdk/full_sdk/voip-common_intermediates_classes.jar')

# 使用jar命令解压classes.jar
def unzip_jar(dir, jarfile):
    os.system('cd ' + dir + ' && jar xvf ' + jarfile)
    log('unzip ' + jarfile)

unzip_jar(AOSP_PATH + '/sdk/full_sdk/', 'ext_intermediates_classes.jar')
unzip_jar(AOSP_PATH + '/sdk/full_sdk/', 'framework-graphics_intermediates_classes.jar')
unzip_jar(AOSP_PATH + '/sdk/full_sdk/', 'framework_intermediates_classes.jar')
unzip_jar(AOSP_PATH + '/sdk/full_sdk/', 'framework-minus-apex_intermediates_classes.jar')
unzip_jar(AOSP_PATH + '/sdk/full_sdk/', 'ims-common_intermediates_classes.jar')
unzip_jar(AOSP_PATH + '/sdk/full_sdk/', 'telephony-common_intermediates_classes.jar')
unzip_jar(AOSP_PATH + '/sdk/full_sdk/', 'voip-common_intermediates_classes.jar')

# 删除jar文件
os.system('rm -f ' + AOSP_PATH + '/sdk/full_sdk/*_classes.jar')
log('rm jar file')

# jar 打包成classes.jar
os.system('cd ' + AOSP_PATH + '/sdk/full_sdk/ && jar cvf full_sdk.jar .')


# 列出 AOSP_PATH + '/sdk/full_sdk/'一级目录下的文件和文件夹,如果不是full_sdk.jar,则删除
def remove_dir(dir):
    for f in os.listdir(dir):
        if f != 'full_sdk.jar':
            os.system('rm -rf ' + dir + '/' + f)
            log('rm -rf ' + dir + '/' + f)

remove_dir(AOSP_PATH + '/sdk/full_sdk')

log(AOSP_PATH + '/sdk/full_sdk/full_sdk.jar')

log('success')