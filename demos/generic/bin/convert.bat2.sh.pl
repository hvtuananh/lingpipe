
#run by invoking
#> perl convert.bat2.sh.pl *.bat
#
#You will be prompted for either a gui or a just see the script name printed with no apparent behavior. With the gui, you should test, close
#and the next item will appear. The cmd* will require a Ctl-c to terminate
#but it tests that java starts up properly.

 
use warnings;
use strict;

foreach my $filename (@ARGV) {
    open(INFILE,"$filename") || die "unto $filename";
    next if $filename !~/.bat/;
    $filename =~s/.bat//;
    my $newFileName = "$filename.sh";
    open(OUTFILE,">$filename.sh");
    print "$filename\n";
    while (<INFILE>) {
	s/^set //;
	s/;/:/g;
	s/([ =])%/$1\$/g;
	s/%//g;
	print OUTFILE;
    }
    system("sh $newFileName");
}
