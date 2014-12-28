# dztools

Here you find two tools for trading with [Dukascopy](http://www.dukascopy.com) over the [Zorro](http://www.takemoneyfromtherichandgiveittothepoor.com/) platform.

## dzplugin

This is the Dukascopy broker plugin which implements the [Zorro-Broker-API](http://www.zorro-trader.com/manual/en/brokerplugin.htm).

## dzconverter

When trading with Dukascopy you probably also want to use their historical price data. In case you already have them available in your local .cache folder(with bi5 file extension) this converter creates *.bar files which Zorro can use for trading and testing.

## General installation

1.) Download and install the latest **32-bit** [Java JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html). Make sure it is the 32-bit version(x86 suffix) since the tools are based on a 32-bit JVM. In case you already have a 32-bit JRE installation(check it with *java -version*) you might skip this step.

2.) Add ${yourJREinstallPath}\jre\bin\ and ${yourJREinstallPath}\jre\bin\client to the **front** of your *Path* environment variable(here is a [howto](http://www.computerhope.com/issues/ch000549.htm)).

3.) Download the [dztools.zip](https://github.com/juxeii/dztools/releases) archive.

4.) Extract the archive into ${yourZorroInstallPath}/Plugin folder.

## dzplugin usage

After extracting the dztools archive you should see a *dukascopy-{version}.dll* and a folder *dztools* in the Plugin directory of your Zorro installation.

Start Zorro and check if the Account drop-down-box shows *Dukascopy* as an available Broker.

Pick a script of your choice and press *Trade*. If everything is fine you should see that the login to Dukascopy has been successful.

The plugin stores its logs to dztools/dzplugin/logs/dzplugin.log(the default log level is *info*). If you encounter problems open dztools/dzplugin/log4j2.xml for configuring the log level. Then change the log level for the file dzplugin-appender to *debug* and save the file. A new Zorro session will now produce a more verbose dzplugin.log file which you can use to report errors.

You can also change the log level for the Dukascopy via dztools/dzplugin/log4j.properties.

## dzconverter usage

This little command line tool allows you to convert *.bi5 Dukascopy history files to the *.bar file format of Zorro.
- The tool converts only 1min bars of an **entire** year(except the current of course). This is currently the highest resolution of Zorro(IMHO a tick based version of Zorro is in preparation).
- Make sure that you have a full year history of past years, otherwise Zorro is not able to test.

**Read these steps carefully, otherwise you run the risk of losing your .cache files!!!**

1.) Go to your JForex installation path and locate the *.cache* directory(e.g. my path is D:\programs\JForex\\.cache).

2.) **Create a *cacheCopy* folder(or some other foler name of your choice).**

3.) Say you want to convert the EUR/USD history. **Copy .cache/EURUSD to cacheCopy!!** This step is needed since the program uses this [Dukascopy API](http://www.dukascopy.com/client/javadoc/com/dukascopy/api/system/IClient.html#setCacheDirectory%28java.io.File%29) method.

4.) Open dztools/dzconverter/config.properties with a text editor. Fill in your login credentials under *user* and *password*. For *cachedir* put your path to the *cacheCopy* here(always use two backslashes as in the given example path). Save this file.

5.) Open dztools/dzconverter/convert.bat. A cmd box window should appear with *java -jar dzconverter-$version}.jar*. If the batch file does not work you can also invoke the java command from a manually opened cmd window. 

6.) The tool expects two parameters: first the asset/instrument name, e.g. *EUR/USD*; second the year you want to convert, e.g. *2013*. A valid convert command sould look like this *java -jar dzconverter-$version}.jar EUR/USD 2013*.

7.) You will see all kind of console output and if the conversion went fine, you will find a *.bar* file in dztools/dzconverter/bars with the asset and year name as specified [here](http://www.zorro-trader.com/manual/en/export.htm).

For subsequent conversion you can skip steps 1,2 and 4.

For reporting/finding bugs adapt the *log4j2.xml* and *log4j.properties* files under dztools/dzconverter. Use the logs in the same way as described for the dzplugin.

## Remarks for the plugin and converter

- This a very early release so **don't expect it to be bug free!**
- Login to a real account is therefore not supported yet(although the code is in place).
- If you don't trust the binaries checkout the dztools project und build it manually(you need to know [maven](http://maven.apache.org/))
- Follow discussions for Zorro in the [forum](http://www.opserver.de/ubb7/ubbthreads.php?ubb=cfrm&c=1)
