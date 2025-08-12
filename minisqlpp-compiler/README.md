# MiniSQL++ Compiler

MiniSQL++ is a compact SQL-subset compiler with a table-driven scanner, LL(1) parser, AST, type checker, CSV interpreter, and Oracle SQL emitter. The CLI is built with picocli.

## Quick Start for Everyone

### GitHub Web UI
1. Upload the project to a new GitHub repository.
2. The **CI workflow** builds the shaded JAR and stores it as an artifact.
3. Download the `*-jar-with-dependencies.jar` from the workflow run.

### Releases
1. Create a tag like `v1.0.0`.
2. The **Release workflow** attaches the shaded JAR to the GitHub release automatically.

### Docker
Run the published image from GitHub Container Registry:

```
docker run --rm -v $(pwd)/data:/data ghcr.io/<owner>/<repo>:latest run csv /query.sql --catalog /data/catalog.json --data /data
```

### Codespaces
Open the repository in **GitHub Codespaces**. The dev container automatically builds the project; run:

```
mvn -q -DskipTests package
java -jar target/minisqlpp-compiler-1.0-SNAPSHOT-jar-with-dependencies.jar --help
```

## Local Usage

### Build
```
# macOS/Linux
./build.sh
# Windows
build.bat
```

### Run Demo
```
# macOS/Linux
./run_demo.sh
# Windows
run_demo.bat
```

## CLI Examples
```
java -jar target/minisqlpp-compiler-1.0-SNAPSHOT-jar-with-dependencies.jar tokens query.sql
java -jar target/... ast query.sql
java -jar target/... typecheck query.sql --catalog data/catalog.json
java -jar target/... run csv query.sql --catalog data/catalog.json --data data
java -jar target/... run oracle query.sql
```

## Debug Playbook
```
mvn -X -e -DskipTests package
```
Review the stack trace near the end to locate the root cause. Fix the indicated source files and rebuild.

If you encounter build errors while using this project, paste the error message into a new issue and regenerate the full corrected files without using diffs.

## Contributing
Pull requests are welcome. Please run `mvn -q -DskipTests package` before submitting.

## License
Released under the MIT License.
