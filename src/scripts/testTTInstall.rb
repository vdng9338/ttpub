#!/usr/bin/ruby -w
testString="Portland, Oregon"
path="//ares/webdev/schedules/"
newFolder=""

def check(path, newFolder, file, testString)
    for i in Dir[path + file]
      print i + "\n"
      begin
        f = File.read(i)
      rescue
        print "Can't open: " + i + "\n"
      else
        array = f.scan(%r{<a href=.*t1.*_[01].htm.*?>})
        for link in array
           ttFile = link[%r{./t1.*_[01].htm}]
           begin
             tt = File.read(path + newFolder + ttFile)
             test = tt.scan(testString)
             if test.nil? || test.empty?
               print "ERROR: I don't see the test string '" + testString + "' in file " + newFolder + ttFile + "\n"
             end
           rescue
              print "WARN: Missing TimeTable: " + path + newFolder + ttFile + "\n"
           end # end begin/rescue exception
        end # end link for loop
      end # end begin/rescue exception
    end # end file for loop
end # end method

check(path, newFolder, "max*.htm", testString)
check(path, newFolder, "r???.htm", testString)
