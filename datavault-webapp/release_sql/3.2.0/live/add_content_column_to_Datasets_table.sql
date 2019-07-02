-- add new column
-- nullable for now (as the existing data needs fixed first)

ALTER TABLE Datasets
ADD Content longtext;

-- fix existing entries
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="6f666eee-14ce-4de2-ae70-77eb42d63725" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>Suppre
ssing inflammation to enhance antigen-specific immunity in older humans using p38MAPK inhibitors and vitaminD3 (MRC grant MR/M003833/1)</title><type uri="/dk/atira/pure/dataset/datasettypes/dat
aset/dataset">Dataset</type><description typeUri="/dk/atira/pure/dataset/descriptions/datasetdescription" type="Description">"## Access ##&#xd;
This dataset is held in the Edinburgh DataVault, directly accessible only to authorised University of Edinburgh users.  </description><description typeUri="/dk/atira/pure/dataset/descriptions/a
bstract" type="Abstract">Older humans are more susceptible to infections, even those to which they were immune to in their youth. Vaccination against many different infectious agents is also su
b-optimal in these individuals. To investigate the mechanisms involved in the decline in immunity during ageing we inject safe derivatives of microorganisms into the skin of old and young volun
teers.  After injection of the agent, we can extract the white cells (leucocytes) that accumulate at the site of the immune response to test for their function.  One of the responses we have be
en investigating is that to the varicella zoster virus (VZV) that induces chickenpox mainly in young individuals. Infected subjects control this persistent virus until an older age where reduce
d immunity leads to viral re-activation to cause shingles.  Older humans are also susceptible to re-activation of TB and we can also test their response to tuberculosis (TB) proteins. Elderly s
ubjects have decreased capacity to respond to VZV in the skin that may be an underlying reason for their increased susceptibility to shingles.  These individuals also have decreased cutaneous r
esponses to TB related proteins and the immune defect in the skin may reflect the general defect in immunity in these individuals.  We have found that this defective response is associated with
 increased baseline inflammation in this tissue. Although inflammatory responses are required to clear infections, excessive inflammation has previously been shown to interfere with the generat
