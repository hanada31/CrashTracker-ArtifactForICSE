# [CrashTracker](https://github.com/hanada31/CrashTracker)

[TOC]

## Install Requirements：

1. Python 3+ 
2. Java 1.8+
3. maven 3.6
4. Linux OS  (by default)
5. When using Windows, the separator should be changed from / to  \\ for part of commands. 

## Steps to run *CrashTracker* 

*Note: Only **English** characters are allowed in the path.*

The default usage is to perform **Android** framework-specific  fault localization using CrashTracker.jar. 

You have two choices:

   **Choice 1:** build and run *CrashTracker* to analyze single apk class Folder: 

```
# Use -DskipTests to skip tests of soot (make build faster)
mvn -f pom.xml clean package -DskipTests

# Copy jar to root directory
cp target/CrashTracker.jar CrashTracker.jar

# unzip all the androidx.zip files in the ../FrameworkETS folder first.
unzip ../FrameworkETS/android2.3.zip -d ../FrameworkETS/android2.3/
unzip ../FrameworkETS/android4.4.zip -d ../FrameworkETS/android4.4/
unzip ../FrameworkETS/android5.0.zip -d ../FrameworkETS/android5.0/
unzip ../FrameworkETS/android6.0.zip -d ../FrameworkETS/android6.0/ 
unzip ../FrameworkETS/android7.0.zip -d ../FrameworkETS/android7.0/
unzip ../FrameworkETS/android8.0.zip -d ../FrameworkETS/android8.0/
unzip ../FrameworkETS/android9.0.zip -d ../FrameworkETS/android9.0/
unzip ../FrameworkETS/android10.0.zip -d ../FrameworkETS/android10.0/
unzip ../FrameworkETS/android11.0.zip -d ../FrameworkETS/android11.0/
unzip ../FrameworkETS/android12.0.zip -d ../FrameworkETS/android12.0/

# Run the tool
## for apk ..files (When using Windows, the separator should be changed from / to \)
java -jar CrashTracker.jar  -path M_application -name cgeo.geocaching-4450.apk -androidJar platforms  -crashInput  ../CrashDataset/crashInfo.json  -exceptionInput ../FrameworkETS -client ApkCrashAnalysisClient -time 30  -outputDir results/output

## for java libraries based on android framework files (When using Windows, the separator should be changed from / to \)
java -jar CrashTracker.jar  -path M_application -name facebook-android-sdk-905.jar -androidJar platforms  -crashInput ../CrashDataset/crashInfo.json  -exceptionInput ../FrameworkETS -client JarCrashAnalysisClient -time 30  -outputDir results/output

you can config the -path, -name, -androidJar and -outputDir.
```

   **Choice 2:**  analyze apks under given folder with Python script:

```
First, modify the sdk folder in the scripts.

Then, run the .py file.

# for apk files
python scripts/runCrashTracker-Apk.py  [apkPath] [resultPath] [target framework version] [strategy name]
e.g., python ./scripts/runCrashTracker-Apk.py  M_application results "no" "no"

# for java libraries based on android framework files
python scripts/runCrashTracker-Jar.py  [apkPath] [resultPath] [target framework version] [strategy name]
e.g., python scripts/runCrashTracker-Jar.py  M_application results "no" "no"

- [target framework version]: E.g., "2.3", "4.4", "6.0", "7.0", "8.0", "9.0", "10.0", "11.0", "12.0" or "no". Pick "no" if the crash triggering framework version is unknown.

- [strategy name]:  "NoCallFilter", "NoSourceType", "ExtendCGOnly",  "NoKeyAPI", "NoParaChain, "NoAppDataTrace", "NOParaChainANDDataTrace"or "no". Pick "no" to use the best/default strategy.
```

**(Optional)**

If you want to localize other versions of Android framework using CrashTracker.jar.

Extracting exception-thrown summary (ETS) for that framework is required before the localization!

```
# Use the following commands to analyze your framework files.
python scripts/runCrashTracker-framework.py [framework code location] [version] [outputDir]  

For example, if the sturcture of your files is as follows:
- CrashTrackerTool
    |-- framework
     |-- android5.0
        |-- a list of .class files extracted from android.jar files (do not use the android.jar file in the Android SDK, as they 			have empty implementation. Instead, extract android.jar files from your android phone or an emulator with the target 			version. Also, you can download from  https://github.com/hanada31/AndroidFrameworkImpl and unzip files)

run: 
    python scripts/runCrashTracker-framework.py  M_framework 2.3 ETSResults
```



## CrashTracker.jar -h arguments

```
java -jar CrashTracker.jar -h

usage: java -jar CrashTracker.jar [options] [-path] [-name] [-androidJar] [-outputDir] [-crashInput] [-exceptionInput] [-client]
 -h                        -h: Show the help information.
 -client <arg>             -client 
 						   	   ExceptionInfoClient: Extract exception information from Android framework.
                               CrashAnalysisClient: Analysis the crash information for an apk.
                               JarCrashAnalysisClient: Analysis the crash information for an third party SDK.
                               CallGraphClient: Output call graph files.
                               ManifestClient: Output manifest.xml file.
                               IROutputClient: Output soot IR files.
 -name <arg>               -name: Set the name of the apk under analysis.
 -path <arg>               -path: Set the path to the apk under analysis.
 -crashPath <arg>          -crashInput: crash information file.
 -exceptionInput <arg>     -exceptionPath: exception file folder.
 -androidJar <arg>         -androidJar: Set the path of android.jar.
 -frameworkVersion <arg>   -frameworkVersion: The version of framework under analysis
 -strategy <arg>           -strategy: effectiveness of strategy "NoCallFilter", "NoSourceType", "ExtendCGOnly",  "NoKeyAPI", "NoParaChain, "NoAppDataTrace", "NOParaChainANDDataTrace"or "no"
 -time <arg>               -time [default:90]: Set the max running time (min).
 -outputDir <arg>          -outputDir: Set the output folder of the apk.

```

## Examples for CrashTracker‘s report 

In folder EvaluationData/Fault Localization Results//

- *Six types of reasons provided by CrashTracker.*

  - **a)**  **Key_API_Related**: Caller of *keyAPI*;
  - **b)**  **Key_Variable_Related_1**: Influences the value of *keyVar* by modifying the value of the passed parameters;
  - **c)**  **Key_Variable_Related_2**: Influences the value of *keyVar* by modifying the value of related object fields;
  - **d)**  **Executed_Method_1**: Not influence the *keyVar* but in crash trace; 
  - **e)**  **Executed_Method_2**: Not in the crash stack but has been executed;
  - **f)**   **Not_Override_Method**: Forgets to override the *Signaler* method.

- *The simplified report by CrashTracker for com.travelzoo.android.apk.*

  - As we can see, there is one reason for this buggy candidate. The crash is triggered when a framework API that throws an exception without any condition is invoked, and the override is required.

    ![com.nextgis.mobile](../Figures/com.nextgis.mobile.png)



- *The simplified report by CrashTracker for com.nextgis.mobile.apk.*

  - As we can see, there are two types of reasons, explaining how the exception is triggered in both the application level (which influences the data passed to crashAPI) and framework level (which influences the exception-triggering condition in signaler).

  

![com.travelzoo.android](../Figures/com.travelzoo.android.png)



## Examples for ETS of exception 

In folder FrameworkETS

![ETS](../Figures/ETS.png)



