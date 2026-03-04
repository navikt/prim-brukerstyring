# prim-brukerstyring
En av 5 backend applikasjoner for PRIM. PRIM er et lederverktøy og en plattform for rekrutering og intern mobilitet i NAV. Denne applikasjonen støtter opp administrasjon og brukerstyring for PRIM.

## Oppsett for pre-commit trigger
Sett opp slik at pre-commit trigger kjøres lokalt på din maskin ved commit for å søke etter secrets, credentials og personinfo i endringer som sjekkes inn.

### Installering
Installer pre-commit på maskinen (trenger kun å kjøres én gang) (https://pre-commit.com/#install)
```shell
pip install pre-commit
```
Installer GitLeaks på maskinen (trenger kun å kjøres én gang) (https://github.com/gitleaks/gitleaks)
Eksempel:
```shell
brew install gitleaks
```
#### Installasjon med nix
```shell
nix profile add nixpkgs#pre-commit
nix profile add nixpkgs#gitleaks
```
### Verifiser installering
```shell
pre-commit --version
gitleaks version
```

### Aktivere pre-commit i prosjektet
Installer pre-commit hooks i github-prosjektet (trenger kun å kjøres én gang per prosjekt)
```shell
pre-commit install
```
Nå skal GitLeaks kjøre på alle endringer som forsøkes å commit'es. Finner den noe mistenkelig vil den stoppe commit'en og vise hva som er funnet.

Commit output skal vise noe slikt som dette:

    Detect hardcoded secrets using Gitleaks..................................Passed

## Sett opp lokal Postgres DB
Last ned postgres på din maskin og sett opp databasen med
```bash
createdb -h localhost -p 5432 brukerstyring
```

Det må også opprettes roller i postgres for at applikasjonen skal kunne starte lokalt:
```bash
CREATE ROLE postgres LOGIN;
CREATE ROLE cloudsqliamuser LOGIN;
```

## Starte lokalt fra IntelliJ
Editer konfigurasjonen:
- Velg: "Run" -> "Edit configurations"
- Legg til en ny konfigurasjon (+) av typen "Spring Boot" og navngi den, f.eks "PrimBrukerstyringApplicationLocal"
- Sett "Main class" til "no.nav.pim.primbrukerstyring.PrimBrukerstyringApplicationLocal"

Kjør konfigurasjonen "BemanningsbehovApplicationLocal"

## Kjøre i sky
All bygging i sky styres av Github, som styres av scriptet .github/workflows/maven.yml

- **TEST** blir pushet til ved alle commits

- **PREPROD** blir pushet til ved commits til branchen master

- **PROD** blir pushet til ved commits til branchen prod-deploy

Verdier for de forskjellige miljøene finner du i mappen /.nais

Applikasjonene kan også overvåkes i https://console.nav.cloud.nais.io/

Mer detaljert kontroll over loggene kan du finne på https://logs.adeo.no/

## Tilgang til DB i sky
Følg [denne guiden](https://docs.nais.io/how-to-guides/command-line-access/setup/) for å sette opp alt som trengs av verktøy for å jobbe med sky-databasene på din maskin.

Aktiver naisdevice på din maskin og logg inn på gcloud
```bash
gcloud auth login --update-adc
```
Dersom du skal nå testmiljø endrer du påfølgende navn til _prim-brukerstyring-test_

Gjør klar tilganger:
```bash
nais postgres prepare prim-brukerstyring
nais postgres grant prim-brukerstyring
```
Start proxy-server:
```bash
nais postgres proxy prim-brukerstyring
```