ion of specific immunity.  We will therefore investigate ways of enhancing the immune response of older humans by blocking basal inflammation. &#xd;
&#xd;
To extend our initial observations of high background inflammation in the skin, we will investigate which cells in this tissue are responsible for the production of inflammatory mediators by us
ing a second human experimental system that tests the response to a skin irritant known as cantharadin. Next we will block inflammation in the skin to determine whether this can lead to improve
d responses upon challenge with antigens. This will be achieved by pre-treating old subjects with a drug that has already been developed by GSK (Losmapimod, p38MAPkinase inhibitor) and another 
that is available off the shelf (vitamin D3). The GSK p38 inhibitors are currently being tested to prevent unwanted inflammation. A surprising observation we made was that p38 inhibition could 
also rejuvenate human T lymphocytes and enhance their ability to proliferate in vitro. Therefore the inhibition of p38 may block unwanted inflammatory response as well as enhance T lymphocyte r
eactivity. Vitamin D3 has also been shown to have anti-inflammatory effects and works in part by inhibiting p38MAPkinase activation. The use of 2 separate interventions in this project increase
s the likelihood of success in this study. While Losmapimod may be more specific in its anti-inflammatory action, the use of vitamin D3 is more cost effective as it is a cheap compound that is 
readily available. &#xd;
&#xd;
The desirable outcome of this study is that either one or other of these compounds, that older volunteers will be treated with, will boost their response to microbial antigen challenge in the s
kin. This will provide proof of concept data that will lead to the exciting possibility of enhancing immunity by inhibiting the increased baseline inflammatory responses that are found during a
geing. This may be a strategy to enhance immune responses in general and specifically to enhance vaccination efficacy to various diseases that is less effective in older subjects.  &#xd;
</description><description typeUri="/dk/atira/pure/dataset/descriptions/reason_for_dataset_access_restriction_and_conditions_for_release" type="Data Citation"/><dataProductionPeriod><startDate>
<year>2019</year><month>2</month><day>13</day></startDate><endDate><year>2019</year><month>2</month><day>13</day></endDate></dataProductionPeriod><personAssociations><personAssociation id="8005
8186"><person uuid="0a863546-b83e-4884-90cc-4d796da614d4"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/0a863546-b83e-4884-90cc-4d796da614d4?apiKey=b32edef1-c406-4263-ac
7d-401354cb28dd"/><name>Barbara Shih</name></person><name><firstName>Barbara</firstName><lastName>Shih</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</p
ersonRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-4
02e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/s
chool">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="80058188"><person uuid="0b08ec8b-f1ca-4dc0-8228-4342450290b4"><link ref="content" href=
"https://www.pure.ed.ac.uk/ws/api/59/persons/0b08ec8b-f1ca-4dc0-8228-4342450290b4?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Colin Simpson</name></person><name><firstName>Colin</firstN
ame><lastName>Simpson</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/datamanager">Data Manager</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-4
02e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><nam
e>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personA
ssociation><personAssociation id="84961310"><externalPerson uuid="96472e89-d41b-48db-80bf-81eef4595821"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-persons/96472e89-d
41b-48db-80bf-81eef4595821?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Arne Akbar</name><type uri="/dk/atira/pure/externalperson/externalpersontypes/externalperson/externalperson">Exter
nal person</type></externalPerson><name><firstName>Arne</firstName><lastName>Akbar</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/owner">Owner</personRole><externalOrgan
isations><externalOrganisation uuid="273dbd87-bcee-46d9-a669-e9286ac24dd4"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-organisations/273dbd87-bcee-46d9-a669-e9286ac24
dd4?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>UCL</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternalorganisationtypes/ueoexternalorganisation/uk_university_hei">UK Un
iversity / HEI</type></externalOrganisation></externalOrganisations></personAssociation><personAssociation id="80058190"><person uuid="3cd693a6-dc98-46d0-bc23-052fd14a1704"><link ref="content" 
href="https://www.pure.ed.ac.uk/ws/api/59/persons/3cd693a6-dc98-46d0-bc23-052fd14a1704?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Neil Mabbott</name></person><name><firstName>Neil</fir
stName><lastName>Mabbott</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/owner">Owner</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-0
5559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (D
ick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="29338e9c-e90d-4
e14-9601-5c075828a42b"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/29338e9c-e90d-4e14-9601-5c075828a42b?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><nam
e>Edinburgh Neuroscience</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/research_group">Research Group</type></organisationalUnit><organisationalUnit uuid="33cb0404
-f599-4803-ab16-ac7f5e3a7997"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/33cb0404-f599-4803-ab16-ac7f5e3a7997?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd
"/><name>Edinburgh Imaging </name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/research_centre">Research Centre</type></organisationalUnit></organisationalUnits></perso
nAssociation><personAssociation id="80058194"><person uuid="62e5166b-05dc-4cd4-8678-618d3727348a"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/62e5166b-05dc-4cd4-8678-6
18d3727348a?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Tom Freeman</name></person><name><firstName>Thomas</firstName><lastName>Freeman</lastName></name><personRole uri="/dk/atira/pure/
dataset/roles/dataset/owner">Owner</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59
/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisa
tion/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation></personAssociations><managingOrganisationalUnit uuid="564daede-9678-402e
-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>R
oyal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></managingOrganisationalUnit><publisher uuid="2ef62159-
071c-48cc-b159-eb879377e986"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/publishers/2ef62159-071c-48cc-b159-eb879377e986?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Ed
inburgh DataVault</name><type uri="/dk/atira/pure/publisher/publishertypes/publisher/publisher">Publisher</type></publisher><physicalDatas><physicalData id="87193607"><title>Suppressing inflamm
ation to enhance antigen-specific immunity in older humans using p38MAPK inhibitors and vitaminD3 (MRC grant MR/M003833/1)</title><storageLocation>Edinburgh DataVault</storageLocation><accessDe
scription>This dataset is held in the DataVault, directly accessible only to authorised University of Edinburgh staff. </accessDescription><media>https://www.ed.ac.uk/is/research-support/datava
ult</media><type uri="/dk/atira/pure/dataset/documents/dataset">Dataset</type></physicalData></physicalDatas><contactPerson uuid="3cd693a6-dc98-46d0-bc23-052fd14a1704"><link ref="content" href=
"https://www.pure.ed.ac.uk/ws/api/59/persons/3cd693a6-dc98-46d0-bc23-052fd14a1704?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Neil Mabbott</name></contactPerson><legalConditions><legalC
ondition id="87193606"><description>This dataset is held in the Edinburgh DataVault, directly accessible only to authorised University of Edinburgh users. Requests for access will not necessari
ly be granted. </description></legalCondition></legalConditions><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.e
d.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/at
ira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="29338e9c-e90d-4e14-9601-5c075828a42b"><link ref="content" href="https://
www.pure.ed.ac.uk/ws/api/59/organisational-units/29338e9c-e90d-4e14-9601-5c075828a42b?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Edinburgh Neuroscience</name><type uri="/dk/atira/pure/
organisation/organisationtypes/organisation/research_group">Research Group</type></organisationalUnit><organisationalUnit uuid="33cb0404-f599-4803-ab16-ac7f5e3a7997"><link ref="content" href="h
ttps://www.pure.ed.ac.uk/ws/api/59/organisational-units/33cb0404-f599-4803-ab16-ac7f5e3a7997?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Edinburgh Imaging </name><type uri="/dk/atira/pu
re/organisation/organisationtypes/organisation/research_centre">Research Centre</type></organisationalUnit></organisationalUnits><externalOrganisations><externalOrganisation uuid="50c28c5e-aaea
-45d8-bb76-966d77afb641"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-organisations/50c28c5e-aaea-45d8-bb76-966d77afb641?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/>
<name>MRC</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternalorganisationtypes/ueoexternalorganisation/fundingbody">Funding body</type></externalOrganisation><externalOrganisati
on uuid="273dbd87-bcee-46d9-a669-e9286ac24dd4"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-organisations/273dbd87-bcee-46d9-a669-e9286ac24dd4?apiKey=b32edef1-c406-426
3-ac7d-401354cb28dd"/><name>UCL</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternalorganisationtypes/ueoexternalorganisation/uk_university_hei">UK University / HEI</type></exter
nalOrganisation></externalOrganisations><publicationDate><year>2019</year><month>2</month><day>13</day></publicationDate><openAccessPermission uri="/dk/atira/pure/dataset/accesspermission/restr
icted">Restricted</openAccessPermission><relatedProjects><relatedProjects uuid="b88ddc5a-5cff-42b5-b7bf-f65a4bccb792"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/projects/b88d
dc5a-5cff-42b5-b7bf-f65a4bccb792?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>MICA enhancing immunity in older humans by targeting the p38 MAPkinase signalling pathway and vitamin D supp
lementation</name><type uri="/dk/atira/pure/upmproject/upmprojecttypes/upmproject/research">Research</type></relatedProjects></relatedProjects><workflow workflowStep="validated">Validated</work
flow><visibility key="FREE">Public - No restriction</visibility><confidential>false</confidential><info><createdBy>bshih</createdBy><createdDate>2019-02-13T11:02:06.433Z</createdDate><modifiedB
y>pward2</modifiedBy><modifiedDate>2019-05-06T17:18:56.379+01:00</modifiedDate></info></dataSet>' where id = '6f666eee-14ce-4de2-ae70-77eb42d63725';
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="138c6ecb-07b2-426f-81cc-69c197be6c2e" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>Painti
ng with bacteria: Smart templated self assembly using motile bacteria (Raw data)</title><type uri="/dk/atira/pure/dataset/datasettypes/dataset/dataset">Dataset</type><description typeUri="/dk/a
tira/pure/dataset/descriptions/datasetdescription" type="Description">This is the complete dataset. The processed data is available on DataShare.</description><description typeUri="/dk/atira/pu
re/dataset/descriptions/abstract" type="Abstract">Complete dataset including all movies, supporting the manuscript entitled 'Painting with bacteria: Smart templated self assembly using motile b
acteria':&#xd;
 &#xd;
External control of the swimming speed of ‘active particles’ can be used to self assemble designer structures in situ on the µm to mm scale. We demonstrate such reconﬁgurable templated active s
elf assembly in a ﬂuid environment using light powered strains of Escherichia coli. The physics and biology controlling the sharpness and formation speed of patterns is investigated using a bes
poke fast-responding strain.</description><description typeUri="/dk/atira/pure/dataset/descriptions/reason_for_dataset_access_restriction_and_conditions_for_release" type="Data Citation">Arlt, 
J., Martinez, V. A., Dawson, A., Pilizota, T. &amp; Poon, W. C. K. Painting with bacteria: smart templated self assembly using motile bacteria (dataset). Edinburgh DataVault. 10.7488/138c6ecb-0
7b2-426f-81cc-69c197be6c2e. (2018)</description><dataProductionPeriod><startDate><year>2016</year><month>8</month><day>19</day></startDate><endDate><year>2017</year><month>8</month><day>11</day
></endDate></dataProductionPeriod><personAssociations><personAssociation id="69272290"><person uuid="2391660e-21ad-43a6-bf18-218efaf32591"><link ref="content" href="https://www.pure.ed.ac.uk/ws
/api/59/persons/2391660e-21ad-43a6-bf18-218efaf32591?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Jochen Arlt</name></person><name><firstName>Jochen</firstName><lastName>Arlt</lastName><
/name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="7d21e6de-e2dc-4598-a62d-410a3c55db98"><link ref="content"
 href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/7d21e6de-e2dc-4598-a62d-410a3c55db98?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Physics and Astronomy</name><t
ype uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="69272292"><person 
uuid="820600b4-2644-43b4-a58a-3d64f31a4465"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/820600b4-2644-43b4-a58a-3d64f31a4465?apiKey=b32edef1-c406-4263-ac7d-401354cb28d
d"/><name>Vincent Arnaud Martinez</name></person><name><firstName>Vincent</firstName><lastName>Martinez</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</
personRole><organisationalUnits><organisationalUnit uuid="7d21e6de-e2dc-4598-a62d-410a3c55db98"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/7d21e6de-e2dc-
4598-a62d-410a3c55db98?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Physics and Astronomy</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">Sc
hool</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="69272294"><person uuid="50f876ea-e424-445b-a52e-a0621710dba4"><link ref="content" href="https://
www.pure.ed.ac.uk/ws/api/59/persons/50f876ea-e424-445b-a52e-a0621710dba4?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Angela Dawson</name></person><name><firstName>Angela</firstName><las
tName>Dawson</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="7d21e6de-e2dc-4598-a62d-410a3c55d
b98"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/7d21e6de-e2dc-4598-a62d-410a3c55db98?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Physic
s and Astronomy</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociatio
n id="69272296"><person uuid="476aa07e-1c38-4563-9eda-006f623ca7cc"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/476aa07e-1c38-4563-9eda-006f623ca7cc?apiKey=b32edef1-c4
06-4263-ac7d-401354cb28dd"/><name>Teuta Pilizota</name></person><name><firstName>Teuta</firstName><lastName>Pilizota</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creat
or">Creator</personRole><organisationalUnits><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2
cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation
/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="69272298"><person uuid="c4020821-8012-494b-a828-c0cda81b0409"><link ref="content" hre
f="https://www.pure.ed.ac.uk/ws/api/59/persons/c4020821-8012-494b-a828-c0cda81b0409?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Wilson Poon</name></person><name><firstName>Wilson</first
Name><lastName>Poon</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="7d21e6de-e2dc-4598-a62d-41
0a3c55db98"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/7d21e6de-e2dc-4598-a62d-410a3c55db98?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of
 Physics and Astronomy</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation></personAs
sociations><managingOrganisationalUnit uuid="7d21e6de-e2dc-4598-a62d-410a3c55db98"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/7d21e6de-e2dc-4598-a62d-410
a3c55db98?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Physics and Astronomy</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></
managingOrganisationalUnit><publisher uuid="2ef62159-071c-48cc-b159-eb879377e986"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/publishers/2ef62159-071c-48cc-b159-eb879377e986?a
piKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Edinburgh DataVault</name><type uri="/dk/atira/pure/publisher/publishertypes/publisher/publisher">Publisher</type></publisher><doi>10.7488/138
c6ecb-07b2-426f-81cc-69c197be6c2e</doi><physicalDatas><physicalData id="70385733"><title>Painting with bacteria: Smart templated self assembly using motile bacteria (Raw data)</title><storageLo
cation>Edinburgh DataVault</storageLocation><accessDescription>This dataset is held in the DataVault, directly accessible only to University staff. To request a copy, contact the Depositor, or 
in their absence the Contact Person or Data Manager. Further info: http://www.ed.ac.uk/is/research-support/datavault</accessDescription><media>http://www.ed.ac.uk/information-services/research-
support/research-data-service/sharing-preserving-data/datavault/interim-datavault/retrieve-data</media><type uri="/dk/atira/pure/dataset/documents/dataset">Dataset</type></physicalData></physic
alDatas><contactPerson uuid="2391660e-21ad-43a6-bf18-218efaf32591"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/2391660e-21ad-43a6-bf18-218efaf32591?apiKey=b32edef1-c40
6-4263-ac7d-401354cb28dd"/><name>Jochen Arlt</name></contactPerson><legalConditions><legalCondition id="69272287"><description>Ongoing research project. This dataset is held in the Edinburgh Da
taVault, accessible only to authorised University of Edinburgh staff. To request access to the data please contact the Depositor, or in their absence the Contact Person or Data Manager, named o
n this page. Further information on retrieving data from the DataVault can be found at: http://www.ed.ac.uk/information-services/research-support/research-data-service/sharing-preserving-data/d
atavault/interim-datavault/retrieve-data.</description></legalCondition></legalConditions><organisationalUnits><organisationalUnit uuid="7d21e6de-e2dc-4598-a62d-410a3c55db98"><link ref="content
" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/7d21e6de-e2dc-4598-a62d-410a3c55db98?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Physics and Astronomy</name><
type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content
" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological Sciences</name><ty
pe uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits><externalOrganisations><externalOrganisation uuid="dd495fdf-d6
e4-46e2-a84b-84365b4df311"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-organisations/dd495fdf-d6e4-46e2-a84b-84365b4df311?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"
/><name>European Research Council</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternalorganisationtypes/ueoexternalorganisation/u_government_body">EU Government Body</type></exte
rnalOrganisation><externalOrganisation uuid="44f9e207-09cd-4786-8a9e-fa76e5091609"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-organisations/44f9e207-09cd-4786-8a9e-f
a76e5091609?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>EPSRC</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternalorganisationtypes/ueoexternalorganisation/fundingbody">F
unding body</type></externalOrganisation></externalOrganisations><publicationDate><year>2018</year></publicationDate><openAccessPermission uri="/dk/atira/pure/dataset/accesspermission/restricte
d">Restricted</openAccessPermission><relatedDataSets><relatedDataSets uuid="ef58e906-7aeb-464f-bdf6-e3f81a9eda93"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/datasets/ef58e906
-7aeb-464f-bdf6-e3f81a9eda93?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Painting with bacteria: Smart templated self assembly using motile bacteria</name><type uri="/dk/atira/pure/data
set/datasettypes/dataset/dataset">Dataset</type></relatedDataSets></relatedDataSets><relatedProjects><relatedProjects uuid="6a9313a4-4c45-4bbe-a619-4b0384d1d7e8"><link ref="content" href="https
://www.pure.ed.ac.uk/ws/api/59/projects/6a9313a4-4c45-4bbe-a619-4b0384d1d7e8?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>PHYSAPS: The Physics of Active Particle Suspensions</name><type 
uri="/dk/atira/pure/upmproject/upmprojecttypes/upmproject/research">Research</type></relatedProjects><relatedProjects uuid="6125dcb4-e547-41c3-b1be-2b030edda5e5"><link ref="content" href="https
://www.pure.ed.ac.uk/ws/api/59/projects/6125dcb4-e547-41c3-b1be-2b030edda5e5?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Design Principles for New Soft Materials</name><type uri="/dk/at
ira/pure/upmproject/upmprojecttypes/upmproject/research">Research</type></relatedProjects></relatedProjects><relatedResearchOutputs><relatedResearchOutput uuid="88d0a2ad-8a95-4512-912b-a664347c
e4b9"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/research-outputs/88d0a2ad-8a95-4512-912b-a664347ce4b9?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Painting with light
-powered bacteria</name><type uri="/dk/atira/pure/researchoutput/researchoutputtypes/contributiontojournal/article">Article</type></relatedResearchOutput></relatedResearchOutputs><workflow work
flowStep="validated">Validated</workflow><keywordGroups><keywordGroup logicalName="Dataset Keywords"><type>Dataset Free Keywords</type><keywords><keyword>Soft Condensed Matter</keyword><keyword
>active matter</keyword><keyword>differential dynamic microscopy</keyword><keyword>self assembly</keyword></keywords></keywordGroup></keywordGroups><visibility key="FREE">Public - No restrictio
n</visibility><confidential>false</confidential><info><createdBy>jarlt</createdBy><createdDate>2018-07-30T10:41:04.904+01:00</createdDate><modifiedBy>pward2</modifiedBy><modifiedDate>2018-08-15
T12:48:10.953+01:00</modifiedDate></info></dataSet>' where id = '138c6ecb-07b2-426f-81cc-69c197be6c2e';
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="bcb94418-0ed3-4fd6-9881-3195fbe2deb9" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>DataVa
ult test1</title><type uri="/dk/atira/pure/dataset/datasettypes/dataset/dataset">Dataset</type><description typeUri="/dk/atira/pure/dataset/descriptions/datasetdescription" type="Description">T
esting DataVault deposit</description><description typeUri="/dk/atira/pure/dataset/descriptions/abstract" type="Abstract"/><description typeUri="/dk/atira/pure/dataset/descriptions/reason_for_d
ataset_access_restriction_and_conditions_for_release" type="Data Citation"/><personAssociations><personAssociation id="66178822"><person uuid="5c98aac6-97e3-46d6-b9cb-d2674553a610"><link ref="c
ontent" href="https://www.pure.ed.ac.uk/ws/api/59/persons/5c98aac6-97e3-46d6-b9cb-d2674553a610?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Kerry Miller</name></person><name><firstName>K
erry-Anne</firstName><lastName>Miller</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="b81f0ca1
-1bdf-4b28-a971-2c750a7a951f"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/b81f0ca1-1bdf-4b28-a971-2c750a7a951f?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd
"/><name>Library and University Collections</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/supportgroupdept">Support Group Dept</type></organisationalUnit></organis
ationalUnits></personAssociation><personAssociation id="66178824"><person uuid="82efffd7-302f-4e49-aca1-29ba80a52a1d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/82eff
fd7-302f-4e49-aca1-29ba80a52a1d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Jennifer Daub</name></person><name><firstName>Jennifer</firstName><lastName>Daub</lastName></name><personRole
 uri="/dk/atira/pure/dataset/roles/dataset/owner">Owner</personRole><organisationalUnits><organisationalUnit uuid="b81f0ca1-1bdf-4b28-a971-2c750a7a951f"><link ref="content" href="https://www.pu
re.ed.ac.uk/ws/api/59/organisational-units/b81f0ca1-1bdf-4b28-a971-2c750a7a951f?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Library and University Collections</name><type uri="/dk/atira
/pure/organisation/organisationtypes/organisation/supportgroupdept">Support Group Dept</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="81057321"><per
son uuid="61ed53e7-1242-471b-86e5-014e4da842e2"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/61ed53e7-1242-471b-86e5-014e4da842e2?apiKey=b32edef1-c406-4263-ac7d-401354c
b28dd"/><name>Martin Donnelly</name></person><name><firstName>Martin</firstName><lastName>Donnelly</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</perso
nRole><organisationalUnits><organisationalUnit uuid="b81f0ca1-1bdf-4b28-a971-2c750a7a951f"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/b81f0ca1-1bdf-4b28-
a971-2c750a7a951f?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Library and University Collections</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/supportgroup
dept">Support Group Dept</type></organisationalUnit></organisationalUnits></personAssociation></personAssociations><managingOrganisationalUnit uuid="b81f0ca1-1bdf-4b28-a971-2c750a7a951f"><link 
ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/b81f0ca1-1bdf-4b28-a971-2c750a7a951f?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Library and University Coll
ections</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/supportgroupdept">Support Group Dept</type></managingOrganisationalUnit><publisher uuid="2ef62159-071c-48cc-b
159-eb879377e986"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/publishers/2ef62159-071c-48cc-b159-eb879377e986?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Edinburgh Dat
aVault</name><type uri="/dk/atira/pure/publisher/publishertypes/publisher/publisher">Publisher</type></publisher><contactPerson uuid="5c98aac6-97e3-46d6-b9cb-d2674553a610"><link ref="content" h
ref="https://www.pure.ed.ac.uk/ws/api/59/persons/5c98aac6-97e3-46d6-b9cb-d2674553a610?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Kerry Miller</name></contactPerson><organisationalUnits
><organisationalUnit uuid="b81f0ca1-1bdf-4b28-a971-2c750a7a951f"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/b81f0ca1-1bdf-4b28-a971-2c750a7a951f?apiKey=b
32edef1-c406-4263-ac7d-401354cb28dd"/><name>Library and University Collections</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/supportgroupdept">Support Group Dept</
type></organisationalUnit></organisationalUnits><publicationDate><year>2018</year><month>7</month><day>10</day></publicationDate><workflow workflowStep="validated">Validated</workflow><visibili
ty key="FREE">Public - No restriction</visibility><confidential>false</confidential><info><createdBy>kmiller3</createdBy><createdDate>2018-07-10T11:22:29.338+01:00</createdDate><modifiedBy>pwar
d2</modifiedBy><modifiedDate>2019-03-13T10:26:51.259Z</modifiedDate></info></dataSet>' where id = 'bcb94418-0ed3-4fd6-9881-3195fbe2deb9';
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="fbce59b5-f9ed-42d7-9607-48aaae41cf29" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>A Corp
us of Late Eighteenth-Century Prose</title><type uri="/dk/atira/pure/dataset/datasettypes/dataset/dataset">Dataset</type><description typeUri="/dk/atira/pure/dataset/descriptions/datasetdescrip
tion" type="Description">About 300,000 words of local English letters on practical subjects, dated 1761-90. The transcribed letters were all written to Richard Orford, a steward of Peter Legh t
he Younger at Lyme Hall in Cheshire.</description><description typeUri="/dk/atira/pure/dataset/descriptions/abstract" type="Abstract"/><description typeUri="/dk/atira/pure/dataset/descriptions/
reason_for_dataset_access_restriction_and_conditions_for_release" type="Data Citation"/><personAssociations><personAssociation id="15143569"><externalPerson uuid="379d7b15-890e-4f5f-8c3a-5f0d28
d5a4d6"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-persons/379d7b15-890e-4f5f-8c3a-5f0d28d5a4d6?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>David Denison</na
me><type uri="/dk/atira/pure/externalperson/externalpersontypes/externalperson/externalperson">External person</type></externalPerson><name><firstName>David</firstName><lastName>Denison</lastNa
me></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole></personAssociation><personAssociation id="15143570"><person uuid="25362aac-9c99-402f-8e6e-0a3919fcd
357"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/25362aac-9c99-402f-8e6e-0a3919fcd357?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Linda Van Bergen</name></pers
on><name><firstName>Linda</firstName><lastName>Van Bergen</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationa
lUnit uuid="154b1679-dc85-4087-8e5e-07f3120daca2"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/154b1679-dc85-4087-8e5e-07f3120daca2?apiKey=b32edef1-c406-42
63-ac7d-401354cb28dd"/><name>School of Philosophy, Psychology and Language Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisati
onalUnit></organisationalUnits></personAssociation></personAssociations><managingOrganisationalUnit uuid="154b1679-dc85-4087-8e5e-07f3120daca2"><link ref="content" href="https://www.pure.ed.ac.
uk/ws/api/59/organisational-units/154b1679-dc85-4087-8e5e-07f3120daca2?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Philosophy, Psychology and Language Sciences</name><type uri
="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></managingOrganisationalUnit><publisher uuid="9de4234b-e228-4d1d-b0a7-f468cf65b7a5"><link ref="content" href="h
ttps://www.pure.ed.ac.uk/ws/api/59/publishers/9de4234b-e228-4d1d-b0a7-f468cf65b7a5?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>John Rylands University Library of Manchester</name><type 
uri="/dk/atira/pure/publisher/publishertypes/publisher/publisher">Publisher</type></publisher><organisationalUnits><organisationalUnit uuid="154b1679-dc85-4087-8e5e-07f3120daca2"><link ref="con
tent" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/154b1679-dc85-4087-8e5e-07f3120daca2?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Philosophy, Psychology an
d Language Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits><publicationDate><year>2002</year
></publicationDate><links><link id="15143567"><url>http://personalpages.manchester.ac.uk/staff/david.denison/late18c</url><description>Institutional repository </description></link></links><wor
kflow workflowStep="validated">Validated</workflow><visibility key="FREE">Public - No restriction</visibility><confidential>false</confidential><info><createdBy>mcumming</createdBy><createdDate
>2014-05-06T15:33:17.305+01:00</createdDate><modifiedBy>slewis23</modifiedBy><modifiedDate>2015-08-28T14:34:25.404+01:00</modifiedDate></info></dataSet>' where id = 'fbce59b5-f9ed-42d7-9607-48aaae41cf29';
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="8a96caae-db2a-42c5-9361-e366faa26e3f" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>Algal 
phospho- and protein rhythms mass spec data</title><type uri="/dk/atira/pure/dataset/datasettypes/dataset/dataset">Dataset</type><description typeUri="/dk/atira/pure/dataset/descriptions/datase
tdescription" type="Description">Circadian protein regulation in the green lineage I.&#xd;
A phospho-dawn anticipates light onset before proteins peak in daytime. &#xd;
&#xd;
## Format ##&#xd;
Thermo .raw mass spectrometer files. This file format can be opened using Thermo Fisher Scientific's Xcalibur software. &#xd;
&#xd;
## Access ##&#xd;
This dataset is held in the Edinburgh DataVault, directly accessible only to authorised University of Edinburgh users.  External users may request access to a copy of the data by contacting the
 Principal Investigator, Contact Person or Data Manager named on this page. University of Edinburgh users who wish to have direct access should consult the information about retrieving data fro
m the DataVault at: http://www.ed.ac.uk/is/research-support/datavault . </description><description typeUri="/dk/atira/pure/dataset/descriptions/abstract" type="Abstract">Daily light-dark cycles
 (LD) drive dynamic regulation of plant and algal transcriptomes via photoreceptor pathways and 24-hour, circadian rhythms. Diel regulation of protein levels and modifications has been less stu
died. Ostreococcus tauri, the smallest free-living eukaryote, provides a minimal model proteome for the green lineage. Here, we compare transcriptome data under LD to the algal proteome and pho
sphoproteome, assayed using shotgun mass spectrometry. Under 10% of 855 quantified proteins were rhythmic but two-thirds of 860 phosphoproteins showed rhythmic modification(s). Most rhythmic pr
oteins peaked in the daytime. Model simulations showed that light-stimulated protein synthesis largely accounts for this distribution of protein peaks. Prompted by apparently dark-stable protei
ns, we sampled during prolonged dark adaptation, where stable RNAs and very limited change to the proteome suggested a quiescent, cellular “dark state”. In LD, acid-directed and proline directe
d protein phosphorylation sites were regulated in antiphase. Strikingly, 39% of rhythmic phospho-sites reached peak levels just before dawn. This anticipatory phosphorylation is distinct from l
ight-responsive translation but consistent with plant phosphoprotein profiles, suggesting that a clock-regulated phospho-dawn prepares green cells for daytime functions.</description><descripti
on typeUri="/dk/atira/pure/dataset/descriptions/reason_for_dataset_access_restriction_and_conditions_for_release" type="Data Citation">Millar A et al. "Algal phospho- and protein rhythms" [data
set] University of Edinburgh</description><dataProductionPeriod><startDate><year>2013</year></startDate><endDate><year>2014</year></endDate></dataProductionPeriod><personAssociations><personAss
ociation id="82482800"><person uuid="eb2e0633-6acf-4b79-a0a6-7f23bba22f7e"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/eb2e0633-6acf-4b79-a0a6-7f23bba22f7e?apiKey=b32e
def1-c406-4263-ac7d-401354cb28dd"/><name>Lisa Imrie</name></person><name><firstName>Lisa</firstName><lastName>Imrie</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creato
r">Creator</personRole><organisationalUnits><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2c
bb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/
school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="82482802"><person uuid="86879389-dd37-4d9d-886e-f549ed23f6ba"><link ref="content" href
="https://www.pure.ed.ac.uk/ws/api/59/persons/86879389-dd37-4d9d-886e-f549ed23f6ba?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Andrew Millar</name></person><name><firstName>Andrew</firs
tName><lastName>Millar</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21
-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School
 of Biological Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAs
sociation id="82482804"><externalPerson uuid="5e25c23f-a586-416a-a308-0ceba3613a5e"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-persons/5e25c23f-a586-416a-a308-0ceba3
613a5e?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Zeenat  Noordally</name><type uri="/dk/atira/pure/externalperson/externalpersontypes/externalperson/externalperson">External person</t
ype></externalPerson><name><firstName>Zeenat </firstName><lastName>Noordally</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole></personAssociat
ion><personAssociation id="82482805"><person uuid="a7173777-f189-40c1-bf69-fb307688b5ad"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/a7173777-f189-40c1-bf69-fb307688b5
ad?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Matthew Hindle</name></person><name><firstName>Matthew</firstName><lastName>Hindle</lastName></name><personRole uri="/dk/atira/pure/datase
t/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/o
rganisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisati
on/organisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac.uk/w
s/api/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological Sciences</name><type uri="/dk/atira/pure/organisation
/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="82482806"><person uuid="22bc40cc-5631-407f-82f0-efca86
65cc1b"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/22bc40cc-5631-407f-82f0-efca8665cc1b?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Sarah Martin</name></perso
n><name><firstName>Sarah</firstName><lastName>Martin</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit
 uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac
7d-401354cb28dd"/><name>School of Biological Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits
></personAssociation><personAssociation id="82482807"><person uuid="c9ed8056-b8a4-4b19-b4f4-705efeea7eb5"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/c9ed8056-b8a4-4b1
9-b4f4-705efeea7eb5?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Daniel Seaton</name></person><name><firstName>Daniel</firstName><lastName>Seaton</lastName></name><personRole uri="/dk/at
ira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac
.uk/ws/api/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological Sciences</name><type uri="/dk/atira/pure/organis
ation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="82482808"><person uuid="dfee7091-0d56-4b1e-a821-5
f0f0b37ea02"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/dfee7091-0d56-4b1e-a821-5f0f0b37ea02?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Ian Simpson</name></p
erson><name><firstName>Ian</firstName><lastName>Simpson</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalU
nit uuid="d9a3581f-93a4-4d74-bf29-14c86a1da9f4"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/d9a3581f-93a4-4d74-bf29-14c86a1da9f4?apiKey=b32edef1-c406-4263
-ac7d-401354cb28dd"/><name>School of Informatics</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="
29338e9c-e90d-4e14-9601-5c075828a42b"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/29338e9c-e90d-4e14-9601-5c075828a42b?apiKey=b32edef1-c406-4263-ac7d-4013
54cb28dd"/><name>Edinburgh Neuroscience</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/research_group">Research Group</type></organisationalUnit><organisationalUnit
 uuid="50fb20c4-42f4-46f8-8b8f-59fac5ed3652"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/50fb20c4-42f4-46f8-8b8f-59fac5ed3652?apiKey=b32edef1-c406-4263-ac
7d-401354cb28dd"/><name>Institute for Adaptive and Neural Computation </name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/research_institute">Research Institute</type><
/organisationalUnit></organisationalUnits></personAssociation><personAssociation id="82482809"><person uuid="8d6cd568-b44e-488f-b384-b829110f834e"><link ref="content" href="https://www.pure.ed.
ac.uk/ws/api/59/persons/8d6cd568-b44e-488f-b384-b829110f834e?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Thierry Le Bihan</name></person><name><firstName>Thierry</firstName><lastName>Le
 Bihan</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><
link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological S
ciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="84
472721"><person uuid="eb2e0633-6acf-4b79-a0a6-7f23bba22f7e"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/eb2e0633-6acf-4b79-a0a6-7f23bba22f7e?apiKey=b32edef1-c406-4263-
ac7d-401354cb28dd"/><name>Lisa Imrie</name></person><name><firstName>Lisa</firstName><lastName>Imrie</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/datamanager">Data Man
ager</personRole><organisationalUnits><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2cbb4a36
-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school
">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="87193264"><person uuid="eb2e0633-6acf-4b79-a0a6-7f23bba22f7e"><link ref="content" href="http
s://www.pure.ed.ac.uk/ws/api/59/persons/eb2e0633-6acf-4b79-a0a6-7f23bba22f7e?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Lisa Imrie</name></person><name><firstName>Lisa</firstName><last
Name>Imrie</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/owner">Owner</personRole><organisationalUnits><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><
link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological S
ciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation></personAssociations><man
agingOrganisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKe
y=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></managingOrganisat
ionalUnit><publisher uuid="2ef62159-071c-48cc-b159-eb879377e986"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/publishers/2ef62159-071c-48cc-b159-eb879377e986?apiKey=b32edef1-c4
06-4263-ac7d-401354cb28dd"/><name>Edinburgh DataVault</name><type uri="/dk/atira/pure/publisher/publishertypes/publisher/publisher">Publisher</type></publisher><doi>10.7488/8a96caae-db2a-42c5-9
361-e366faa26e3f</doi><physicalDatas><physicalData id="82482810"><title>Algal phospho- and protein rhythms mass spec data</title><storageLocation>Edinburgh DataVault</storageLocation><accessDes
cription>This dataset is held in the DataVault, directly accessible only to authorised University of Edinburgh staff. To request a copy, contact the Depositor, or the Contact Person or Data Man
ager. Further info: http://www.ed.ac.uk/is/research-support/datavault</accessDescription><media>https://www.ed.ac.uk/is/research-support/datavault</media><type uri="/dk/atira/pure/dataset/docum
ents/dataset">Dataset</type></physicalData></physicalDatas><contactPerson uuid="eb2e0633-6acf-4b79-a0a6-7f23bba22f7e"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/eb2e0
633-6acf-4b79-a0a6-7f23bba22f7e?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Lisa Imrie</name></contactPerson><legalConditions><legalCondition id="84472652"><description>This dataset is 
held in the Edinburgh DataVault, directly accessible only to authorised University of Edinburgh users. External users may request access to a copy of the data by contacting the Depositor, or in
 their absence the Contact Person or Data Manager named on this page. Further information on retrieving data from the DataVault can be found at: "http://www.ed.ac.uk/is/research-support/datavau
lt</description></legalCondition></legalConditions><organisationalUnits><organisationalUnit uuid="2cbb4a36-c06a-4610-9a21-8d6f1f71465d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/ap
i/59/organisational-units/2cbb4a36-c06a-4610-9a21-8d6f1f71465d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Biological Sciences</name><type uri="/dk/atira/pure/organisation/org
anisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/
59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organi
sation/organisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="d9a3581f-93a4-4d74-bf29-14c86a1da9f4"><link ref="content" href="https://www.pure.ed.ac.
uk/ws/api/59/organisational-units/d9a3581f-93a4-4d74-bf29-14c86a1da9f4?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Informatics</name><type uri="/dk/atira/pure/organisation/org
anisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="29338e9c-e90d-4e14-9601-5c075828a42b"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/
59/organisational-units/29338e9c-e90d-4e14-9601-5c075828a42b?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Edinburgh Neuroscience</name><type uri="/dk/atira/pure/organisation/organisation
types/organisation/research_group">Research Group</type></organisationalUnit><organisationalUnit uuid="50fb20c4-42f4-46f8-8b8f-59fac5ed3652"><link ref="content" href="https://www.pure.ed.ac.uk/
ws/api/59/organisational-units/50fb20c4-42f4-46f8-8b8f-59fac5ed3652?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Institute for Adaptive and Neural Computation </name><type uri="/dk/atira
/pure/organisation/organisationtypes/organisation/research_institute">Research Institute</type></organisationalUnit></organisationalUnits><externalOrganisations><externalOrganisation uuid="26ec
28ce-cc10-4c7e-982c-9d3aa0c7b607"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-organisations/26ec28ce-cc10-4c7e-982c-9d3aa0c7b607?apiKey=b32edef1-c406-4263-ac7d-401354
cb28dd"/><name>BBSRC</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternalorganisationtypes/ueoexternalorganisation/fundingbody">Funding body</type></externalOrganisation></extern
alOrganisations><publicationDate><year>2019</year></publicationDate><openAccessPermission uri="/dk/atira/pure/dataset/accesspermission/restricted">Restricted</openAccessPermission><relatedProje
cts><relatedProjects uuid="5e39b8fa-5c6a-4bb1-8692-b59f1dd66867"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/projects/5e39b8fa-5c6a-4bb1-8692-b59f1dd66867?apiKey=b32edef1-c406
-4263-ac7d-401354cb28dd"/><name>Does an Ancient Circadian Clock control transcriptional rhythms using a non transcriptional oscillator</name><type uri="/dk/atira/pure/upmproject/upmprojecttypes
/upmproject/research">Research</type></relatedProjects></relatedProjects><relatedResearchOutputs><relatedResearchOutput uuid="4a2a4013-be32-4b25-ae6f-3039c5968e85"><link ref="content" href="htt
ps://www.pure.ed.ac.uk/ws/api/59/research-outputs/4a2a4013-be32-4b25-ae6f-3039c5968e85?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Circadian protein regulation in the green lineage I. A
 phospho-dawn anticipates light onset before proteins peak in daytime.</name><type uri="/dk/atira/pure/researchoutput/researchoutputtypes/workingpaper/workingpaper">Working paper</type></relate
dResearchOutput></relatedResearchOutputs><links><link id="87193263"><url>https://www.ebi.ac.uk/pride/archive/</url><description>An open access copy of the data is available at the European Bioi
nformatics Institute's PRIDE Archive. </description><linkType uri="/dk/atira/pure/links/dataset/external_data_repository">External Data Repository</linkType></link></links><workflow workflowSte
p="validated">Validated</workflow><visibility key="FREE">Public - No restriction</visibility><confidential>false</confidential><info><createdBy>limrie</createdBy><createdDate>2019-04-16T16:05:4
1.773+01:00</createdDate><modifiedBy>pward2</modifiedBy><modifiedDate>2019-05-06T17:06:26.349+01:00</modifiedDate></info></dataSet>' where id = '8a96caae-db2a-42c5-9361-e366faa26e3f';
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="2716e831-287a-4815-a532-cad72fc8c3ed" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>South 
of Scotland Castles of the 15th Century</title><type uri="/dk/atira/pure/dataset/datasettypes/dataset/dataset">Dataset</type><description typeUri="/dk/atira/pure/dataset/descriptions/datasetdes
cription" type="Description">A file listing spreadsheet contains details of the steps taken in the production of the maps.</description><description typeUri="/dk/atira/pure/dataset/descriptions
/abstract" type="Abstract">This data details the locations of castles in the south of Scotland in the 15th century. The locations were gathered by Prof MacQueen. The data were cleaned and mappe
d by Pauline Ward using ESRI ArcGIS 10.2 with assistance from Stuart Macdonald (both Data Library).</description><description typeUri="/dk/atira/pure/dataset/descriptions/reason_for_dataset_acc
ess_restriction_and_conditions_for_release" type="Data Citation">MacQueen, Hector; Ward, Pauline; Macdonald, Stuart. (2016). South of Scotland Castles of the 15th Century, 1400-1499 [dataset]. 
University of Edinburgh. http://dx.doi.org/10.7488/ds/1392.</description><temporalCoveragePeriod><startDate><year>1800</year></startDate><endDate><year>1800</year></endDate></temporalCoveragePe
riod><geographicalCoverage>South of Scotland</geographicalCoverage><personAssociations><personAssociation id="27569289"><person uuid="971efbb7-1842-4fb8-91fe-3cdfdca218ea"><link ref="content" h
ref="https://www.pure.ed.ac.uk/ws/api/59/persons/971efbb7-1842-4fb8-91fe-3cdfdca218ea?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Hector MacQueen</name></person><name><firstName>Hector<
/firstName><lastName>MacQueen</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="d9ce722a-8dc9-4f
b7-8dff-aa0ba73958a2"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/d9ce722a-8dc9-4fb7-8dff-aa0ba73958a2?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name
>School of Law</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation
 id="27569291"><person uuid="d630b059-17a5-41cc-a3b3-51469e0898d6"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/d630b059-17a5-41cc-a3b3-51469e0898d6?apiKey=b32edef1-c40
6-4263-ac7d-401354cb28dd"/><name>Pauline Ward</name></person><name><firstName>Pauline</firstName><lastName>Ward</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">C
reator</personRole><organisationalUnits><organisationalUnit uuid="fae8c723-f45e-42df-af1a-d2b59f02f9fc"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/fae8c7
23-f45e-42df-af1a-d2b59f02f9fc?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>EDINA</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/supportgroupdept">Support Gr
oup Dept</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="27569293"><person uuid="2e97e8c1-0ee7-4368-b8bf-fa68966df8c3"><link ref="content" href="http
s://www.pure.ed.ac.uk/ws/api/59/persons/2e97e8c1-0ee7-4368-b8bf-fa68966df8c3?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Stuart Macdonald</name></person><name><firstName>Stuart</firstNa
me><lastName>Macdonald</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="fae8c723-f45e-42df-af1a
-d2b59f02f9fc"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/fae8c723-f45e-42df-af1a-d2b59f02f9fc?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>EDINA<
/name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/supportgroupdept">Support Group Dept</type></organisationalUnit></organisationalUnits></personAssociation></personAss
ociations><managingOrganisationalUnit uuid="d9ce722a-8dc9-4fb7-8dff-aa0ba73958a2"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/d9ce722a-8dc9-4fb7-8dff-aa0b
a73958a2?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Law</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></managingOrganisatio
nalUnit><publisher uuid="1f0e03f8-11e9-4f1f-a853-6c86144f1015"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/publishers/1f0e03f8-11e9-4f1f-a853-6c86144f1015?apiKey=b32edef1-c406
-4263-ac7d-401354cb28dd"/><name>Edinburgh DataShare</name><type uri="/dk/atira/pure/publisher/publishertypes/publisher/publisher">Publisher</type></publisher><doi>10.7488/ds/1392</doi><organisa
tionalUnits><organisationalUnit uuid="d9ce722a-8dc9-4fb7-8dff-aa0ba73958a2"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/d9ce722a-8dc9-4fb7-8dff-aa0ba73958
a2?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Law</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit><organ
isationalUnit uuid="fae8c723-f45e-42df-af1a-d2b59f02f9fc"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/fae8c723-f45e-42df-af1a-d2b59f02f9fc?apiKey=b32edef1
-c406-4263-ac7d-401354cb28dd"/><name>EDINA</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/supportgroupdept">Support Group Dept</type></organisationalUnit></organisa
tionalUnits><externalOrganisations><externalOrganisation uuid="927a48ec-70b9-4290-9af8-57a1faafe584"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-organisations/927a48e
c-70b9-4290-9af8-57a1faafe584?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>University of  Edinburgh</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternalorganisationtypes/u
eoexternalorganisation/uk_university_hei">UK University / HEI</type></externalOrganisation></externalOrganisations><publicationDate><year>2016</year><month>4</month><day>27</day></publicationDa
te><openAccessPermission uri="/dk/atira/pure/dataset/accesspermission/open">Open</openAccessPermission><workflow workflowStep="validated">Validated</workflow><keywordGroups><keywordGroup logica
lName="Dataset Keywords"><type>Dataset Free Keywords</type><keywords><keyword>history</keyword><keyword>castles</keyword></keywords></keywordGroup></keywordGroups><visibility key="FREE">Public 
- No restriction</visibility><confidential>false</confidential><info><createdBy>pward2</createdBy><createdDate>2016-08-31T10:09:42.109+01:00</createdDate><modifiedBy>estoica</modifiedBy><modifi
edDate>2018-02-12T18:27:05.123Z</modifiedDate></info></dataSet>' where id = '2716e831-287a-4815-a532-cad72fc8c3ed';
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="499f2a33-ac66-4ad8-b1d4-03e7414a78fe" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>Roslin
 Institute archival data from former staff etc</title><type uri="/dk/atira/pure/dataset/datasettypes/dataset/dataset">Dataset</type><description typeUri="/dk/atira/pure/dataset/descriptions/dat
asetdescription" type="Description">Various datasets historically left by former staff and/or students  of the Institute which require to be archived to comply with funder requirements. &#xd;
&#xd;
## Access ##&#xd;
This dataset is held in the Edinburgh DataVault, directly accessible only to authorised University of Edinburgh users. </description><description typeUri="/dk/atira/pure/dataset/descriptions/ab
stract" type="Abstract"/><description typeUri="/dk/atira/pure/dataset/descriptions/reason_for_dataset_access_restriction_and_conditions_for_release" type="Data Citation"/><dataProductionPeriod>
<startDate><year>2016</year></startDate><endDate><year>2016</year></endDate></dataProductionPeriod><personAssociations><personAssociation id="81930568"><person uuid="af9b1a03-6ecc-4a54-b4ab-eac
3d13e4dc8"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/af9b1a03-6ecc-4a54-b4ab-eac3d13e4dc8?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Pete Kaiser</name></per
son><name><firstName>Peter</firstName><lastName>Kaiser</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUn
it uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-
ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organ
isationalUnits></personAssociation><personAssociation id="81930570"><person uuid="0b08ec8b-f1ca-4dc0-8228-4342450290b4"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/0b0
8ec8b-f1ca-4dc0-8228-4342450290b4?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Colin Simpson</name></person><name><firstName>Colin</firstName><lastName>Simpson</lastName></name><personRo
le uri="/dk/atira/pure/dataset/roles/dataset/data_manager">Data Manager</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href
="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</na
me><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="81930572"><pe
rson uuid="b29afa01-d83c-44ef-b7ed-4bf987d4c45a"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/b29afa01-d83c-44ef-b7ed-4bf987d4c45a?apiKey=b32edef1-c406-4263-ac7d-401354
cb28dd"/><name>Stephen Bishop</name></person><name><firstName>Stephen</firstName><lastName>Bishop</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</person
Role><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9
986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school
">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="81930574"><person uuid="074c276c-942f-4d7d-86a2-9dba45b6dbec"><link ref="content" href="http
s://www.pure.ed.ac.uk/ws/api/59/persons/074c276c-942f-4d7d-86a2-9dba45b6dbec?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Peter Simmonds</name></person><name><firstName>Peter</firstName>
<lastName>Simmonds</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-055
59db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dic
k) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><
personAssociation id="81930576"><person uuid="c3af01db-7028-4d59-bf2f-52ecef571c6f"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/c3af01db-7028-4d59-bf2f-52ecef571c6f?ap
iKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Elizabeth Glass</name></person><name><firstName>Elizabeth</firstName><lastName>Glass</lastName></name><personRole uri="/dk/atira/pure/dataset/r
oles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/orga
nisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/
organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="81930578"><person uuid="a43e6253-0cda-4079-a92a-02bedcb
90c4d"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/a43e6253-0cda-4079-a92a-02bedcb90c4d?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Colin Sharp</name></person>
<name><firstName>Colin</firstName><lastName>Sharp</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uu
id="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-
401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisati
onalUnits></personAssociation><personAssociation id="81930580"><person uuid="34e196ad-b5e9-4eab-be6a-1c42e6bd396c"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/34e196ad
-b5e9-4eab-be6a-1c42e6bd396c?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>John Hopkins</name></person><name><firstName>John</firstName><lastName>Hopkins</lastName></name><personRole uri=
"/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pur
e.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk
/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="81930582"><person uuid="432c59
b7-fde0-46f0-9615-b637faca3e2e"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/432c59b7-fde0-46f0-9615-b637faca3e2e?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Ar
vind Mahajan</name></person><name><firstName>Arvind</firstName><lastName>Kumar</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationa
lUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?ap
iKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></or
ganisationalUnit></organisationalUnits></personAssociation><personAssociation id="84670881"><person uuid="99bb0b0a-ce0a-4bb7-bed1-d51f92b4ccda"><link ref="content" href="https://www.pure.ed.ac.
uk/ws/api/59/persons/99bb0b0a-ce0a-4bb7-bed1-d51f92b4ccda?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Tahar Ait-Ali</name></person><name><firstName>Tahar</firstName><lastName>Ait-Ali</l
astName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref=
"content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterina
ry Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id=
"85690231"><person uuid="2efca7eb-7f9f-4795-958d-199069d08bba"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/2efca7eb-7f9f-4795-958d-199069d08bba?apiKey=b32edef1-c406-42
63-ac7d-401354cb28dd"/><name>Wilfred Goldmann</name></person><name><firstName>Wilfred</firstName><lastName>Goldmann</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creato
r">Creator</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/56
4daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/o
rganisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="85690233"><person uuid="fab54358-d843-4fb1-b166-2ef68858c385"><link ref="c
ontent" href="https://www.pure.ed.ac.uk/ws/api/59/persons/fab54358-d843-4fb1-b166-2ef68858c385?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Nora Hunter</name></person><name><firstName>No
ra</firstName><lastName>Hunter</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-4
02e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><nam
e>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personA
ssociation><personAssociation id="85690235"><person uuid="fca5cb60-7bf8-4ac4-a6e1-f8c170025c40"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/fca5cb60-7bf8-4ac4-a6e1-f8c
170025c40?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Gerry McLachlan</name></person><name><firstName>Gerry</firstName><lastName>McLachlan</lastName></name><personRole uri="/dk/atira/pu
re/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws
/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/o
rganisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="85702286"><person uuid="d328a3a1-938c-402f-
946d-c1e53c68efc2"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/d328a3a1-938c-402f-946d-c1e53c68efc2?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Mick Watson</na
me></person><name><firstName>Michael</firstName><lastName>Watson</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organi
sationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-
c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUn
it></organisationalUnits></personAssociation></personAssociations><managingOrganisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/
api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/or
ganisation/organisationtypes/organisation/school">School</type></managingOrganisationalUnit><publisher uuid="2ef62159-071c-48cc-b159-eb879377e986"><link ref="content" href="https://www.pure.ed.
ac.uk/ws/api/59/publishers/2ef62159-071c-48cc-b159-eb879377e986?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Edinburgh DataVault</name><type uri="/dk/atira/pure/publisher/publishertypes/
publisher/publisher">Publisher</type></publisher><physicalDatas><physicalData id="84249310"><title>Roslin Institute archival data from former staff etc</title><storageLocation>Edinburgh DataVau
lt</storageLocation><accessDescription>This dataset is held in the DataVault, directly accessible only to authorised University of Edinburgh staff. To request a copy, contacor Data Manager. Fur
ther info:</accessDescription><media>https://www.ed.ac.uk/is/research-support/datavault</media><type uri="/dk/atira/pure/dataset/documents/dataset">Dataset</type></physicalData></physicalDatas>
<contactPerson uuid="0b08ec8b-f1ca-4dc0-8228-4342450290b4"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/0b08ec8b-f1ca-4dc0-8228-4342450290b4?apiKey=b32edef1-c406-4263-a
c7d-401354cb28dd"/><name>Colin Simpson</name></contactPerson><legalConditions><legalCondition id="84249309"><description>Requests for access will not necessarily be granted. This dataset is hel
d in the Edinburgh DataVault, directly accessible only to authorised University of Edinburgh users. External users may request access to a copy of the data by contacting the Contact Person name
d on this page. Further information on retrieving data from the DataVault can be found at: "http://www.ed.ac.uk/is/research-support/datavault</description></legalCondition></legalConditions><or
ganisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-0555
9db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School
</type></organisationalUnit></organisationalUnits><externalOrganisations><externalOrganisation uuid="26ec28ce-cc10-4c7e-982c-9d3aa0c7b607"><link ref="content" href="https://www.pure.ed.ac.uk/ws
/api/59/external-organisations/26ec28ce-cc10-4c7e-982c-9d3aa0c7b607?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>BBSRC</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternal
organisationtypes/ueoexternalorganisation/fundingbody">Funding body</type></externalOrganisation></externalOrganisations><publicationDate><year>2019</year></publicationDate><openAccessPermissio
n uri="/dk/atira/pure/dataset/accesspermission/restricted">Restricted</openAccessPermission><relatedProjects><relatedProjects uuid="a117ab63-0d49-44b6-b4ae-ecbe365a0986"><link ref="content" hre
f="https://www.pure.ed.ac.uk/ws/api/59/projects/a117ab63-0d49-44b6-b4ae-ecbe365a0986?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Analysis and prediction in complex animal systems</name>
<type uri="/dk/atira/pure/upmproject/upmprojecttypes/upmproject/research">Research</type></relatedProjects><relatedProjects uuid="c1aaf755-8557-47ca-8dd5-0697e48f9f14"><link ref="content" href=
"https://www.pure.ed.ac.uk/ws/api/59/projects/c1aaf755-8557-47ca-8dd5-0697e48f9f14?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Innate immunity and endemic diseases in livestock species<
/name><type uri="/dk/atira/pure/upmproject/upmprojecttypes/upmproject/research">Research</type></relatedProjects></relatedProjects><workflow workflowStep="validated">Validated</workflow><visibi
lity key="FREE">Public - No restriction</visibility><confidential>false</confidential><info><createdBy>csimpso3</createdBy><createdDate>2019-04-03T11:57:06.538+01:00</createdDate><modifiedBy>pw
ard2</modifiedBy><modifiedDate>2019-05-01T11:34:47.786+01:00</modifiedDate></info></dataSet>' where id = '499f2a33-ac66-4ad8-b1d4-03e7414a78fe';
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="7d6ce345-9a6c-4330-9b16-9d21f2f550bc" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>Roslin
 Bio-Imaging Archive</title><type uri="/dk/atira/pure/dataset/datasettypes/dataset/dataset">Dataset</type><description typeUri="/dk/atira/pure/dataset/descriptions/datasetdescription" type="Des
