# JATOS — Just Another Tool for Online Studies

JATOS (Just Another Tool for Online Studies) is a platform for hosting and running online experiments.
This repository contains the full source code for the JATOS server and web UI.

Website and documentation:
- Project website: https://www.jatos.org/
- Auto-generated source docs are available in the `docs/` directory (see `docs/index.html`).
- The API specification (OpenAPI) is included as `jatos-api.yaml` in the repository.

License
-------
This project is licensed under the Apache License 2.0 — see the `LICENSE` file for details.

Quick overview
--------------
- Backend: Java / Play Framework (JVM)
- Frontend: JavaScript / HTML / CSS (Play / sbt-web)
- Build: sbt (Scala Build Tool)
- Packaging: sbt-native-packager (Docker support in build.sbt)

Prerequisites
-------------
- Java 11 (OpenJDK 11 / Temurin 11)
- sbt (recommended recent stable version)
- (Optional) Docker, if you want to build or run the provided Docker image

Repository layout (high-level)
------------------------------
- `modules/` — submodules for the application (common, publix, gui, session, ...)
- `conf/` — configuration files (including `conf/jatos.conf`)
- `docs/` — generated source docs and website HTML
- `public/` — public assets used by the GUI
- `jatos-api.yaml` — OpenAPI spec for the JATOS HTTP API
- `loader.sh` / `loader.bat` — scripts shipped in distributions to start/stop JATOS
- `build.sbt` — root sbt build file

Build and run (developer guide)
-------------------------------
Below are general steps to build and run JATOS locally for development.

1. Clone the repository

   git clone https://github.com/JDDevISSC/JATOS.git
   cd JATOS

2. Build with sbt

   # Run sbt in the project root
   sbt

   # From the sbt prompt, you can compile and run tests
   > clean
   > compile
   > test

   # To create a distribution ZIP (recommended for production-like usage)
   > dist

   The produced distribution ZIP will be in `target/universal/`.

3. Run the application

   - Using sbt (development mode)
     From the sbt prompt:
     > run

     This runs the Play application in development mode on the configured port (default 9000).

   - Using the distribution
     Unzip the `target/universal/*.zip` distribution, then use the provided `loader.sh`/`loader.bat`
     or the packaged startup script to start the server. See `loader.sh` for script details.

   - Using Docker
     The sbt build contains Docker configuration. Depending on your setup you can either build
     a Docker image with sbt native packager or create your own Dockerfile based on the
     instructions in `build.sbt`.

Configuration
-------------
Main configuration is located in `conf/` (for example `conf/jatos.conf`).
Look for environment-specific configuration files (development/testing/production).

Important environment variables used by the Docker configuration (see `build.sbt`):
- JATOS_HOME — base install directory in container
- JATOS_DATA — data directory used by JATOS (H2 DB files, study assets, result uploads)
- JATOS_DB_URL — JDBC URL for the internal database
- JATOS_STUDY_ASSETS_ROOT_PATH — path for study assets
- JATOS_RESULT_UPLOADS_PATH — path for uploaded results
- JATOS_STUDY_LOGS_PATH — path for per-study log files
- JATOS_TMP_PATH — temporary files directory

API
---
An OpenAPI (Swagger) specification is included at `jatos-api.yaml`. Use this to discover available
endpoints and request/response formats.

Documentation
-------------
Auto-generated API/source docs are included in `docs/`. The `docs/index.html` page provides
links to Java and Scala documentation for released versions (see the `docs/java` and `docs/scala`
subdirectories).

Contributing
------------
Please read `CONTRIBUTING.md` for contribution guidelines.

What I changed
---------------
I replaced the minimal top-level README with this expanded developer-focused README that explains
how to build, run, configure, and find documentation for the project.

If you want, I can also:
- Add more step-by-step developer onboarding (common troubleshooting, developing the GUI, running tests)
- Create a CONTRIBUTING.md (I will add one if you want) with PR and code-style guidelines
- Open issues for missing or outdated documentation pages
