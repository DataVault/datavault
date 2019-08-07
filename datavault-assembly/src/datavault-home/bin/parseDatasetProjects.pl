#!/usr/bin/perl
  
use strict;
use warnings;
use utf8;

use XML::LibXML;
use Getopt::Long;
binmode STDOUT, ":utf8";
my $opt = {};
GetOptions($opt, "projectFile=s", "datasetFile=s");
die "Usage is perl parseDatasetProjects.pl -projectFile ./xmlFile.xml -datasetFile ./xmlFile\n" unless exists $opt->{projectFile} && exists $opt->{datasetFile};

my $projectFilename = $opt->{projectFile};
my $datasetFilename = $opt->{datasetFile};

my $projectDom = XML::LibXML->load_xml(location => $projectFilename);
my $datasetDom = XML::LibXML->load_xml(location => $datasetFilename);

my %projectHash;

foreach my $project ($projectDom->findnodes('/result/project')) {
   my $uuid = $project->findnodes('./@uuid')->to_literal();
   my $pureid = $project->findnodes('./@pureId')->to_literal();
   #print $uuid . "\t" . $pureid . "\n";
   $projectHash{$uuid} = $pureid;
}

#foreach my $project (keys %projectHash) {
#   print $project . "\t" . $projectHash{$project} . "\n";
#}

foreach my $project ($datasetDom->findnodes('/result/dataSet')) {
   my $datasetUuid = $project->findnodes('./@uuid')->to_literal();
   #my @projectUuid = $project->findnodes('./relatedProjects/relatedProjects/@uuid')->to_literal();
   my @projectUuid = $project->findnodes('./relatedProjects/relatedProjects/@uuid');
   my $firstUuid = "";
   if (@projectUuid) {
      $firstUuid = shift @projectUuid;
   }
   if (defined $firstUuid && $firstUuid ne "") { 
      #print "'" . $firstUuid->to_literal() . "'\n";
      if (exists $projectHash{$firstUuid->to_literal()}) {
         print $datasetUuid . "\t" . $projectHash{$firstUuid->to_literal()} . "\n";
      } else {
         print $datasetUuid . "\t" . "Not found" . "\n";
      }
   } else {
      print $datasetUuid . "\t" . "Unknown" . "\n";	
   }
}

