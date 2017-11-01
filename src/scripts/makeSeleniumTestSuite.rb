#!/usr/bin/ruby -w
require 'date'

path = "./"

def addTests(path, file)
    for i in Dir[path + file]
      puts "<tr><td><a target=\"testFrame\" href=\"#{i}\">#{i}</a></td></tr>"
    end # end file for loop
end # end method

d = DateTime.now
puts "<table>"
puts "<tr><td>Test Suite: #{d.month}-#{d.mday}-#{d.year}</td></tr>"
addTests(path, "web*.htm*")
puts "/<table>"
STDERR << "\n>>>> OK, now run this in your browser: \n"
STDERR << "chrome://selenium-ide/content/selenium/TestRunner.html?auto=true&baseURL=http://dev/&test=file:///C:"
STDERR << "\n"