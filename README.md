# dztools

Here you find two tools for trading with [Dukascopy](http://www.dukascopy.com) over the [Zorro](http://www.takemoneyfromtherichandgiveittothepoor.com/) platform.

## dzplugin

This is the Dukascopy Broker plugin which implements the [Zorro-Broker-API](http://www.zorro-trader.com/manual/en/brokerplugin.htm).

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

