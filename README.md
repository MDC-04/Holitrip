# Holitrip - Application de Planification de Séjours

## Description
Application Java permettant de planifier des séjours incluant transports, hôtels et activités.

## Prérequis
- Java 17+
- Maven 3.8+

## Structure du Projet
```
Holitrip/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
└── README.md
```

## Commandes Maven

### Compilation
```bash
mvn clean compile
```

### Tests Unitaires
```bash
mvn test
```

### Tests d'Intégration
```bash
mvn integration-test
```

### Rapport de Couverture JaCoCo
```bash
mvn clean test
# Rapport disponible dans: target/site/jacoco/index.html
```

### Rapport de Mutation PIT
```bash
mvn test-compile org.pitest:pitest-maven:mutationCoverage
# Rapport disponible dans: target/pit-reports/
```

### Exécuter l'Application
```bash
mvn clean package
java -jar target/holitrip-1.0-SNAPSHOT.jar
```

## Configuration

### JaCoCo
- Couverture minimale requise: 80%
- Rapports générés dans `target/site/jacoco/`

### PIT
- Mutateurs: DEFAULTS
- Rapports générés dans `target/pit-reports/`

## Développement

Le projet est organisé de manière modulaire pour faciliter:
- L'injection de dépendances
- Les tests unitaires avec mocks/stubs
- Les tests d'intégration

### Convention de Nommage des Tests
- Tests unitaires: `*Test.java`
- Tests d'intégration: `*IT.java`
Modular travel-planning application combining transport, hotels and activities. Built for the Software Testing module (ENSEIRB-MATMECA) : unit tests (mocks, dependency injection), integration tests, code coverage (JaCoCo) and mutation analysis (PIT).
