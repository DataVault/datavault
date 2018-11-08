#!/bin/bash

/usr/bin/curl "https://www.pure.ed.ac.uk/ws/api/59/persons?apiKey=$1&fields=ids.id&size=20000" > $2/purePerson.xml
/usr/bin/perl $HOME/datavault-home/bin/parsePerson.pl -xmlFile $2/purePerson.xml > $2/person.flat
/bin/rm $2/purePerson.xml

/usr/bin/curl "https://www.pure.ed.ac.uk/ws/api/59/datasets?apiKey=$1&fields=title,personAssociations.personAssociation.person.uuid,workflow&size=10000" > $2/pureDatasetDisplay.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetDisplay.pl -xmlfile $2/pureDatasetDisplay.xml > $2/datasetDisplay.flat
/bin/rm $2/pureDatasetDisplay.xml

/usr/bin/curl "https://www.pure.ed.ac.uk/ws/api/59/datasets?apiKey=$1&size=10000" > $2/pureDatasetFull.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetFull.pl -xmlfile $2/pureDatasetFull.xml > $2/datasetFull.flat
/bin/rm $2/pureDatasetFull.xml

