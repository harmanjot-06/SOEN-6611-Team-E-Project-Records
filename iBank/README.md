# iBank Smart ABM (SOEN 6611, Fall 2026, Team E, Deliverable 2)

A Java Swing implementation of the iBank Automated Banking Machine described in Deliverable 1.

## Run

You only need a JDK, version 17 or later. No IDE is required.

```bash
./build.sh            # Linux / macOS
build.bat             # Windows
```

With Maven: `mvn package`, then `java -jar target/ibank-2.0.0.jar`.

## Test

```bash
mvn test              # 52 JUnit 5 tests
mvn checkstyle:check  # Google Java Style (google_checks.xml): 0 violations
```

## Demonstration data (fictional)

| Card number          | PIN  | Holder              | Notes                                   |
|----------------------|------|---------------------|-----------------------------------------|
| 4520 0000 0000 1234  | 2468 | Alice Tremblay      | iBank client, chequing + savings, 2 payees |
| 4520 0000 0000 5678  | 1357 | Bruno Gagnon        | iBank client, low balance ($150.75)     |
| 5100 0000 0000 9012  | 8642 | Other-bank client   | Withdraw and balance only               |
| 4520 0000 0000 3456  | 9753 | Dana Roy            | Card already locked                     |
| Staff ID `CUST01`    | 7391 | Cash custodian      | Operator console                        |

## Structure

| Package     | Responsibility                                                       |
|-------------|----------------------------------------------------------------------|
| `model`     | Domain objects: Money, Account (Chequing, Savings), Card, Client ... |
| `exception` | Checked exception hierarchy rooted at `IbankException`               |
| `security`  | PBKDF2 PIN hashing, PIN policy                                       |
| `service`   | Use cases: authentication, transactions, dispenser, compliance, operator |
| `i18n`      | English/French messages (`src/main/resources/i18n`)                  |
| `ui`        | Swing screens, controller and reusable widgets                       |
| `data`      | Representative sample data                                           |

## Measurement (Problems 5 and 6)

```bash
python3 tools/measure.py src/main/java measurements
```

This writes `measurements/sloc_per_class.csv` and `measurements/readability_per_method.csv`.
Test code (`src/test`) is not measured.
