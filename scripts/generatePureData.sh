#!/bin/bash

/usr/bin/curl "https://www-test.pure.ed.ac.uk/ws/api/59/persons?apiKey=52eead0d-ec0e-44f0-9c80-66fe4518af49&fields=ids.id&size=20000" > /tmp/purePerson.xml
/usr/bin/perl parsePerson.pl -xmlFile /tmp/purePerson.xml > /tmp/person.flat
/bin/rm /tmp/purePerson.xml

/usr/bin/curl "https://www-test.pure.ed.ac.uk/ws/api/59/datasets?apiKey=52eead0d-ec0e-44f0-9c80-66fe4518af49&fields=title,personAssociations.personAssociation.person.uuid,workflow&size=10000" > /tmp/pureDatasetDisplay.xml
/usr/bin/perl parseDatasetDisplay.pl -xmlfile /tmp/pureDatasetDisplay.xml > /tmp/datasetDisplay.flat
/bin/rm /tmp/pureDatasetDisplay.xml

/usr/bin/curl "https://www-test.pure.ed.ac.uk/ws/api/59/datasets?apiKey=52eead0d-ec0e-44f0-9c80-66fe4518af49&&size=10000" > /tmp/pureDatasetFull.xml
/usr/bin/perl parseDatasetFull.pl -xmlfile /tmp/pureDatasetFull.xml > /tmp/datasetFull.flat
/bin/rm /tmp/pureDatasetFull.xml

