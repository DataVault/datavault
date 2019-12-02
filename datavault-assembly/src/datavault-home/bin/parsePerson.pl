#!/usr/bin/perl

use strict;
use warnings;
use utf8;

use XML::LibXML;
use Getopt::Long;
binmode STDOUT, ":utf8";
my $opt = {};
GetOptions($opt, "xmlFile=s");
die "Usage is perl parsePerson.pl -xmlFile ./xmlFile.xml\n" unless exists $opt->{xmlFile};

my $filename = $opt->{xmlFile};

my $dom = XML::LibXML->load_xml(location => $filename);

foreach my $person ($dom->findnodes('/result/items/person')) {
    my $idType = '';
    foreach my $id ($person->findnodes('./ids/id')) {
        my $type = $id->findnodes('./type/term/text');
        if ($type eq 'Employee ID') {
           my $employeeId = $id->findnodes('./value');
           print $person->findnodes('./@uuid')->to_literal() . "\t" . $employeeId . "\n";
        }
    }
}
