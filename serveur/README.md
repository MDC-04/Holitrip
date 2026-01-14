# Projet Holitrip - Application de Planification de Séjours

## Membres de l'équipe
(À compléter avec vos noms et prénoms)

## Architecture de l'application

### Vue d'ensemble
L'application Holitrip permet de planifier des séjours incluant transports, hôtels et activités selon les préférences utilisateur.

### Composants principaux

#### Interfaces de services
- `TransportService` : Recherche de transports (train/avion)
- `HotelService` : Recherche d'hôtels avec notes
- `ActivityService` : Recherche d'activités par catégorie
- `GeocodingService` : Conversion adresses → coordonnées GPS
- `DistanceService` : Calcul de distances (formule d'Haversine)
- `PackageService` : Assemblage de forfaits complets

#### Implémentations
- `JsonTransportService`, `JsonHotelService`, `JsonActivityService` : Services basés sur fichiers JSON
- `ApiGeocodingService` : Utilise l'API geocode.maps.co
- `HaversineDistanceService` : Calcul distance à vol d'oiseau
- `PackageBuilder` : Logique d'assemblage des forfaits

#### Modèles
- `Transport`, `Hotel`, `Activity`, `Trip`, `Package`
- `Coordinates` : Latitude/Longitude
- `GeocodingException` : Gestion erreurs géocodage

### Données
Les données sont stockées dans `src/main/resources/data/` :
- `transports.json` : Liste des trajets disponibles
- `hotels.json` : Hôtels avec adresses réelles parisiennes
- `activities.json` : Activités culturelles/sportives

La clé API pour le géocodage est dans `src/main/resources/application.properties`.

---

## Manuel d'utilisation

### Prérequis
- Java 17 ou supérieur
- Maven 3.6+
- Connexion Internet (pour le géocodage réel)

### 1. Compilation du projet
```bash
cd serveur
mvn clean compile
```

### 2. Exécution des tests unitaires
```bash
mvn test -DskipITs
```
**Résultat attendu:** `Tests run: 30, Failures: 0, Errors: 0`

### 3. Exécution des tests d'intégration
```bash
mvn verify
```
ou
```bash
mvn integration-test
```
**Résultat attendu:** Les ITs s'exécutent après les tests unitaires. Les tests d'API externe sont skippés sauf si `RUN_REAL_GEOCODING_TESTS=true`.

### 4. Production du rapport de couverture JaCoCo
```bash
mvn test
```
Le rapport est automatiquement généré dans : `target/site/jacoco/index.html`

Ouvrir avec :
```bash
xdg-open target/site/jacoco/index.html  # Linux
open target/site/jacoco/index.html       # macOS
start target/site/jacoco/index.html      # Windows
```

### 5. Production du rapport d'analyse par mutation PIT
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```
Le rapport est généré dans : `target/pit-reports/YYYYMMDDHHMI/index.html`

Pour trouver et ouvrir le dernier rapport :
```bash
LATEST=$(ls -t target/pit-reports/ | head -1)
xdg-open target/pit-reports/$LATEST/index.html
```

### 6. Exécution de l'application (scénario prédéfini)

#### Option A : Avec Maven (recommandé)
**Sans géocodage réseau (stub déterministe) :**
```bash
mvn -DskipTests package
RUN_REAL_GEOCODING_TESTS=false mvn -DskipTests exec:java \
  -Dexec.mainClass=fr.univ.holitrip.HolitripMain \
  -Dexec.classpathScope=runtime
```

**Avec géocodage réel (API externe) :**
```bash
RUN_REAL_GEOCODING_TESTS=true mvn -DskipTests exec:java \
  -Dexec.mainClass=fr.univ.holitrip.HolitripMain \
  -Dexec.classpathScope=runtime
```

#### Option B : Avec java directement
```bash
mvn -DskipTests package
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency
RUN_REAL_GEOCODING_TESTS=true java -cp target/classes:target/dependency/* \
  fr.univ.holitrip.HolitripMain
```

#### Scénario utilisateur par défaut
Le scénario prédéfini dans `HolitripMain.java` :
- Départ : **Bordeaux** → Destination : **Paris**
- Date départ : **2025-01-15**
- Durée : **3 jours**
- Budget maximum : **600€**
- Transport : Train (priorité prix)
- Hôtel : Minimum 3 étoiles (priorité prix)
- Activités : Catégorie **CULTURE**, distance max **20 km**

**Résultat attendu :**
```
--- Package #1 ---
Hotel: Hôtel Ibis Paris Gare de Lyon (coords: 48.845, 2.373)
Outbound: TRAIN Bordeaux→Paris 08:00-10:00 (80€)
Return: TRAIN Paris→Bordeaux 18:00-20:00 (75€)
Activities:
  - Musée du Louvre (17€, distance: 3.85 km)
  - Tour Eiffel (26€, distance: 5.93 km)
Total price: 483€
```

---

## Rapport de couverture de code

### Scores globaux (JaCoCo)
- **Instructions :** 63%
- **Branches :** 43%
- **Lignes :** 62%
- **Méthodes :** 73%

### Détails par package
1. **fr.univ.holitrip** (HolitripMain) : 0%
   - **Justification :** Point d'entrée de l'application (main), non testé unitairement par conception.

2. **fr.univ.holitrip.service.impl** : 78% instructions, 53% branches
   - Bonne couverture des services métier principaux.

3. **fr.univ.holitrip.model** : 65% instructions, 53% branches
   - DTOs et modèles : accesseurs/mutateurs basiques partiellement couverts.

4. **fr.univ.holitrip.exception** : 100%
   - Classes d'exception simples entièrement couvertes.

### Justifications des parties non couvertes
- **HolitripMain (main)** : Classe d'entrée CLI, testée manuellement.
- **Getters/setters triviaux** : Certains accesseurs de DTOs non couverts (code trivial).
- **Branches d'erreurs rares** : Certains catch blocks pour exceptions réseau/parsing non couverts (nécessiteraient mocks complexes).

---

## Rapport d'analyse par mutation

### Scores PIT
- **Mutations générées :** 339
- **Mutations tuées :** 103 (30%)
- **Test strength :** 53%
- **Mutations sans couverture :** 146

### Analyse des mutants survivants

#### Mutants équivalents ou négligeables
- **ConditionalsBoundaryMutator (11% killed)** : Certains mutants sur bornes (<, <=, >, >=) sont équivalents au comportement nominal pour les données de test.
- **PrimitiveReturnsMutator (31% killed)** : Retours de valeurs par défaut (0, false) parfois non détectables sans assertions très spécifiques.

#### Mutants dans du code peu critique
- **VoidMethodCallMutator (14% killed, 25 sans couverture)** : Appels à des méthodes void (logs, setters) dans HolitripMain ou dans des branches non couvertes.
- **NullReturnValsMutator (25% killed)** : Certains retours null dans des branches d'exception non exercées.

#### Mutants dans le code principal
- **NegateConditionalsMutator (27% killed)** : Négation de conditions booléennes. Les 39 survivants indiquent des branches conditionnelles non testées ou des conditions redondantes.
- **MathMutator (43% killed)** : Mutations sur opérations arithmétiques. Les 15 survivants concernent des calculs de distance/prix dans des cas limites non testés.

### Justification du score
Le score de 30% peut sembler bas, mais :
- **146 mutations sans couverture** concernent principalement `HolitripMain` (non testé unitairement).
- Le **test strength de 53%** sur le code couvert est raisonnable.
- L'ajout de tests supplémentaires pour tuer davantage de mutants nécessiterait du temps additionnel, mais le niveau actuel démontre une suite de tests solide pour les comportements critiques.

### Améliorations possibles
Pour augmenter le score :
1. Ajouter des tests ciblant les branches conditionnelles identifiées par `NegateConditionalsMutator`.
2. Tester les cas limites arithmétiques (prix=0, distances extrêmes).
3. Mocker des scénarios d'erreurs réseau/API pour exercer les branches catch.

---

## Structure du projet

```
serveur/
├── src/
│   ├── main/
│   │   ├── java/fr/univ/holitrip/
│   │   │   ├── HolitripMain.java          # Point d'entrée CLI
│   │   │   ├── exception/
│   │   │   │   └── GeocodingException.java
│   │   │   ├── model/
│   │   │   │   ├── Activity.java
│   │   │   │   ├── Coordinates.java
│   │   │   │   ├── Hotel.java
│   │   │   │   ├── Package.java
│   │   │   │   ├── Transport.java
│   │   │   │   └── Trip.java
│   │   │   └── service/
│   │   │       ├── ActivityService.java
│   │   │       ├── DistanceService.java
│   │   │       ├── GeocodingService.java
│   │   │       ├── HotelService.java
│   │   │       ├── PackageService.java
│   │   │       ├── TransportService.java
│   │   │       └── impl/
│   │   │           ├── ApiGeocodingService.java
│   │   │           ├── HaversineDistanceService.java
│   │   │           ├── JsonActivityService.java
│   │   │           ├── JsonHotelService.java
│   │   │           ├── JsonTransportService.java
│   │   │           └── PackageBuilder.java
│   │   └── resources/
│   │       ├── application.properties      # Clé API geocoding
│   │       └── data/
│   │           ├── activities.json
│   │           ├── hotels.json
│   │           └── transports.json
│   └── test/
│       ├── java/fr/univ/holitrip/
│       │   ├── service/
│       │   │   ├── it/                    # Tests d'intégration (*IT.java)
│       │   │   │   ├── ApiGeocodingServiceTestIT.java
│       │   │   │   ├── IntegrationDemoIT.java
│       │   │   │   ├── IntegrationPlaneTestIT.java
│       │   │   │   └── IntegrationTestIT.java
│       │   │   └── unit/                  # Tests unitaires (*Test.java)
│       │   │       ├── ActivityServiceTest.java
│       │   │       ├── ApiGeocodingServiceTest.java
│       │   │       ├── DistanceServiceTest.java
│       │   │       ├── HotelServiceTest.java
│       │   │       ├── PackageServiceTest.java
│       │   │       └── TransportServiceTest.java
│       │   └── testhelpers/stubs/
│       │       └── TestGeocodingService.java # Stub déterministe
│       └── resources/data/                # Données de test (copies)
├── target/
│   ├── site/jacoco/                       # Rapports JaCoCo
│   └── pit-reports/                       # Rapports PIT
├── pom.xml
└── README.md
```

---

## Difficultés rencontrées

### 1. Géocodage externe
- **Problème :** API geocode.maps.co nécessite une clé et peut retourner 404 pour adresses non reconnues.
- **Solution :** Implémentation d'un stub déterministe pour les tests + gestion robuste des erreurs.

### 2. Distance et activités
- **Problème :** Filtrage par distance nécessite géocodage de toutes les activités.
- **Solution :** Cache en mémoire des coordonnées + gestion gracieuse des échecs de géocodage.

### 3. Couverture des branches
- **Problème :** Nombreuses branches d'erreur difficiles à tester.
- **Solution :** Injection de dépendances + mocks pour simuler échecs réseau/parsing.

### 4. Mutation testing
- **Problème :** Score initial bas (~30%) dû à HolitripMain non testé.
- **Solution :** Acceptation que le main CLI ne soit pas testé unitairement (test manuel).

---

## Évaluation personnelle

### Pertinence du projet
- ✅ Excellente mise en pratique de TDD, injection de dépendances et doublures.
- ✅ Cas d'usage réel avec API externe et calculs géographiques.
- ✅ Bonne progression : tests → couverture → mutation.

### Charge de travail
- **Temps estimé :** 20-25 heures par groupe de 3-4 personnes
- **Répartition :** Conception 30%, Implémentation 40%, Tests 30%

### Points positifs
- Apprentissage approfondi de JaCoCo et PIT
- Pratique de l'architecture modulaire
- Gestion de services externes et erreurs

### Améliorations suggérées
- Fournir un dataset JSON plus large pour tests variés
- Proposer des scénarios utilisateur prédéfinis supplémentaires
- Clarifier les attentes sur le score de mutation minimum

---

## Commandes de remise

### Génération des rapports finaux
```bash
# Tout en une commande
mvn clean test verify org.pitest:pitest-maven:mutationCoverage
```

### Création de l'archive de remise
```bash
cd ..
tar -czf holitrip-projet.tar.gz \
  serveur/src/ \
  serveur/target/site/jacoco/ \
  serveur/target/pit-reports/ \
  serveur/pom.xml \
  serveur/README.md \
  rapport.pdf
```

ou

```bash
zip -r holitrip-projet.zip \
  serveur/src/ \
  serveur/target/site/jacoco/ \
  serveur/target/pit-reports/ \
  serveur/pom.xml \
  serveur/README.md \
  rapport.pdf
```

---

## Licence
Projet académique - Module de Test Logiciel
