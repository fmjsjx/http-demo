#!/bin/bash


# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
cd ${PRGDIR}


nobuild="false"
show_help="false"
profile_dev="false"
profile_test="false"
profile_pre="false"
profile_prd="false"
doreboot="false"
openapi="false"

for arg in $*; do
  if [ "$arg" = "-h" ]; then
    show_help="true"
  elif [ "$arg" = "--help" ]; then
    show_help="true"
  elif [ "$arg" = "--dev" ]; then
    profile_dev="true"
  elif [ "$arg" = "--test" ]; then
    profile_test="true"
  elif [ "$arg" = "--pre" ]; then
    profile_pre="true"
  elif [ "$arg" = "--prd" ]; then
    profile_prd="true"
  elif [ "$arg" = "--all" ]; then
    profile_test="true"
    profile_dev="true"
  elif [ "$arg" = "--nobuild" ]; then
    nobuild="true"
  elif [ "$arg" = "--reboot" ]; then
    doreboot="true"
  elif [ "$arg" = "--openapi" ]; then
    openapi="true"
  elif [ "$arg" = "--openapi-dev" ]; then
    openapi_dev=true
  fi
done

if [ "$show_help" = "true" ]; then
  echo "Usage:"
  echo "  $0 [options]"
  echo "Options:"
  echo "  -h --help      print help"
  echo "  --all          install both dev & test profiles"
  echo "  --dev          install dev profile"
  echo "  --test         install test profile"
  echo "  --pre          install pre profile"
  echo "  --prd          install prd profile"
  echo "  --nobuild      will not exec 'mvn clean package' command"
  echo "  --reboot       will exec 'systemctl restart http-demo' command after build"
  echo "  --openapi      will update openapi(HTTP RESTful doc)"
  echo "  --openapi-dev  will update openapi-dev(HTTP RESTful doc DEV version)"
  echo ""
  exit 0
fi

# openapi
if [ "$openapi" = "true" ]; then
  rsync -vrct doc/openapi/ /usr/share/nginx/html/openapi/
  echo "openapi updated"
fi
if [ "$openapi_dev" = "true" ]; then
  rsync -vrct doc/openapi/ /usr/share/nginx/html/openapi-dev/
  echo "openapi updated"
fi

# build
if [ "$nobuild" = "false" ]; then
  mvn clean package
fi

# dev
if [ "$profile_dev" = "true" ]; then
  echo ""
  echo "-- profile: dev --"
  rsync -vct target/http-demo-*.jar /opt/http-demo/
  rsync -vct src/main/conf/error-message.yml /opt/http-demo/conf/
  echo "installed on dev profile"
  if [ "$doreboot" = "true" ]; then
    supervisorctl restart http-demo
    echo "rebooted on dev profile"
  fi
fi

# test
if [ "$profile_test" = "true" ]; then
  echo ""
  echo "-- profile: test --"
  rsync -vct target/http-demo-*.jar test:/opt/http-demo/
  rsync -vct src/main/conf/error-message.yml test:/opt/http-demo/conf/
  echo "installed on test profile"
  if [ "$doreboot" = "true" ]; then
    ssh test-140 supervisorctl restart http-demo
    echo "rebooted on test profile"
  fi
fi

# pre
#if [ "$profile_pre" = "true" ]; then
#  echo ""
#  echo "-- profile: pre --"
#  rsync -vct target/http-demo-*.jar host-pre:/opt/http-demo/
#  rsync -vct src/main/conf/error-message.yml host-pre:/opt/http-demo/conf/
#  echo "installed on pre profile"
#  if [ "$doreboot" = "true" ]; then
#    ssh root@host-pre supervisorctl restart http-demo
#    echo "rebooted on pre profile"
#  fi
#fi

# prd
#if [ "$profile_prd" = "true" ]; then
#  echo ""
#  echo "-- profile: prd --"
#  hosts="host-prd-001"
#  for host in $hosts; do
#    echo "install to remote host $host ..."
#    rsync -vct target/http-demo-*.jar $host:/opt/http-demo/
#    rsync -vct src/main/conf/error-message.yml $host:/opt/http-demo/conf/
#    echo "OK"
#  done
#  echo "installed on prd profile"
#  if [ "$doreboot" = "true" ]; then
#    for host in $hosts; do
#      echo "reboot on remote host $host..."
#      ssh root@$host supervisorctl restart http-demo
#      echo "OK"
#    done
#    echo "rebooted on prd profile"
#  fi
#fi

echo "======================================="
echo "done"
