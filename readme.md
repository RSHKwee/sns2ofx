# Introduction

The intent of this (java) application is to convert sns (www.snsbank.nl) CAMT053 xml files to ofx files 
that can be read by a program like GnuCash (www.gucash.org).

The origin is a Python script which can be found on Github:  https://github.com/chmistry/sns2ofx/releases.
The original script needed some modifications due to a new Python version and some changes in the SNS CSV format.

Due to Python installation and versionsns problems, this Java applicaton was born.
The Python script(s) are rewritten in Java.

The ofx specification can be downloaded from http://www.ofx.net/

A tutorial on how to keep your bank records in GnuCash can be read on:
http://www.chmistry.nl/financien/beginnen-met-boekhouden-in-gnucash/

# Opensns menu
When runnsns the application (Windows excutable or Java jar-file) the followsns menu is shown:

![Main screen sns2ofx](./sns2ofxMain.PNG)

Button _XML File(s)_: an SNS XML file can be choosen.

Button _Output folder_: Point to the directory where the generated OFX-file(s) are stored. 
A proposal for the "Output filename" is made, this can be changed.

Button _Convert to OFX_: The conversion to OFX format is started, the progres is shown in the lower panel.
<br>
Button _Start GnuCash_: GnuCash is started.

# Settsns menu

![Settsnss menu](./sns2ofxSettsnss.PNG)

In the "settsnss" menu the followsns options are available:
- A SNS CSV file may contain transactions for more then one account, all converted OFX transcations can be stored in one OFX file or per account a separate OFX file (default).
- Where to find _GnuCash_ executable.
- For debuggsns a _loglevel_ can be defined, default is level _INFO_.
- _Look and Feel_ of the GUI can be adjusted.
- _Create logfiles_ in the choosen directory a HTML- and a textfile with loggsns is created.
