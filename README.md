# prim-brukerstyring
En av 5 backend applikasjoner for PRIM. PRIM er et lederverktøy og en plattform for rekrutering og intern mobilitet i NAV. Denne applikasjonen støtter opp administrasjon og brukerstyring for PRIM.

## Sett opp lokal Postgres DB
Last ned postgres på din maskin og sett opp databasen med
```bash
createdb -h localhost -p 5432 -U postgres brukerstyring
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
nais postgres prepare --all-privs prim-brukerstyring
nais postgres grant prim-brukerstyring
```
Start proxy-server:
```bash
nais postgres proxy prim-brukerstyring
```

Denne kan kobles på med bruker og passord som er lagret lokalt på klusteret og du finner med:
```bash
kubectl exec -it prim-brukerstyring-XXXXXXX -c prim-brukerstyring -- env
```
pod-nummeret XXXXXXX kan du finne med:
```bash
kubectl get pods
```