cription">An Archive of scientific images collated from the Roslin Institute Bio-Imaging share. &#xd;
These images were collated by users that have subsequently left the institute or have not made their data sets fully identifiable. &#xd;
&#xd;
These Images were generated using a variety of Brightfield, Fluorescent and Laser Confocal microscopes within Roslin Institute Bioimaging Facility and are stored in various digital image format
s including (but not limited to): &#xd;
.czi (Carl Zeiss)&#xd;
.lif (Leica Image Format) &#xd;
.ids/ics (Nikon)&#xd;
.ome.tif (Image 5D stack)&#xd;
.tiff, .tif, .png, .jpg&#xd;
&#xd;
The archive will be split into sections with folders containing names from A-E, F-K, L-R, S-Z.&#xd;
&#xd;
## Access ##&#xd;
This dataset is held in the Edinburgh DataVault, directly accessible only to authorised University of Edinburgh users. Requests for access will not necessarily be granted. External users may re
quest access to a copy of the data by contacting the Contact Person named on this page. University of Edinburgh users who wish to have direct access should consult the information about retriev
ing data from the DataVault at: http://www.ed.ac.uk/is/research-support/datavault . &#xd;
</description><description typeUri="/dk/atira/pure/dataset/descriptions/abstract" type="Abstract">An Archive of scientific images collated from the Roslin Institute Bio-Imaging share.&#xd;
</description><description typeUri="/dk/atira/pure/dataset/descriptions/reason_for_dataset_access_restriction_and_conditions_for_release" type="Data Citation"/><dataProductionPeriod><startDate>
<year>2010</year></startDate><endDate><year>2018</year></endDate></dataProductionPeriod><personAssociations><personAssociation id="75726160"><person uuid="9eb2fdf4-fc01-45c5-887f-3a71870e4fee">
<link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/9eb2fdf4-fc01-45c5-887f-3a71870e4fee?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Robert Fleming</name></person><nam
e><firstName>Robert</firstName><lastName>Fleming</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/owner">Owner</personRole><organisationalUnits><organisationalUnit uuid="5
64daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-40135
4cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit><organisationalUn
it uuid="33cb0404-f599-4803-ab16-ac7f5e3a7997"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/33cb0404-f599-4803-ab16-ac7f5e3a7997?apiKey=b32edef1-c406-4263-
ac7d-401354cb28dd"/><name>Edinburgh Imaging </name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/research_centre">Research Centre</type></organisationalUnit></organisati
onalUnits></personAssociation><personAssociation id="75726163"><person uuid="9ff04cc7-7e88-47aa-aaee-122b73f548e5"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/9ff04cc7
-7e88-47aa-aaee-122b73f548e5?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Graeme Robertson</name></person><name><firstName>Graeme</firstName><lastName>Robertson</lastName></name><personR
ole uri="/dk/atira/pure/dataset/roles/dataset/owner">Owner</personRole><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www
.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri=
"/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits></personAssociation><personAssociation id="75726165"><person uuid="0b
08ec8b-f1ca-4dc0-8228-4342450290b4"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/0b08ec8b-f1ca-4dc0-8228-4342450290b4?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><nam
e>Colin Simpson</name></person><name><firstName>Colin</firstName><lastName>Simpson</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/datamanager">Data Manager</personRole><
organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05
559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">Scho
ol</type></organisationalUnit></organisationalUnits></personAssociation></personAssociations><managingOrganisationalUnit uuid="564daede-9678-402e-9986-05559db55950"><link ref="content" href="ht
tps://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) School of Veterinary Studies</name><
type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></managingOrganisationalUnit><publisher uuid="2ef62159-071c-48cc-b159-eb879377e986"><link ref="content"
 href="https://www.pure.ed.ac.uk/ws/api/59/publishers/2ef62159-071c-48cc-b159-eb879377e986?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Edinburgh DataVault</name><type uri="/dk/atira/pur
