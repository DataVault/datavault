#!/bin/bash

/usr/bin/curl "https://$1.pure.ed.ac.uk/ws/api/$2/persons?apiKey=$3&fields=ids.id&size=30000" > $4/purePerson.xml
/usr/bin/perl $HOME/datavault-home/bin/parsePerson.pl -xmlFile $4/purePerson.xml > $4/person.flat
/bin/rm $4/purePerson.xml

/usr/bin/curl "https://$1.pure.ed.ac.uk/ws/api/$2/datasets?apiKey=$3&fields=title,personAssociations.personAssociation.person.uuid,workflow&size=30000" > $4/pureDatasetDisplay.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetDisplay.pl -xmlfile $4/pureDatasetDisplay.xml > $4/datasetDisplay.flat
/bin/rm $4/pureDatasetDisplay.xml

/usr/bin/curl "https://$1.pure.ed.ac.uk/ws/api/$2/datasets?apiKey=$3&size=30000" > $4/pureDatasetFull.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetFull.pl -xmlfile $4/pureDatasetFull.xml > $4/datasetFull.flat
/bin/rm $4/pureDatasetFull.xml

/usr/bin/curl "https://$1.pure.ed.ac.uk/ws/api/$2/projects?apiKey=$3&fields=project&size=30000" > $4/pureProjects.xml
/usr/bin/curl "https://$1.pure.ed.ac.uk/ws/api/$2/datasets?apiKey=$3&fields=dataSets,relatedProjects.relatedProjects&size=30000" > $4/pureDatasetProjects.xml
/usr/bin/perl $HOME/datavault-home/bin/parseDatasetProjects.pl -projectFile $4/pureProjects.xml -datasetFile $4/pureDatasetProjects.xml > $4/projectId.flat
/bin/rm $4/pureDatasetProjects.xml
/bin/rm $4/pureProjects.xml

