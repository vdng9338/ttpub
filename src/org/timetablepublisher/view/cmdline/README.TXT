This Application will generate Web TimeTables, including HTML and PDF files.

It will generate a .ZIP file full of the data.  

There is a test tool that we use at TriMet to confirm a complete TT install.
It requries Ruby.  It will read a bunch of the static landing pages for links
to the generated time tables (in TriMet's case, the t1.*_[01].htm pattern is
an indicator).  

To run the test program:  ruby testTTInstall.rb

NOTE: the .bat excecute scripts work...but the .sh script has been giving trouble
If you can't execute the .bat file, then set the proprerties of this file to 
'execute'.