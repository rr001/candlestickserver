@echo off
setlocal enabledelayedexpansion
set "jvm_opts=-Djava.net.preferIPv4Stack=true"
set "main_class=%1"

for /r . %%g in (../lib/*.jar) do (
  set "jar=%%~nxg"
  set "cp=!cp!;../lib/!jar!"
)

set "cp=..;.;../conf/;../classes/;!cp!"
java !jvm_opts! -cp !cp! !main_class!

endlocal

