#!/usr/bin/perl

use strict;
use warnings;
use utf8;

use XML::LibXML;
use Getopt::Long;
binmode STDOUT, ":utf8";
my $opt = {};
GetOptions($opt, "xmlFile=s");
die "Usage is perl parseDatasetDisplay.pl -xmlFile ./xmlFile.xml\n" unless exists $opt->{xmlFile};

my $filename = $opt->{xmlFile};

my $dom = XML::LibXML->load_xml(location => $filename);

foreach my $dataset ($dom->findnodes('/result/items/dataSet')) {
   my $ds = $dataset->findnodes('./@uuid')->to_literal();
   my $crisID = $dataset->findnodes('./@pureId')->to_literal();
   my $title = $dataset->findnodes('./title')->to_literal();
   $dataset =~ s/\n//g;
   $dataset =~ s/\t//g;
   print $ds . "\t" . $title . "\t" . $dataset . "\t" . $crisID . "\n";
}
