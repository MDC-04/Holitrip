# Projet Holitrip - Application de Planification de Séjours

## Membres de l'équipe
- Mohamed Dyae CHELLAF
- Mohamed Taha SANDI
- Mohamed Reda EL KHADER
- Mathieu MOREL

---

## Architecture de l'application

L'application Holitrip permet de planifier des séjours complets incluant transports (directs ou multi-correspondances), hôtels et activités selon les préférences utilisateur. L'architecture repose sur l'injection de dépendances avec des interfaces et implémentations séparées pour faciliter les tests unitaires avec doublures.

### Services

| Interface | Implémentation | Responsabilité |
|-----------|----------------|----------------|
| `TransportService` | `JsonTransportService` | Recherche de transports avec algorithme BFS (max 3 legs, 60min connexion, homogénéité mode) |
| `HotelService` | `JsonHotelService` | Recherche d'hôtels par ville, note minimale et prix |
| `ActivityService` | `JsonActivityService` | Recherche d'activités avec filtrage par catégorie et distance |
| `GeocodingService` | `ApiGeocodingService` | Conversion adresses → coordonnées GPS (API geocode.maps.co) |
| `DistanceService` | `HaversineDistanceService` | Calcul de distance à vol d'oiseau (formule d'Haversine) |
| `PackageService` | `PackageBuilder` | Assemblage des forfaits complets selon critères utilisateur |

### Classe utilitaire
- **`TransportHelper`** : Validation des correspondances (isValidConnection, isSameMode, getTripDuration)

### Modèles
- `Transport`, `Trip`, `Hotel`, `Activity`, `Package`, `Coordinates`

---

## Manuel d'utilisation

### Prérequis
- Java 17 ou supérieur
- Maven 3.8+
- Connexion Internet (pour le géocodage via API externe)

**Note importante** : Toutes les commandes Maven doivent être exécutées depuis le dossier `serveur/`

### 1. Compilation du projet
```bash
mvn clean compile
```

### Tests unitaires (107 tests)
```bash
mvn test -DskipITs
```

### Tests d'intégration (17 tests)
```bash
mvn verify
```

### Rapport de couverture JaCoCo
```bash
mvn clean test jacoco:report
```
Rapport : `target/site/jacoco/index.html`

### Rapport de mutation PIT
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```
Rapport : `target/pit-reports/*/index.html`

### Exécution de l'application
```bash
mvn exec:java -Dexec.mainClass="fr.univ.holitrip.HolitripMain"
mvn exec:java -Dexec.mainClass="fr.univ.holitrip.HolitripMain2"
```

---

## Scores de tests

### Couverture JaCoCo
- **Instructions** : 93% (1809/1936)
- **Branches** : 78% (253/322)
- **Lignes** : 91% (409/451)
- **Méthodes** : 97% (58/60)
- **Classes analysées** : 13 (exclusion HolitripMain* et DTOs)

### Mutation PIT
- **Mutations générées** : 281
- **Mutations tuées** : 215 (77%)
- **Test strength** : 81%
- **Couverture lignes mutées** : 90% (398/442)

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


---
### Arborescence

| Classe | Instructions | Branches | Commentaire |
|--------|--------------|----------|-------------|
```
serveur/
├── src/main/java/fr/univ/holitrip/
│   ├── service/                    # Interfaces
│   │   ├── TransportService.java
│   │   ├── HotelService.java
│   │   ├── ActivityService.java
│   │   ├── GeocodingService.java
│   │   ├── DistanceService.java
│   │   └── PackageService.java
│   ├── service/impl/               # Implémentations
│   │   ├── JsonTransportService.java
│   │   ├── JsonHotelService.java
│   │   ├── JsonActivityService.java
│   │   ├── ApiGeocodingService.java
│   │   ├── HaversineDistanceService.java
│   │   └── PackageBuilder.java
│   ├── util/                       # Utilitaires
│   │   └── TransportHelper.java
│   ├── model/                      # Modèles
│   │   ├── Transport.java
│   │   ├── Trip.java
│   │   ├── Hotel.java
│   │   ├── Activity.java
│   │   ├── Package.java
│   │   └── Coordinates.java
│   ├── exception/
│   │   └── GeocodingException.java
│   └── HolitripMain.java 
├── src/main/resources/
│   ├── data/
│   │   ├── transports.json
│   │   ├── hotels.json
│   │   └── activities.json
│   └── application.properties
├── src/test/java/fr/univ/holitrip/
│   ├── service/unit/               # Tests unitaires (107)
│   │   ├── PackageServiceTest.java (40 tests)
│   │   ├── TransportServiceTest.java (10 tests)
│   │   ├── HotelServiceTest.java (9 tests)
│   │   ├── ActivityServiceTest.java (3 tests)
│   │   ├── ApiGeocodingServiceTest.java (6 tests)
│   │   ├── DistanceServiceTest.java (2 tests)
│   │   ├── TransportHelperTest.java (24 tests)
│   │   └── PackageTest.java (13 tests)
│   └── service/integration/         # Tests d'intégration (17)
│       └── *.IT.java
└── pom.xml