e/publisher/publishertypes/publisher/publisher">Publisher</type></publisher><physicalDatas><physicalData id="80828608"><title>Roslin Bio-Imaging Archive</title><storageLocation>Edinburgh DataVa
ult</storageLocation><accessDescription>This dataset is held in the DataVault, directly accessible only to authorised University of Edinburgh staff. To request a copy, contact the Depositor, or
 the Contact Person or Data Manager. Further info: http://www.ed.ac.uk/is/research-support/datavault</accessDescription><media>https://www.ed.ac.uk/is/research-support/datavault</media><type ur
i="/dk/atira/pure/dataset/documents/image">Image</type></physicalData></physicalDatas><contactPerson uuid="9eb2fdf4-fc01-45c5-887f-3a71870e4fee"><link ref="content" href="https://www.pure.ed.ac
.uk/ws/api/59/persons/9eb2fdf4-fc01-45c5-887f-3a71870e4fee?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Robert Fleming</name></contactPerson><legalConditions><legalCondition id="80828185
"/><legalCondition id="80828347"><description>This dataset is held in the Edinburgh DataVault, directly accessible only to authorised University of Edinburgh staff. Requests for access will not
 necessarily be granted. External users may request access to a copy of the data by contacting the Contact Person named on this page. Further information on retrieving data from the DataVault c
