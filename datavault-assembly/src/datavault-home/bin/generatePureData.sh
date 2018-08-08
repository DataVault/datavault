#!/bin/bash

/usr/bin/curl "https://www.pure.ed.ac.uk/ws/api/59/persons?apiKey=$1&fields=ids.id&size=20000" > /tmp/purePerson.xml
/usr/bin/perl $HOME/datavault-home/bin/parsePerson.pl -xmlFile /tmp/purePerson.xml > /tmp/person.flat
/bin/rm /tmp/purePerson.xml

/usr/bin/curl "https://www.pure.ed.ac.uk/ws/api/59/datasets?apiKey=$1&fields=title,personAssociations.personAssociation.person.uuid,workflow&size=10000" > /tmp/pureDatasetDisplay.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetDisplay.pl -xmlfile /tmp/pureDatasetDisplay.xml > /tmp/datasetDisplay.flat
/bin/rm /tmp/pureDatasetDisplay.xml

/usr/bin/curl "https://www.pure.ed.ac.uk/ws/api/59/datasets?apiKey=$1&size=10000" > /tmp/pureDatasetFull.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetFull.pl -xmlfile /tmp/pureDatasetFull.xml > /tmp/datasetFull.flat
/bin/rm /tmp/pureDatasetFull.xml

