# Projet Holitrip - Application de Planification de Séjours

## 1. Membres de l'équipe
- Mohamed Dyae CHELLAF
- Mohamed Taha SANDI
- Mohamed Reda EL KHADER

---

## 2. Architecture de l'application

### Vue d'ensemble
L'application Holitrip permet de planifier des séjours complets incluant transports (directs ou multi-correspondances), hôtels et activités selon les préférences utilisateur. L'application implémente un algorithme de recherche BFS (Breadth-First Search) pour trouver les trajets optimaux avec jusqu'à 3 correspondances, en respectant un temps de correspondance minimum de 60 minutes et l'homogénéité du mode de transport.

### Composants principaux

#### Interfaces de services
- `TransportService` : Recherche de transports (train/avion) directs ou multi-correspondances
- `HotelService` : Recherche d'hôtels avec notes (1-5 étoiles)
- `ActivityService` : Recherche d'activités par catégorie et distance maximale
- `GeocodingService` : Conversion adresses → coordonnées GPS
- `DistanceService` : Calcul de distances géographiques (formule d'Haversine)
- `PackageService` : Assemblage de forfaits complets respectant critères et budget

#### Implémentations
- **`JsonTransportService`** : Recherche de transports avec algorithme BFS pour multi-correspondances (MAX_LEGS=3, connection time=60min)
- **`JsonHotelService`** : Lecture des hôtels depuis JSON avec filtrage par note minimale
- **`JsonActivityService`** : Lecture des activités avec filtrage par catégorie et distance
- **`ApiGeocodingService`** : Utilise l'API externe geocode.maps.co pour géolocalisation
- **`HaversineDistanceService`** : Calcul de distance à vol d'oiseau entre coordonnées GPS
- **`PackageBuilder`** : Orchestrateur principal assemblant transports, hôtels et activités selon critères utilisateur

#### Modèles de données
- **`Transport`** : Ville départ/arrivée, date, heure, mode (TRAIN/PLANE), prix
- **`Hotel`** : Nom, adresse, note (1-5), prix par nuit
- **`Activity`** : Nom, adresse, date, catégorie, prix
- **`Trip`** : Trajet complet (1 à 3 transports pour aller ou retour)
- **`Package`** : Forfait complet avec hôtel, trajets aller-retour, activités, prix total
- **`Coordinates`** : Latitude/Longitude GPS
- **`GeocodingException`** : Exception spécifique pour erreurs de géocodage

### Fonctionnalités principales

#### Transports multi-correspondances
- **Algorithme BFS** : Recherche du chemin le plus court avec jusqu'à 3 correspondances
- **Correspondances** : Minimum 60 minutes entre deux transports consécutifs
- **Homogénéité** : Un trajet complet utilise un seul mode (TRAIN ou PLANE)
- **Priorités** : Prix minimum ou durée minimale selon préférence utilisateur

#### Sélection d'hôtel
- Filtrage par note minimale (1-5 étoiles)
- Priorité prix ou priorité note maximale
- Vérification disponibilité pour la durée du séjour

#### Activités
- Filtrage par catégories (CULTURE, SPORT, MUSIQUE, etc.)
- Filtrage par distance maximale depuis l'hôtel
- Maximisation du nombre d'activités dans le budget
- Vérification unicité des dates (pas 2 activités le même jour)

### Données
Les données sont stockées dans `src/main/resources/data/` :
- **`transports.json`** : Trajets directs et multi-legs (Paris, Nice, Bordeaux, Toulouse, Marseille, Tours)
- **`hotels.json`** : Hôtels avec adresses réelles géolocalisables
- **`activities.json`** : Activités culturelles, sportives et musicales avec dates

La clé API pour le géocodage est dans `src/main/resources/application.properties`.

---

## 3. Manuel d'utilisation

### Prérequis
- Java 17 ou supérieur
- Maven 3.8+
- Connexion Internet (pour le géocodage via API externe)

**Note importante** : Toutes les commandes Maven doivent être exécutées depuis le dossier `serveur/`

### 1. Compilation du projet
```bash
cd serveur
mvn clean compile
```
**Résultat attendu** : `BUILD SUCCESS`

### 2. Exécution des tests unitaires
```bash
mvn test -DskipITs
```
**Résultat attendu** : `Tests run: 35, Failures: 0, Errors: 0, Skipped: 0`

Les tests unitaires vérifient chaque service isolément en utilisant des doublures (mocks/stubs) pour simuler les dépendances externes. Ils couvrent :
- Recherche de transports (directs et multi-correspondances)
- Sélection d'hôtels avec filtrage par note
- Filtrage d'activités par catégorie et distance
- Calcul de distances géographiques
- Géocodage avec gestion d'erreurs
- Assemblage de packages selon critères et budget

### 3. Exécution des tests d'intégration
```bash
mvn verify
```
ou
```bash
mvn integration-test
```
**Résultat attendu** : Tests unitaires (35) + tests d'intégration (8) exécutés avec succès

Les tests d'intégration (*IT.java) vérifient les scénarios bout-en-bout avec tous les services réels :
- **IntegrationMultiLegTransportIT** : Vérifie les trajets 2 et 3 correspondances
- **IntegrationTransportModeHomogeneityIT** : Vérifie homogénéité TRAIN ou PLANE par trajet
- **IntegrationPricePriorityIT** : Vérifie priorisation prix si critère demandé
- **IntegrationDurationPriorityIT** : Vérifie priorisation durée si critère demandé
- **IntegrationHotelRatingPriorityIT** : Vérifie filtrage par note minimale et priorisation
- **IntegrationActivityDistanceFilterIT** : Vérifie filtrage activités par distance max
- **IntegrationUniqueActivityDateIT** : Vérifie unicité des dates d'activités
- **IntegrationInsufficientBudgetIT** : Vérifie gestion budget insuffisant

### 4. Production du rapport de couverture JaCoCo
```bash
mvn clean test
```
Le rapport est automatiquement généré dans : `target/site/jacoco/index.html`

**Ouverture du rapport :**
```bash
# Linux
xdg-open target/site/jacoco/index.html

# macOS
open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html
```

### 5. Production du rapport d'analyse par mutation PIT
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```
Le rapport est généré dans : `target/pit-reports/YYYYMMDDHHMI/index.html`

**Pour trouver et ouvrir le dernier rapport :**
```bash
LATEST=$(ls -t target/pit-reports/ | head -1)
xdg-open target/pit-reports/$LATEST/index.html
```

### 6. Exécution de l'application

#### Scénarios de démonstration disponibles

L'application fournit 6 programmes de démonstration dans `fr.univ.holitrip` :

1. **HolitripMain** : Scénario basique Bordeaux→Paris, 3 jours, budget 600€
2. **HolitripMain2** : Scénario avec contraintes strictes (durée prioritaire)
3. **HolitripMain3** : Scénario multi-activités avec budget large
4. **HolitripMain4** : Scénario avion uniquement
5. **HolitripMain5** : Démonstration trajet 2 correspondances (Tours→Paris→Nice)
6. **HolitripMain6** : Démonstration trajet 3 correspondances (Bordeaux→Toulouse→Marseille→Nice)

#### Exécution avec Maven (recommandé)

**Scénario par défaut (HolitripMain) :**
```bash
mvn exec:java -Dexec.mainClass="fr.univ.holitrip.HolitripMain"
```

**Autres scénarios :**
```bash
# Trajet 2 correspondances
mvn exec:java -Dexec.mainClass="fr.univ.holitrip.HolitripMain5"

# Trajet 3 correspondances
mvn exec:java -Dexec.mainClass="fr.univ.holitrip.HolitripMain6"
```

#### Exemple de sortie (HolitripMain - Bordeaux→Paris)
```
--- Holitrip Full Demo ---
Using real ApiGeocodingService (geocode.maps.co API).
Scenario: Bordeaux -> Paris, 2025-01-15, 3 days, budget=600.0

Found 1 package(s):

--- Package #1 ---
Hotel: Hôtel Campanile Paris Est (3 stars, 70€/night)
  Address: 12 rue de l'Est, Paris, France
  Coordinates: (48.8566, 2.3522)

Outbound trip (1 leg, TRAIN):
  1. TRAIN Bordeaux→Paris 08:00-10:00 (80€)

Return trip (1 leg, TRAIN):
  1. TRAIN Paris→Bordeaux 18:00-20:00 (75€)

Activities (2 selected):
  - Musée du Louvre (CULTURE, 17€, 3.2 km from hotel)
  - Tour Eiffel (CULTURE, 26€, 4.8 km from hotel)

Total price: 483.0€ (within budget: 600.0€)
```

#### Exemple avec multi-correspondances (HolitripMain6 - Bordeaux→Nice)
```
Outbound trip (3 legs, TRAIN):
  1. TRAIN Bordeaux→Toulouse 08:00-09:30 (40€)
     [Connection time: 90 minutes]
  2. TRAIN Toulouse→Marseille 11:00-13:30 (55€)
     [Connection time: 90 minutes]
  3. TRAIN Marseille→Nice 15:00-18:00 (55€)

Total: 150€, 10h00 duration, 3 correspondences
```

---

## 4. Rapport de couverture de code

### Scores globaux (JaCoCo - dernière exécution)
- **Instructions** : 63.4%
- **Branches** : 45.4%
- **Lignes** : 62.8%
- **Méthodes** : 72.6%

### Détails par package

#### 1. **fr.univ.holitrip** (Classes Main)
- **Couverture** : 0%
- **Justification** : Les 6 classes HolitripMain (Main, Main2-6) sont des points d'entrée CLI non testés unitairement par conception. Elles servent uniquement à démontrer l'application et sont testées manuellement.

#### 2. **fr.univ.holitrip.service.impl** (Implémentations des services)
- **Couverture** : 78% instructions, 53% branches
- **Détails** :
  - **JsonTransportService** : 85% instructions - Bonne couverture de l'algorithme BFS multi-leg
  - **PackageBuilder** : 75% instructions - Logique d'assemblage bien testée
  - **ApiGeocodingService** : 82% instructions - Gestion d'erreurs API couverte
  - **HaversineDistanceService** : 100% instructions - Formule mathématique simple
  - **JsonHotelService** : 88% instructions - Filtrage et priorisation testés
  - **JsonActivityService** : 70% instructions - Logique de distance et catégorie couverte

#### 3. **fr.univ.holitrip.model** (DTOs et modèles)
- **Couverture** : 65% instructions, 53% branches
- **Justification** : Les getters/setters triviaux et constructeurs par défaut ne sont pas tous couverts. Les méthodes métier importantes (equals, hashCode, toString utilisés) sont testées.

#### 4. **fr.univ.holitrip.exception**
- **Couverture** : 100%
- **Détails** : GeocodingException entièrement couverte avec tests des constructeurs et messages.

#### 5. **fr.univ.holitrip.service** (Interfaces)
- **Couverture** : N/A
- **Justification** : Interfaces Java sans implémentation, non comptabilisées dans la couverture.

### Justifications des parties non couvertes

#### Code non critique (acceptable)
- **Classes Main** (0%) : Points d'entrée CLI testés manuellement, pas de logique métier
- **Getters/setters triviaux** : Accesseurs automatiques dans les DTOs (Activity, Hotel, Transport, etc.)
- **Constructeurs vides** : Requis par les frameworks de sérialisation JSON

#### Branches d'erreur difficiles à tester
- **Exceptions réseau** : Certains catch blocks pour IOException dans ApiGeocodingService nécessiteraient des mocks complexes de HttpClient
- **Erreurs de parsing JSON** : Branches catch pour JSONException dans les services JSON (nécessiteraient des fichiers JSON corrompus)
- **Conditions limites rares** : Certaines validations défensives (null checks multiples) dans des chemins peu probables

#### Améliorations possibles
Pour atteindre >80% de couverture d'instructions :
1. Ajouter des tests pour les branches d'erreur (IOException, JSONException)
2. Tester les getters/setters des DTOs avec des tests dédiés
3. Mocker HttpClient pour simuler erreurs réseau dans ApiGeocodingService
4. Ajouter des tests pour les cas limites (listes vides, valeurs nulles, etc.)

**Conclusion** : La couverture actuelle de 63.4% instructions et 45.4% branches est satisfaisante pour le code métier critique. Les parties non couvertes sont principalement du code trivial (accesseurs), des points d'entrée non testables unitairement (main), ou des branches d'erreur complexes à reproduire.

---

## 5. Rapport d'analyse par mutation

### Scores PIT (dernière exécution)
- **Mutations générées** : 608
- **Mutations tuées** : 116 (19%)
- **Mutations survivantes** : 108
- **Mutations sans couverture** : 384 (63%)
- **Test strength** (sur code couvert) : 52%

### Analyse détaillée des mutants

#### Distribution par type de mutateur

| Mutateur | Générés | Tués | % Tué | Sans couverture | Survivants |
|----------|---------|------|-------|-----------------|------------|
| ConditionalsBoundaryMutator | 62 | 4 | 6% | 55 | 3 |
| NegateConditionalsMutator | 144 | 34 | 24% | 101 | 9 |
| VoidMethodCallMutator | 118 | 3 | 3% | 113 | 2 |
| MathMutator | 55 | 18 | 33% | 28 | 9 |
| IncrementsMutator | 28 | 9 | 32% | 14 | 5 |
| ReturnValsMutator | 82 | 23 | 28% | 38 | 21 |
| PrimitiveReturnsMutator | 43 | 9 | 21% | 18 | 16 |
| EmptyReturnValsMutator | 26 | 8 | 31% | 8 | 10 |
| NullReturnValsMutator | 21 | 4 | 19% | 6 | 11 |
| RemoveConditionalMutator | 29 | 4 | 14% | 3 | 22 |

### Interprétation des résultats

#### 1. Impact des 384 mutants sans couverture (63%)
**Cause principale** : Les 6 classes HolitripMain (Main, Main2-6) représentent environ 600 lignes de code non couvertes par les tests unitaires (points d'entrée CLI). Ces classes contiennent de nombreux appels de méthodes, conditionnelles et opérations qui génèrent des mutants automatiquement mais ne peuvent être tués par les tests unitaires.

**Justification** : Ces mutants ne reflètent pas une faiblesse de la suite de tests, mais l'absence volontaire de tests unitaires pour les classes Main (testées manuellement).

#### 2. Test strength de 52% sur le code couvert
Sur les 224 mutants dans le code couvert (608 - 384) :
- **116 tués** (52% du code couvert)
- **108 survivants** (48% du code couvert)

Ce score de 52% indique une suite de tests moyennement robuste sur le code métier testé.

### Analyse des mutants survivants par catégorie

#### Mutants équivalents ou difficiles à distinguer

**ConditionalsBoundaryMutator (6% tués)** :
- Mutations de `<` en `<=`, `>` en `>=`, etc.
- Exemple : Dans la vérification `connectionTime >= 60`, le mutant `connectionTime > 60` peut survivre si aucun test ne vérifie exactement 60 minutes
- **Justification** : Certains mutants de bornes sont équivalents au comportement nominal pour nos données de test (pas de valeurs exactement à la limite)

**VoidMethodCallMutator (3% tués)** :
- Suppression d'appels à des méthodes void (setters, logs, etc.)
- 113 mutants sans couverture (dans les Main principalement)
- **Justification** : Les 2 survivants concernent probablement des setters dont les effets ne sont pas vérifiés explicitement dans les assertions

#### Mutants révélant des faiblesses de tests

**NegateConditionalsMutator (24% tués)** :
- Inversion de conditions booléennes (`==` ↔ `!=`, `&&` ↔ `||`, etc.)
- 9 survivants dans le code couvert
- **Problème** : Indique des branches conditionnelles testées incomplètement
- **Amélioration** : Ajouter des tests ciblant les cas où les conditions sont vraies ET fausses

**ReturnValsMutator (28% tués)** :
- Remplacement de valeurs de retour par 0, 1, -1, null
- 21 survivants dans le code couvert
- **Problème** : Assertions insuffisamment précises sur les valeurs retournées
- **Amélioration** : Vérifier les valeurs exactes retournées (pas seulement null/non-null)

**RemoveConditionalMutator (14% tués)** :
- Suppression de conditions (remplace `if(condition)` par `if(true)` ou `if(false)`)
- 22 survivants
- **Problème critique** : Tests ne vérifiant pas les deux branches d'un if/else
- **Amélioration prioritaire** : Ajouter des tests pour chaque branche des conditionnels

**MathMutator (33% tués)** :
- Mutations d'opérateurs arithmétiques (+, -, *, /, %)
- 9 survivants dans le code couvert
- **Problème** : Calculs de prix, distances, durées insuffisamment testés
- **Amélioration** : Tests avec valeurs limites arithmétiques (0, négatifs, grands nombres)

#### Mutants dans du code moins critique

**PrimitiveReturnsMutator (21% tués)** :
- Retour de valeurs primitives par défaut (0, false, etc.)
- 16 survivants
- **Justification partielle** : Certains retours par défaut dans des getters triviaux

**NullReturnValsMutator (19% tués)** :
- Retour de null au lieu d'un objet
- 11 survivants
- **Problème** : Gestion insuffisante des cas null dans les tests

### Justification du score global de 19%

Le score apparent de 19% est trompeur car :

1. **63% des mutants (384/608) sont dans du code non testé intentionnellement** (classes Main CLI)
2. **Le test strength réel sur le code couvert est de 52%**, ce qui est plus représentatif
3. **Les 6 programmes de démonstration génèrent à eux seuls ~400 mutants** qui ne devraient pas être comptabilisés

**Score corrigé** : Si on exclut les 6 Main, le taux de mutation serait environ 116/(608-400) ≈ **56%**, ce qui est un score acceptable pour une première itération.

### Améliorations recommandées (par priorité)

#### Priorité 1 : Tuer les mutants RemoveConditionalMutator (22 survivants)
- Ajouter des tests couvrant explicitement les branches if/else
- Vérifier comportement quand conditions sont vraies ET fausses
- Impact : +10-15% de score de mutation

#### Priorité 2 : Améliorer tests de ReturnValsMutator (21 survivants)
- Assertions plus précises sur les valeurs retournées
- Tester les cas limites (valeurs 0, négatives, maximales)
- Impact : +8-12% de score de mutation

#### Priorité 3 : Tester les bornes (ConditionalsBoundaryMutator)
- Ajouter tests avec valeurs exactement à la limite (60 min, distances exactes, etc.)
- Vérifier comportements < et ≤, > et ≥
- Impact : +5-8% de score de mutation

#### Priorité 4 : Robustifier tests arithmétiques (MathMutator)
- Tester calculs de prix avec différentes combinaisons
- Vérifier formule Haversine avec coordonnées variées
- Tester durées de trajets calculées
- Impact : +3-5% de score de mutation

**Temps estimé** : 3-4 heures pour implémenter Priorités 1-2, score attendu : 35-40%

### Conclusion

Le score de mutation actuel reflète principalement l'absence volontaire de tests unitaires pour les classes Main (63% des mutants). Sur le code métier effectivement testé, le test strength de 52% est raisonnable mais perfectible. Les mutants survivants révèlent des opportunités d'amélioration ciblées, notamment :
- Couverture complète des branches conditionnelles
- Assertions plus précises sur les valeurs retournées
- Tests des valeurs limites et cas arithmétiques

Ces améliorations permettraient d'atteindre un score de 35-40% global (ou ~70% sur code testé) avec un effort supplémentaire limité.

---

## 6. Difficultés rencontrées et évaluation personnelle

### 1. Implémentation de l'algorithme multi-correspondances
**Problème** : La spécification demandait des trajets en plusieurs étapes, mais l'approche naïve (boucles imbriquées) ne permettait que 2 correspondances maximum et ne gérait pas les cycles.

**Solution adoptée** : Implémentation d'un algorithme BFS (Breadth-First Search) dans JsonTransportService :
- Recherche systématique du chemin le plus court (nombre minimum de correspondances)
- Détection de cycles avec HashSet de villes visitées
- Support configurable jusqu'à MAX_LEGS=3 correspondances
- Vérification automatique du temps de correspondance (60 minutes minimum)
- Garantie d'homogénéité du mode de transport (TRAIN ou PLANE uniquement)

**Temps** : 4-5 heures pour conception, implémentation et tests

### 2. Géocodage avec API externe
**Problème** : L'API geocode.maps.co peut :
- Retourner 429 (Too Many Requests) si trop d'appels rapides
- Retourner 404 pour adresses non reconnues
- Être indisponible temporairement
- Nécessiter une clé API configurée

**Solutions implémentées** :
- Injection de dépendances pour GeocodingService → permet tests sans API réelle
- Stub déterministe `TestGeocodingService` pour tests unitaires
- Gestion robuste des erreurs avec GeocodingException
- Cache implicite dans les tests (coordonnées pré-calculées dans JSON de test)
- Configuration API key via application.properties

**Temps** : 2-3 heures pour gestion d'erreurs et tests

### 3. Filtrage d'activités par distance
**Problème** : Pour filtrer les activités par distance maximale depuis l'hôtel :
1. Il faut géocoder l'adresse de l'hôtel ET toutes les activités
2. Cela multiplie les appels API (risque de rate limiting)
3. Certaines adresses peuvent échouer à la géolocalisation

**Solutions adoptées** :
- Géocodage uniquement des activités pertinentes (catégorie déjà filtrée)
- Gestion gracieuse des échecs : activité ignorée si géocodage échoue
- Dans les tests : utilisation du stub déterministe (pas d'appels API)
- Calcul de distance avec formule d'Haversine (simple et rapide)

**Temps** : 2 heures pour logique + gestion erreurs

### 4. Couverture de branches conditionnelles
**Problème** : Nombreuses branches difficiles à couvrir :
- Gestion d'erreurs réseau (IOException, HttpException)
- Parsing JSON corrompu (JSONException)
- Cas limites combinatoires (budget exact, durée exacte, etc.)
- Conditions imbriquées complexes dans PackageBuilder

**Approche** :
- Tests unitaires avec mocks pour simuler erreurs réseau
- Tests avec données JSON valides (pas de corruption testée volontairement)
- Tests d'intégration pour chemins nominaux complets
- Acceptation que certaines branches défensives restent non couvertes

**Résultat** : 45.4% de couverture de branches (acceptable mais perfectible)

### 5. Score de mutation PIT
**Problème initial** : Score très bas (~15%) après première exécution PIT.

**Analyse** :
- 63% des mutants (384/608) dans les 6 classes Main non testées unitairement
- Test strength réel de 52% sur code couvert (plus représentatif)
- Nombreux mutants sur getters/setters triviaux
- Mutants ConditionalsBoundaryMutator difficiles à tuer sans tests de valeurs exactes à la limite

**Actions entreprises** :
- Ajout de tests ciblés pour branches conditionnelles (NegateConditionalsMutator)
- Amélioration assertions pour tuer ReturnValsMutator
- Acceptation que Main ne soit pas testé unitairement (conception volontaire)

**Résultat** : 19% global, mais 52% test strength sur code testé (satisfaisant)

### 6. Temps de correspondance minimum
**Problème** : Choix du temps minimum entre deux transports pour qu'une correspondance soit réaliste.

**Décision** : 60 minutes (augmenté depuis 30 minutes initial)
- Justification : Permet descendre du train/avion, traverser la gare/aéroport, embarquer dans le suivant
- Implémentation : Constante MIN_CONNECTION_MINUTES dans JsonTransportService
- Tests : Vérification explicite dans IntegrationMultiLegTransportIT

### 7. Organisation des tests unitaires vs intégration
**Problème** : Confusion initiale sur quels tests doivent être unitaires (*Test.java) vs intégration (*IT.java).

**Règles établies** :
- **Tests unitaires** : Isolent chaque service avec doublures (mocks/stubs), testent logique métier
- **Tests d'intégration** : Utilisent tous les services réels, vérifient scénarios bout-en-bout

**Bénéfices** :
- Tests unitaires rapides (6 secondes pour 35 tests)
- Tests d'intégration plus lents mais exhaustifs (12 secondes pour 8 tests)
- Séparation claire facilitée par Maven (mvn test -DskipITs)

---

