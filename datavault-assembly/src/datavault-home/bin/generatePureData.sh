#!/bin/bash

/usr/bin/curl "https://$1.pure.ed.ac.uk/ws/api/$2/persons?apiKey=$3&fields=ids.id&size=20000" > $4/purePerson.xml
/usr/bin/perl $HOME/datavault-home/bin/parsePerson.pl -xmlFile $4/purePerson.xml > $4/person.flat
/bin/rm $4/purePerson.xml

/usr/bin/curl "https://$1.pure.ed.ac.uk/ws/api/$2/datasets?apiKey=$3&fields=title,personAssociations.personAssociation.person.uuid,workflow&size=10000" > $4/pureDatasetDisplay.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetDisplay.pl -xmlfile $4/pureDatasetDisplay.xml > $4/datasetDisplay.flat
/bin/rm $4/pureDatasetDisplay.xml

/usr/bin/curl "https://$1.pure.ed.ac.uk/ws/api/$2/datasets?apiKey=$3&size=10000" > $4/pureDatasetFull.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetFull.pl -xmlfile $4/pureDatasetFull.xml > $4/datasetFull.flat
/bin/rm $4/pureDatasetFull.xml

