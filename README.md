# Sample Applications for Esper-Provisioned Devices

This repository contains sample device applications that demonstrate various Esper features. Feel free to use them for your own projects!

Here is a list of included projects:

1. EsperSampleKioskApp:

    This is a sample Kiosk mode app that shows how one can use Esper to quickly deploy a kiosk-mode app. It also shows the use of Esper's Device SDK to acquire the Device ID from a provisioned device.
    
2. EsperScriptTesterApp:

    This is a barebones app which can be used to test the functionality of the scripts namespace. The scripts namespace enables launching an activity, starting a service, or sending a broadcast intent to any application on a device using a POST request. This app is intended to be used in conjunction with the automated testing scripts described in the section below, or with any custom script command. 
    
    The app consists of six main components:

    - `MainActivity` which is launched when the app is first run, and is responsible for registering an implicit broadcast receiver for testing purposes. The receiver is registered on creation of the activity, so the test app must be launched first before testing sending an implicit broadcast.

    - `ImplicitActivity`, a blank activity which is used for testing launching with an implicit intent.
        - declared in the manifest with an intent filter for the action `com.example.scripttestapp.LAUNCH_IMPLICIT_ACTIVITY`

    - `ExplicitActivity`, a blank activity which is used for testing launching with an explicit intent.
        - declared in the manifest with an intent filter for the action `com.example.scripttestapp.LAUNCH_EXPLICIT_ACTIVITY`

    - `TestBackgroundService`, an example service which is used to test starting a background service.

    - `TestForegroundService`, an example service which is used to test starting a foreground service.
        - This service creates a notification when it is started in the foreground

    - `TestBroadcastReceiver`, an example broadcast receiver which can be used to test sending explicit AND implicit broadcast intents
        - declared in the manifest with an intent filter for the actions `com.example.esperscripttesterapp.EXPLICIT_BROADCAST` and `com.example.esperscripttesterapp.IMPLICIT_BROADCAST`

    All components (excluding the `MainActivity`) log to the console when they are successfully launched. The log contains the action and extras that were included with the intent that started the component. In addition, all log information is displayed in a toast message. Example log:

    ```
    I/ImplicitActivity: Sucessfully started with action: [com.example.scripttestapp.LAUNCH_IMPLICIT_ACTIVITY] and extras: {test: implicit activity launch,version: 1}
    ```
    
3.  automated-script-tests:

    This folder contains a suite of json files with scripts to be tested on the EsperScriptTesterApp, as well as two bash scripts to make running tests faster.
    
    `setDeviceUUID.sh` is a bash script that can be used to update the UUID in all of the JSON bodies to be used for testing. This UUID should belong to the provisioned device you wish to run the tests on.
    
    - usage: `./setDeviceUUID.sh <UUID>`
        
    `execute_test.sh` is a bash script that can be used to run specified tests on a device, and record the logs in an output file. To run this test, you need a provisioned device connected via adb, with the `EsperScriptTesterApp` installed
    - The script takes in the Android Device Identifier of the device to be tested and an optional parameter to match the pattern of a specific test to run.
    - usage: `./execute_test.sh <android_device_id> <pattern>`
        - The Android Device Identifiers can be found by running the adb devices command 
             ```
                    $ adb devices
                    List of devices attached
                    PT99621BA1A32501291 device
                    emulator-5554.      device
             ```
         - Here are some valid example patterns: ”test_*”, “*activity*”, test_activity_implicit_2.json
         - full example: `./execute_test.sh emulator-5554 ”test_*”`

        1. When the script is run, the each example script in the json/ directory that matches the provided pattern (or all files if none is provided) will be executed. Logcat output from the device is saved in a file named `outputs.txt`. Allow the tests to run to completion (if you quit early, you will have to manually kill the logcat process that is running in the background).

        2. While running, you may see different activities being launched and toasts being displayed on the device. In the console, you will see the name of each JSON file being run.

        3. After the test completes, the `outputs.txt` file will contain several different logs. Logs should come from the ScriptProcessor class, as well as any of the test app's components when they are successfully launched. 
        
        *Note: In order to test test_broadcast_implicit.json properly, you must launch the EsperScriptTesterApp before running the test, (in order to register to receive the implicit broadcast)*
    
    `json/` contains all the json bodies used for testing the scripts namespace. 
    - All tester JSON files have a similar format.
    - Files with names that begin with “err_” are for verifying that scripts with errors (such as missing parameters or invalid parameter values) behave as expected.
    - Files that begin with “test_” are for testing intended behavior on the sample app.
        - Some file names end with “_1” or “_2”.
            - 1: does NOT include the launchType parameter 
            - 2: does include the parameter.
        - To improve logging, two extras are usually included in an example script. The first extra, “test,” contains a simple description of the test that is being run. The second extra, “version,” (similar to the number in the JSON file name), indicates whether or not the launchType parameter is used in that test.
            ```    
            "extras": {
             "test": "implicit activity launch",
            "version": 1
            }
            ```
