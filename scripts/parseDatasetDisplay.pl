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

foreach my $dataset ($dom->findnodes('/result/dataSet')) {
   # title, person uuid only include if workflow is validated
   my $title = $dataset->findnodes('./title')->to_literal();
   my $workflow = $dataset->findnodes('./workflow')->to_literal();
   my $persons = $dataset->findnodes('./personAssociations/personAssociation/person/@uuid')->to_literal();
   my $ds = $dataset->findnodes('./@uuid')->to_literal();
   foreach my $person ($dataset->findnodes('./personAssociations/personAssociation/person/@uuid')){
      print $person->to_literal() . "\t" . $ds . "\t" . $title . "\t" . $workflow . \n";
   }
}
