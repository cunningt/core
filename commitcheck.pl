#!/usr/bin/perl

$ENV{'MAVEN_HOME'} = "/usr/local/maven";
$ENV{'JAVA_HOME'} = "/usr/java/jdk1.6.0_22"; 
#system ("mvn -Dorg.switchyard.findbugs.enable install");

my $firefoxlist = "";

open (CHECKSTYLE, "find . | grep checkstyle.html |");
while ($chkhtml = <CHECKSTYLE>) {
	chomp $chkhtml;
	print $chkhtml . "\n";
	$firefoxlist .= $chkhtml . " ";
}
close(CHECKSTYLE);

open (FINDBUGS, "find . | grep findbugs.html |");
while ($fbhtml = <FINDBUGS>) {
	chomp $fbhtml;
	print $fbhtml . "\n";
	$firefoxlist .= $fbhtml . " ";
}
close (FINDBUGS);

system ("firefox $firefoxlist");
