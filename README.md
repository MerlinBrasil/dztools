# dztools

Here you find two tools for trading with [Dukascopy](http://www.dukascopy.com) over the [Zorro](http://www.takemoneyfromtherichandgiveittothepoor.com/) platform.

## dzplugin

This is the Dukascopy Broker plugin which implements the [Zorro-Broker-API](http://www.zorro-trader.com/manual/en/brokerplugin.htm).

## dzconverter

When trading with Dukascopy you probably also want to use their historical price data. In case you already have them available in your local .cache folder(with bi5 file extension) this converter creates *.bar files which Zorro can use for trading and testing.

## General installation

1.) Download and install the latest **32-bit** [Java JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html). Make sure it is the 32-bit version since the tools are based on a 32-bit JVM. In case you already have a 32-bit JRE installation(check it with *java -version*) you might skip this step.

2.) Add <yourJREintsallPath>\jre\bin\ and <yourJREintsallPath>\jre\bin\client to the **front** of your *Path* environment variable(here is a [howto](http://www.computerhope.com/issues/ch000549.htm)).