an be found at: http://www.ed.ac.uk/is/research-support/datavault .</description></legalCondition></legalConditions><organisationalUnits><organisationalUnit uuid="564daede-9678-402e-9986-05559d
b55950"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/564daede-9678-402e-9986-05559db55950?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Royal (Dick) 
School of Veterinary Studies</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit><organisationalUnit uuid="33cb0404-f599-4803-a
b16-ac7f5e3a7997"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/33cb0404-f599-4803-ab16-ac7f5e3a7997?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Edi
nburgh Imaging </name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/research_centre">Research Centre</type></organisationalUnit></organisationalUnits><externalOrganisati
ons><externalOrganisation uuid="26ec28ce-cc10-4c7e-982c-9d3aa0c7b607"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-organisations/26ec28ce-cc10-4c7e-982c-9d3aa0c7b607?a
piKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>BBSRC</name><type uri="/dk/atira/pure/ueoexternalorganisation/ueoexternalorganisationtypes/ueoexternalorganisation/fundingbody">Funding body</
type></externalOrganisation></externalOrganisations><publicationDate><year>2019</year></publicationDate><openAccessPermission uri="/dk/atira/pure/dataset/accesspermission/restricted">Restricted
</openAccessPermission><relatedProjects><relatedProjects uuid="67b13a31-bc0a-40da-b57d-646e01095ee5"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/projects/67b13a31-bc0a-40da-b5
7d-646e01095ee5?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>ISP 2017/22-Salaries-Programme 3</name><type uri="/dk/atira/pure/upmproject/upmprojecttypes/upmproject/research">Research</ty
pe></relatedProjects><relatedProjects uuid="46087e85-8ce3-4ede-ba47-543971f67d86"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/projects/46087e85-8ce3-4ede-ba47-543971f67d86?api
Key=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>ISP 2017/22-Salaries-Programme 2</name><type uri="/dk/atira/pure/upmproject/upmprojecttypes/upmproject/research">Research</type></relatedProject
s><relatedProjects uuid="60ac1c7d-581b-4d6a-9fc6-44a2e086596c"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/projects/60ac1c7d-581b-4d6a-9fc6-44a2e086596c?apiKey=b32edef1-c406-4
263-ac7d-401354cb28dd"/><name>ISP 2017/22-Salaries-Programme 1</name><type uri="/dk/atira/pure/upmproject/upmprojecttypes/upmproject/research">Research</type></relatedProjects></relatedProjects
><workflow workflowStep="validated">Validated</workflow><visibility key="FREE">Public - No restriction</visibility><confidential>false</confidential><info><createdBy>csimpso3</createdBy><create
dDate>2018-10-02T14:38:33.520+01:00</createdDate><modifiedBy>pward2</modifiedBy><modifiedDate>2019-03-08T15:45:05.635Z</modifiedDate></info></dataSet>' where id = '7d6ce345-9a6c-4330-9b16-9d21f2f550bc';
update Datasets set content = '<dataSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" uuid="fbce59b5-f9ed-42d7-9607-48aaae41cf29" xsi:schemaLocation="https://www.pure.ed.ac.uk/ws/api/59/xsd/schema1.xsd"><title>A Corp
us of Late Eighteenth-Century Prose</title><type uri="/dk/atira/pure/dataset/datasettypes/dataset/dataset">Dataset</type><description typeUri="/dk/atira/pure/dataset/descriptions/datasetdescrip
tion" type="Description">About 300,000 words of local English letters on practical subjects, dated 1761-90. The transcribed letters were all written to Richard Orford, a steward of Peter Legh t
he Younger at Lyme Hall in Cheshire.</description><description typeUri="/dk/atira/pure/dataset/descriptions/abstract" type="Abstract"/><description typeUri="/dk/atira/pure/dataset/descriptions/
reason_for_dataset_access_restriction_and_conditions_for_release" type="Data Citation"/><personAssociations><personAssociation id="15143569"><externalPerson uuid="379d7b15-890e-4f5f-8c3a-5f0d28
d5a4d6"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/external-persons/379d7b15-890e-4f5f-8c3a-5f0d28d5a4d6?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>David Denison</na
me><type uri="/dk/atira/pure/externalperson/externalpersontypes/externalperson/externalperson">External person</type></externalPerson><name><firstName>David</firstName><lastName>Denison</lastNa
me></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole></personAssociation><personAssociation id="15143570"><person uuid="25362aac-9c99-402f-8e6e-0a3919fcd
357"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/persons/25362aac-9c99-402f-8e6e-0a3919fcd357?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>Linda Van Bergen</name></pers
on><name><firstName>Linda</firstName><lastName>Van Bergen</lastName></name><personRole uri="/dk/atira/pure/dataset/roles/dataset/creator">Creator</personRole><organisationalUnits><organisationa
lUnit uuid="154b1679-dc85-4087-8e5e-07f3120daca2"><link ref="content" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/154b1679-dc85-4087-8e5e-07f3120daca2?apiKey=b32edef1-c406-42
63-ac7d-401354cb28dd"/><name>School of Philosophy, Psychology and Language Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisati
onalUnit></organisationalUnits></personAssociation></personAssociations><managingOrganisationalUnit uuid="154b1679-dc85-4087-8e5e-07f3120daca2"><link ref="content" href="https://www.pure.ed.ac.
uk/ws/api/59/organisational-units/154b1679-dc85-4087-8e5e-07f3120daca2?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Philosophy, Psychology and Language Sciences</name><type uri
="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></managingOrganisationalUnit><publisher uuid="9de4234b-e228-4d1d-b0a7-f468cf65b7a5"><link ref="content" href="h
ttps://www.pure.ed.ac.uk/ws/api/59/publishers/9de4234b-e228-4d1d-b0a7-f468cf65b7a5?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>John Rylands University Library of Manchester</name><type 
uri="/dk/atira/pure/publisher/publishertypes/publisher/publisher">Publisher</type></publisher><organisationalUnits><organisationalUnit uuid="154b1679-dc85-4087-8e5e-07f3120daca2"><link ref="con
tent" href="https://www.pure.ed.ac.uk/ws/api/59/organisational-units/154b1679-dc85-4087-8e5e-07f3120daca2?apiKey=b32edef1-c406-4263-ac7d-401354cb28dd"/><name>School of Philosophy, Psychology an
d Language Sciences</name><type uri="/dk/atira/pure/organisation/organisationtypes/organisation/school">School</type></organisationalUnit></organisationalUnits><publicationDate><year>2002</year
></publicationDate><links><link id="15143567"><url>http://personalpages.manchester.ac.uk/staff/david.denison/late18c</url><description>Institutional repository </description></link></links><wor
kflow workflowStep="validated">Validated</workflow><visibility key="FREE">Public - No restriction</visibility><confidential>false</confidential><info><createdBy>mcumming</createdBy><createdDate
>2014-05-06T15:33:17.305+01:00</createdDate><modifiedBy>slewis23</modifiedBy><modifiedDate>2015-08-28T14:34:25.404+01:00</modifiedDate></info></dataSet>' where id = 'fbce59b5-f9ed-42d7-9607-48aaae41cf29';


-- add non nullable constraint to new column
ALTER TABLE Datasets
CHANGE Contents Contents longtext NOT NULL;

