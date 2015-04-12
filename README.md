# Diagramaphone
### A senior design project at the University of Kentucky

Authors

    Jesse Hall
    Bryan Potere
    Julian Viso
    Larry Williamson

## Usage
This project is an android application developed in Android Studio.  This repository includes the gradle files that make is possible to generate an Android Studio project directly from VCS (choose "Check out project from Version Control after first opening Android Studio).

This repository contains the debug/run implementation of the project which includes log file generation.  See the following for generating a release .apk version.

    http://developer.android.com/tools/publishing/preparing.html

## Included files

*AndroidManifest.xml*                           See this file for the permissions and activities in the project.

*app/src/java/MainActivity.java*                This file contains the main source code for this project.

*app/build.gradle*                              Specifies the sdk versions that this project uses.  Current min is
14, and target is 21.  Also specifies the tess-two dependency for app.

*libraries/tess-two/build.gradle*               Specifies the sdk versions and location of jniLibs.

*settings.gradle*                               Rules for which parts of the project to build.

#Synopsis

The goal of this project is to provide additional information to visually impaired users that want to access technical charts and diagrams.  Currently the project is capable of optical character recognition of a small region surrounding the user's touch location.  Auditory and haptic cues should give some information about the content of the image to the user.  
Future releases could introduce additional analysis of the image including the relation of text to graphical content and its relation to each other.    

# Attributions
## tess-two
This project makes use of tess-two by Robert Theis at Google, available at the following repository.

    https://github.com/rmtheis/tess-two
The tess-two library is an android wrapper for tesseract, an optical character recognition library written in C.  It is distributed under the following license.

Tutorial for building this library:

    https://coderwall.com/p/eurvaq/tesseract-with-andoird-and-gradle
*Note that it tells you to delete the libs folder because you don't need it.  This is completely wrong!  Included in this project as jniLibs.*

### License
/*
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

## OpenCV
This project includes but does not implement use of the OpenCV for android library.  OpenCV is an open source computer vision project available at the following location.

    http://opencv.org/
    
This project initially planned to use OpenCV to detect the locations of shapes in images and how they relate to each other, but this was too ambitious for a only a single semester's worth of time to complete.

OpenCV is available under the following license.

### License

    http://opencv.org/license.html

By downloading, copying, installing or using the software you agree to this license.
If you do not agree to this license, do not download, install, copy or use the software.

License Agreement
For Open Source Computer Vision Library
(3-clause BSD License)

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the names of the copyright holders nor the names of the contributors may be used to endorse or promote products derived from this software without specific prior written permission.

This software is provided by the copyright holders and contributors “as is” and any express or implied warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall copyright holders or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of
the use of this software, even if advised of the possibility of such damage.
