#!/bin/sh
java -cp "../modules/*:../lib/*" -Dsnap.mainClass=org.esa.snap.core.gpf.main.GPT -Dsnap.home="../" -Xmx4G org.esa.snap.runtime.Launcher "$@"