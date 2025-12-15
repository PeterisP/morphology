Bibliotēkas laidiena sagatavošana
=================================

Paredzēts reizi ceturksnī pēc tezaurs.lv ceturkšņa laidiena datu iesaldēšanas sapakot arī Maven Central (https://central.sonatype.com/artifact/lv.ailab.morphology/morphology ) šīs pakas atjauninātu versiju

Nepieciešamie datu avoti:
--
Jaunākie dati no tēzaura (ir pateikts, ka darba versijas dati ir gatavi, un ir piekļuve tai datu bāzei)

Jaunākie Treebank / anotēto korpusu dati (https://github.com/LUMII-AILab/Treebank) 

Vārdnīcas datu atjaunināšana
----

Vajag rīku https://github.com/PeterisP/tezaurs_dump , kuram db_config.py jānorāda strādājoša piekļuve tēzaura darba versijas Postgresql datubāzei.

Rīks sagatavos `tezaurs_lexemes.json` (vai arī `tezaurs_latgalian.json` ja skriptu laidīs ar parametru latgalian), kas jāieliek morfoloģijas pakā zem `src/main/resources/`

Vajadzētu arī atjaunināt `Statistics.xml` failu. To no vārdnīcas un korpusa datiem (kuru atjaunināšana ir zemāk) uzģenerē skripts `CorpusProcessing.java`, tas to uzģenerēs zem projekta pamatmapes, lai to atjauninātu, tas arī jāieliek zem `src/main/resources/`

Korpusa datu atjaunināšana
---

Vajag skriptus no https://github.com/LUMII-AILab/CorporaTools
Vajag strādājošu Perl vidi 
uz Linux:
	```
		sudo apt-get install libxml-libxslt-perl
		sudo cpan -i Treex::PML
	```
uz OS X:
	```
		curl -L https://install.perlbrew.pl | bash   
		source ~/perl5/perlbrew/etc/bashrc
		perlbrew install perl-5.16.0   
		perlbrew switch perl-5.16.0
		sudo cpan -i XML::LibXSLT
		sudo cpan -i Treex::PML
	```
Uz Windows:
	???

Palaižam `PmlCorporaTools/preparePOSTagData.sh`, kam vajadzētu sakopēt jaunākos datus zem `../morphology/src/main/resources/`

Versijas atjaunināšana
-----

Izlaiž testus un paskatās vai `MorphoEvaluate` rezultātos nav būtisku procentu kritumu un rupju kļūdu:
- TagSetTest
- MorphologyTest
- LatgalianTest
- MorphoEvaluate

pom.xml jāatjaunina laidiena versija, un ar `mvn clean deploy` tas varētu nonākt maven central, ja ir visi priekšnosacījumi
- pareizi piekļuves kredenciāļi ~/.m2/settings.xml
- nokonfigurēta gpg atslēga pakas parakstīšanai

Nākamie soļi pēc morfoloģijas versijas atjaunināšanas
----

LVTager blokā (https://github.com/PeterisP/LVTagger/)
	jāapdeito pom.xml dependenciji
	mvn clean install lai paņem svaigākos dependencijus
	Pozitīvi var būt arī apmācīt jaunu produkcijas modeli tagerim `./morpho_train.sh -production` kas uz 2025. MacbookPro iet 45min
	Ciparus pieraksta MorphoCRF/morfoCRFeksperimenti.txt
	produkcijas modeļus pako atsevišķā maven pakā, kas ir mapītē `morphomodel`
	
Webservisu blokā (https://github.com/LUMII-AILab/Webservices)
	jāapdeito pom.xml dependenciji
	jāpako maven uz maven central
	jāuzkopē uz `api.tezaurs.lv`
	`sudo service tezaurs-api restart`


