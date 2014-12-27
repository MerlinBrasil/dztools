@if (@X)==(@Y) @end /*
start cmd.exe
cscript //E:JScript //nologo "%~f0"
exit/b
*/
var obj = new ActiveXObject("WScript.Shell");
obj.SendKeys("java -jar dzconverter-0.9.0.jar ");